package org.binas.station.ws.cli;

import org.binas.station.ws.*;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import javax.xml.ws.BindingProvider;
import java.util.Map;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

/**
 * Client port wrapper.
 *
 * Adds easier end point address configuration to the Port generated by
 * wsimport.
 */
public class StationClient implements StationPortType {

	/** WS service */
	StationService service = null;

	/** WS port (port type is the interface, port is the implementation) */
	StationPortType port = null;

	/** UDDI server URL */
	private String uddiURL = null;

	/** WS name */
	private String wsName = null;

	/** WS end point address */
	private String wsURL = null; // default value is defined inside WSDL

	public String getWsURL() {
		return wsURL;
	}

	/** output option **/
	private boolean verbose = true;

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/** constructor with provided web service URL , USE WHEN WE HAVE THE WSURL FROM UDDI or other means*/
	public StationClient(String wsURL) throws StationClientException {
		this.wsURL = wsURL;
		createStub();
	}

	/** constructor with provided UDDI location and name, USE WHEN WE DONT KNOW THE WSURL */
	public StationClient(String uddiURL, String wsName) throws StationClientException {
	    System.out.println("0");
		this.uddiURL = uddiURL;
		this.wsName = wsName;

		System.out.println("1");
		uddiLookup();
		System.out.println("2");
		createStub();
	}

	/** UDDI lookup */
	private void uddiLookup() throws StationClientException {
	    System.out.println("StationClient looking up UDDI for wsURl");

        try{
            System.out.printf("Client contacting UDDI at %s%n", uddiURL);
            UDDINaming uddiNaming = new UDDINaming(uddiURL);

            System.out.println("Client is looking up for station in UDDI with wsName: " + wsName);
            String endpointAddress = uddiNaming.lookup(wsName);

            this.wsURL = endpointAddress;

            if (endpointAddress == null) {
                System.out.println("Not found!");
                return;
            } else {
                System.out.printf("Found %s%n", endpointAddress);
            }
        }catch(UDDINamingException e){
            e.printStackTrace();
        }
	}


	/** Stub creation and configuration */
	private void createStub() {
		 if (verbose)
		    System.out.println("Creating stub ...");
		 service = new StationService();
		 port = service.getStationPort();
		 System.out.println("creating client stub : port = " + port);

		 if (wsURL != null) {
			 if (verbose)
				 System.out.println("Setting endpoint address ...");
				 BindingProvider bindingProvider = (BindingProvider) port;
				 Map<String, Object> requestContext = bindingProvider.getRequestContext();
				 requestContext.put(ENDPOINT_ADDRESS_PROPERTY, wsURL);
		 }
	}

	// remote invocation methods ----------------------------------------------

	 @Override
	 public StationView getInfo() {
	    return port.getInfo();
	 }

	 @Override
	 public void getBina() throws NoBinaAvail_Exception {
	    port.getBina();
	 }

	 @Override
	 public int returnBina() throws NoSlotAvail_Exception {
	    return port.returnBina();
	 }

	// test control operations ------------------------------------------------

	 @Override
	 public String testPing(String inputMessage) {
	    return port.testPing(inputMessage);
	 }

	 @Override
	 public void testClear() {
	    port.testClear();
	 }

	 @Override
	 public void testInit(int x, int y, int capacity, int returnPrize) throws BadInit_Exception {
	    port.testInit(x, y, capacity, returnPrize);
	 }

}
