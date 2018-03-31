package org.binas.ws;

import org.binas.domain.Binas;
import org.binas.exception.EmailExistsException;
import org.binas.exception.InvalidEmailException;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;

import javax.jws.WebService;
import java.util.List;

@WebService(
        endpointInterface = "org.binas.ws.BinasPortType",
        wsdlLocation = "binas.wsdl",
        name = "BinasWebService",
        portName = "BinasPort",
        targetNamespace = "http://ws.binas.org/",
        serviceName = "BinasService"
)
public class BinasPortImpl implements BinasPortType{
    private BinasEndpointManager endpointManager;
    private String uddiUrl;

    public BinasPortImpl(){ }

    public BinasPortImpl(BinasEndpointManager binasEndpointManager, String uddiUrl){
        this.endpointManager = binasEndpointManager;
        this.uddiUrl = uddiUrl;
    }

    @Override
    public List<StationView> listStations(Integer numberOfStations, CoordinatesView coordinates){
        return null;
    }

    @Override
    public StationView getInfoStation(String stationId) throws InvalidStation_Exception{




        return null;
    }

    @Override
    public int getCredit(String email) throws UserNotExists_Exception{
        return 0;
    }

    @Override
    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception{
        try{
            return Binas.getInstance().activateUser(email);
        }catch(EmailExistsException e){
            throwEmailExists("");
        }catch(InvalidEmailException e){
            throwInvalidEmail("");
        }

        return null;
    }


    @Override
    public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception, NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception{

    }

    @Override
    public void returnBina(String stationId, String email) throws FullStation_Exception, InvalidStation_Exception, NoBinaRented_Exception, UserNotExists_Exception{

    }


    // Test control operations
    /** Diagnostic operation to check if service is running. */
    @Override
    public String testPing(String inputMessage) {
        String stationBaseWsName = "A58_Station";
        String wsName = null;
        String result = "";


        // testar apenas para 3 estacoes
        for(int i = 1; i <= 3; i++){
            wsName = stationBaseWsName + i;

            StationClient stationClient = null;
            try{
                stationClient = new StationClient(uddiUrl, wsName);
            } catch(StationClientException e){
                e.printStackTrace();
            }

            try{
                System.out.printf("Contacting UDDI at %s%n", uddiUrl);
                UDDINaming uddiNaming = new UDDINaming(uddiUrl);

                System.out.println("Binas is looking up for station in UDDI with wsName: " + wsName);
                uddiNaming.lookup(wsName);

                result += stationClient.testPing(inputMessage) + "\n";
                System.out.println(result);

            }catch(UDDINamingException e){
                e.printStackTrace();
                break;
            }
        }

        /* Codigo antigo test ping para endpoints
        // If no input is received, return a default name.
        if (inputMessage == null || inputMessage.trim().length() == 0)
            inputMessage = "friend";

        // If the station does not have a name, return a default.
        String wsName = endpointManager.getWsName();
        if (wsName == null || wsName.trim().length() == 0)
            wsName = "Station";

        // Build a string with a message to return.
        StringBuilder builder = new StringBuilder();
        builder.append("Hello ").append(inputMessage);
        builder.append(" from ").append(wsName);
        return builder.toString();

        */

        return result;
    }


    @Override
    public void testClear(){
        Binas.getInstance().clearUsers();
    }

    @Override
    public void testInitStation(String stationId, int x, int y, int capacity, int returnPrize) throws BadInit_Exception{

    }

    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception{
        Binas.getInstance().clearUsers();
    }

    // Exception Helpers

    /** Helper to throw a new InvalidEmail exception. */
    private void throwInvalidEmail(final String message) throws InvalidEmail_Exception {
        InvalidEmail faultInfo = new InvalidEmail();
        faultInfo.message = message;

        throw new InvalidEmail_Exception(message, faultInfo);
    }

    /** Helper to throw a new InvalidEmail exception. */
    private void throwEmailExists(final String message) throws EmailExists_Exception {
        EmailExists faultInfo = new EmailExists();
        faultInfo.message = message;

        throw new EmailExists_Exception(message, faultInfo);
    }
}
