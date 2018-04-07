package org.binas.ws;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.*;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;

/** The endpoint manager starts and registers the service. */
public class BinasEndpointManager {

    /** UDDI naming server location */
    private String uddiURL = null;
    /** Web Service name */
    private String wsName = null;

    /** Get Web Service UDDI publication name */
    public String getWsName() {
        return wsName;
    }

    /** Web Service location to publish */
    private String wsURL = null;

    /** Port implementation */
    private BinasPortImpl portImpl = null;

    /** Obtain Port implementation */
    public BinasPortType getPort() {
        return portImpl;
    }

    /** Web Service end point */
    private Endpoint endpoint = null;

     /** UDDI Naming instance for contacting UDDI server */
     private UDDINaming uddiNaming = null;

     /** Get UDDI Naming instance for contacting UDDI server */
     UDDINaming getUddiNaming() {
        return uddiNaming;
     }

    /** output option */
    private boolean verbose = true;

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /** constructor with provided UDDI location, WS name, and WS URL */
    public BinasEndpointManager(String uddiURL, String wsName, String wsURL) {
        this.uddiURL = uddiURL;
        this.wsName = wsName;
        this.wsURL = wsURL;

        portImpl = new BinasPortImpl(this, uddiURL);
    }

    /** constructor with provided web service URL */
    public BinasEndpointManager(String wsName, String wsURL) {
        this.wsName = wsName;
        this.wsURL = wsURL;
    }

    /* end point management */

    public void start() throws Exception {
        try {
            // publish end point
            endpoint = Endpoint.create(this.portImpl);
            if (verbose) {
                System.out.printf("Starting %s%n", wsURL);
            }
            endpoint.publish(wsURL);
        } catch (Exception e) {
            endpoint = null;
            if (verbose) {
                System.out.printf("Caught exception when starting: %s%n", e);
            }
            throw e;
        }
        publishToUDDI();
    }

    public void awaitConnections() {
        if (verbose) {
            System.out.println("Binas Awaiting connections");
            System.out.println("Press enter to shutdown");
        }
        try {
            System.in.read();
        } catch (IOException e) {
            if (verbose) {
                System.out.printf("Caught i/o exception when awaiting requests: %s%n", e);
            }
        }
    }

    public void stop() throws Exception {
        try {
            if (endpoint != null) {
                // stop end point
                endpoint.stop();
                if (verbose) {
                    System.out.printf("Stopped %s%n", wsURL);
                }
            }
        } catch (Exception e) {
            if (verbose) {
                System.out.printf("Caught exception when stopping: %s%n", e);
            }
        }
        this.portImpl = null;
        unpublishFromUDDI();
    }

    /* UDDI */

    void publishToUDDI() throws Exception {
        System.out.printf("Publishing '%s' to UDDI at %s%n", wsName, uddiURL);
        uddiNaming = new UDDINaming(uddiURL);
        uddiNaming.rebind(wsName, wsURL);
    }

    void unpublishFromUDDI() {
        try {
            if (uddiNaming != null) {
                // delete from UDDI
                uddiNaming.unbind(wsName);
                System.out.printf("Deleted '%s' from UDDI%n", wsName);
            }
        } catch (Exception e) {
            System.out.printf("Caught exception when deleting: %s%n", e);
        }
    }
}
