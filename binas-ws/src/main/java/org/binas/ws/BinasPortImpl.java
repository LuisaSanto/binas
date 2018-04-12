package org.binas.ws;

import org.binas.domain.Binas;
import org.binas.domain.BinasManager;
import org.binas.exception.EmailExistsException;
import org.binas.exception.InvalidEmailException;
import org.binas.exception.UserNotExistsException;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import javax.jws.WebService;
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
        Vector<StationView> allStationViews = (Vector<StationView>) BinasManager.getStations(uddiUrl);

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
        return BinasManager.getStationView(stationId, uddiUrl);
    }

    @Override
    public int getCredit(String email) throws UserNotExists_Exception {
        try {
            UserView userView =  Binas.getInstance().getUser(email);
            return userView.getCredit();

        } catch (UserNotExistsException userNotExists) {
            throwUserNotExists("User does not exist");
        }
        return Integer.parseInt(null);
    }

    @Override
    public UserView activateUser(String email) throws EmailExists_Exception, InvalidEmail_Exception{
        try{
            return Binas.getInstance().activateUser(email);
        }catch(EmailExistsException e){
            throwEmailExists("Email does not exist");
        }catch(InvalidEmailException e){
            throwInvalidEmail("Invalid email");
        }

        return null;
    }


    @Override
    public void rentBina(String stationId, String email) throws AlreadyHasBina_Exception, InvalidStation_Exception,
            NoBinaAvail_Exception, NoCredit_Exception, UserNotExists_Exception{
        try{
            StationView station = BinasManager.getStationView(stationId, uddiUrl);
            if(station != null){

                UserView user = Binas.getInstance().getUser(email);

                if (!user.isHasBina()){
                    if (user.getCredit() > 0) {
                        user.setCredit(user.getCredit() - 1);
                        user.setHasBina(true);
                        StationClient stationClient = BinasManager.getStationClient(stationId, uddiUrl);

                        if (stationClient != null) {
                            stationClient.getBina();
                        }
                    } else
                        throwNoCredit("User has no credit");
                } else
                    throwAlreadyHasBina("User already has bina");
            }else throwInvalidStation("Invalid station");
        } catch (UserNotExistsException userNotExists) {
            throwUserNotExists("User does not exist");
        } catch (org.binas.station.ws.NoBinaAvail_Exception e) {
            //TODO HELP HERE
            throwNoBinaAvail("No bina available");
        }
    }

    @Override
    public void returnBina(String stationId, String email) throws FullStation_Exception, InvalidStation_Exception,
            NoBinaRented_Exception, UserNotExists_Exception{
        try{
            StationView stationView = BinasManager.getStationView(stationId, uddiUrl);
            if(stationView != null){
                UserView user = Binas.getInstance().getUser(email);

                if(user.isHasBina()){
                    StationClient stationClient = BinasManager.getStationClient(stationId, uddiUrl);
                    if (stationClient != null) {
                        int bonus = stationClient.returnBina();
                        user.setCredit(user.getCredit()+bonus);
                        user.setHasBina(false);
                    }
                }
                else throwNoBinaRented("No bina rented");
            }
            else throwInvalidStation("Invalid Station");
        } catch (UserNotExistsException userNotExists) {
            throwUserNotExists("User does not exist");
        } catch (NoSlotAvail_Exception e) {
            throwFullStation("No slot available");
        }

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
        try {
            StationClient stationClient = BinasManager.getStationClient(stationId, uddiUrl);

            if(stationClient != null){
                stationClient.testInit(x, y, capacity, returnPrize);
            }else{
                System.out.println("Station not found");
            }

        } catch (org.binas.station.ws.BadInit_Exception e) {
            throwBadInit("Bad init station");
        }
    }

    @Override
    public void testInit(int userInitialPoints) throws BadInit_Exception{
        Binas.getInstance().setInitialUserPoints(userInitialPoints);
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

    /** Helper to throw a new UserNotExists exception. */
    private void throwUserNotExists(final String message) throws UserNotExists_Exception {
        UserNotExists faultInfo = new UserNotExists();
        faultInfo.message = message;

        throw new UserNotExists_Exception(message, faultInfo);
    }

    /** Helper to throw a new AlreadyHasBina exception. */
    private void throwAlreadyHasBina(final String message) throws AlreadyHasBina_Exception {
        AlreadyHasBina faultInfo = new AlreadyHasBina();
        faultInfo.message = message;

        throw new AlreadyHasBina_Exception(message, faultInfo);
    }

    /** Helper to throw a new NoCredit exception. */
    private void throwNoCredit(final String message) throws NoCredit_Exception {
        NoCredit faultInfo = new NoCredit();
        faultInfo.message = message;

        throw new NoCredit_Exception(message, faultInfo);
    }

    /** Helper to throw a new InvalidStation exception. */
    private void throwInvalidStation(final String message) throws InvalidStation_Exception {
        InvalidStation faultInfo = new InvalidStation();
        faultInfo.message = message;

        throw new InvalidStation_Exception(message, faultInfo);
    }

    /** Helper to throw a new NoBinaAvail exception. */
    private void throwNoBinaAvail(final String message) throws NoBinaAvail_Exception {
        NoBinaAvail faultInfo = new NoBinaAvail();
        faultInfo.message = message;

        throw new NoBinaAvail_Exception(message, faultInfo);
    }

    /** Helper to throw a new FullStation exception. */
    private void throwFullStation(final String message) throws FullStation_Exception {
        FullStation faultInfo = new FullStation();
        faultInfo.message = message;

        throw new FullStation_Exception(message, faultInfo);
    }

    /** Helper to throw a new NoBinaRented exception. */
    private void throwNoBinaRented(final String message) throws NoBinaRented_Exception {
        NoBinaRented faultInfo = new NoBinaRented();
        faultInfo.message = message;

        throw new NoBinaRented_Exception(message, faultInfo);
    }

    /** Helper to throw a new BadInit exception. */
    private void throwBadInit(final String message) throws BadInit_Exception {
        BadInit faultInfo = new BadInit();
        faultInfo.message = message;

        throw new BadInit_Exception(message, faultInfo);
    }
}
