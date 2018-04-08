package org.binas.ws;

import org.binas.domain.Binas;
import org.binas.exception.EmailExistsException;
import org.binas.exception.InvalidEmailException;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import javax.jws.WebService;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

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
        if(numberOfStations < 1 || coordinates == null){
            return null;
        }

        Vector<StationView> closestKStations = new Vector<>();

        try{
            UDDINaming uddiNaming = new UDDINaming(uddiUrl);

            // get all station records in UDDI , using * wildcard
            Collection<UDDIRecord> UDDIrecords = uddiNaming.listRecords("A58_Station*");

            Vector<String> urls = new Vector<>();
            for(UDDIRecord uddiRecord : UDDIrecords){
                urls.add(uddiRecord.getUrl());
            }

            Vector<StationView> allStationViews = new Vector<>();
            StationClient stationClient = null;
            for(int i = 0; i < urls.size(); i++){
                stationClient = new StationClient(urls.elementAt(i));

                // TODO piazza question, conversion between 2 almost identical StationView
                allStationViews.addElement((StationView) stationClient.getInfo());
            }

            for(int i = 0; i < numberOfStations; i++){
                if(allStationViews.size() == 0){
                    break;
                }

                double lowestDistance = Double.POSITIVE_INFINITY;
                StationView closestStation = null;

                for(int j = 0 ; j < allStationViews.size(); j++){
                    StationView stationView = allStationViews.elementAt(j);
                    double dist = distanceBetweenCoordinates(coordinates, stationView.getCoordinate());

                    if(dist < lowestDistance){
                        lowestDistance = dist;
                        closestStation = stationView;
                    }
                }

                closestKStations.add(closestStation);
                allStationViews.removeElement(closestStation);
            }
        } catch(UDDINamingException e){
            System.out.println("Problem reaching UDDI from Binas");
        } catch(StationClientException e){
            System.out.println("Problem creating StationClient");
        }

        return closestKStations;
    }

    /* Calculate distance between 2 sets of coordinateViews */
    private double distanceBetweenCoordinates(CoordinatesView coord1, CoordinatesView coord2){
        double xDiff = coord1.getX() - coord2.getX();
        double yDiff = coord1.getY() - coord2.getY();

        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
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
                System.out.println("Problem creating StationClient");
            }

            result += stationClient.testPing(inputMessage) + "\n";
        }
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
