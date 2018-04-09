package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class RentBinaMethodIT extends BaseIT {
    UserView user;
    StationView station;
    String validEmail1 = "valid.email@valid.domain";
    String validEmail2 = "valid1.email@valid.domain";
    String stationID = "station ID";
    String noStationID = "station ID 2";
    @Before
    public void setUp() {
        client.testClear();
        try {
            user = client.activateUser(validEmail1);
            user.setCredit(10);
            user.setHasBina(false);
            user.setEmail(validEmail1);
            station.setAvailableBinas(10);
            station.setId(stationID);
        } catch (EmailExists_Exception | InvalidEmail_Exception e) {
            //TODO SOMETHING HERE
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
            AlreadyHasBina_Exception, NoBinaAvail_Exception, NoCredit_Exception {
        station.setAvailableBinas(0);
        client.rentBina(stationID, validEmail1);
    }


    @After
    public void tearDown(){
        client.testClear();
    }

}
