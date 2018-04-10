package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class RentBinaMethodIT extends BaseIT {
    private String validEmail1 = "valid.email@valid.domain";
    private String validEmail2 = "valid1.email@valid.domain";
    private String stationID = "A58_Station1";
    private String noStationID = "station ID";
    UserView user;
    StationView station;


    @Before
    public void setUp() {
        //client.testClear();
        try {
            user = client.activateUser(validEmail1);
            station = client.getInfoStation(stationID);
            client.testInitStation(stationID, 1, 1, 20, 2);
            System.out.println(station.getId());
            user.setCredit(10);
            user.setHasBina(false);


        } catch (EmailExists_Exception | InvalidEmail_Exception | InvalidStation_Exception e) {
            //TODO SOMETHING HERE
            System.out.println("yay error");
        } catch (BadInit_Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = InvalidStation_Exception.class)
    public void noStationView() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
       client.rentBina(noStationID, validEmail1);
    }

    @Test(expected = AlreadyHasBina_Exception.class)
    public void userHasBina() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        user.setHasBina(true);
        client.rentBina(stationID, validEmail1);
    }

    @Test(expected = NoCredit_Exception.class)
    public void userHasNoCredit() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        user.setCredit(0);
        client.rentBina(stationID, validEmail1);
    }

    @Test(expected = NoCredit_Exception.class)
    public void userHasNegativeCredit() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        user.setCredit(-1);
        client.rentBina(stationID, validEmail1);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExist() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        client.rentBina(stationID, validEmail2);
    }

    @Test(expected = NoBinaAvail_Exception.class)
    public void noBinaAvail() throws UserNotExists_Exception, InvalidStation_Exception,
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception, BadInit_Exception {
        station.setAvailableBinas(0);
        client.rentBina(stationID, validEmail1);
    }

    @After
    public void tearDown(){
        client.testClear();
    }

}
