package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ReturnBinaMethodIT extends BaseIT{
    UserView user;
    StationView station;
    String validEmail1 = "valid.email@valid.domain";
    String validEmail2 = "valid1.email@valid.domain";
    String stationID = "A58_Station1";
    String noStationID = "station ID";

    @Before
    public void setUp() {
        client.testClear();
        try {
            user = client.activateUser(validEmail1);
            station = client.getInfoStation(stationID);

            client.testInitStation(station.getId(), station.getCoordinate().getX(), station.getCoordinate().getY(), 10, 0);
        } catch (EmailExists_Exception | InvalidEmail_Exception e) {
            System.out.println("Problem activating user in setUp, email invalid or already exists");
        } catch(InvalidStation_Exception e){
            System.out.println("Invalid station in ReturnBinaMethodIT setUp");
        } catch(BadInit_Exception e){
            e.printStackTrace();
        }

    }

    @Test(expected = InvalidStation_Exception.class)
    public void noStationView() throws UserNotExists_Exception, InvalidStation_Exception,
            FullStation_Exception, NoBinaRented_Exception {
        client.returnBina(noStationID, validEmail1);
    }

    @Test(expected = NoBinaRented_Exception.class)
    public void userHasNoBina() throws UserNotExists_Exception, InvalidStation_Exception,
            NoBinaRented_Exception, FullStation_Exception {
        client.returnBina(stationID, validEmail1);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExist() throws UserNotExists_Exception, InvalidStation_Exception,
            FullStation_Exception, NoBinaRented_Exception {
        client.returnBina(stationID, validEmail2);
    }

    @Test(expected = FullStation_Exception.class)
    public void noSlotAvail() throws UserNotExists_Exception, InvalidStation_Exception,
            FullStation_Exception, NoBinaRented_Exception, AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {

        client.rentBina(stationID, validEmail1);
        setStationAvailableBinas(0);
        client.returnBina(stationID, validEmail1);
    }

    private void setStationAvailableBinas(int availBinas){
        try{
            client.testInitStation(station.getId(), station.getCoordinate().getX(), station.getCoordinate().getY(), availBinas, 0);
        } catch(BadInit_Exception e){
            e.printStackTrace();
        }
    }

    @After
    public void tearDown(){
        client.testClear();
    }
}
