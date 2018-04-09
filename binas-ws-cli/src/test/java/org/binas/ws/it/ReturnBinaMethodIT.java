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
    String stationID = "station ID";
    String noStationID = "station ID 2";

    @Before
    public void setUp() {
        client.testClear();
        try {
            user = client.activateUser(validEmail1);
            user.setHasBina(true);
            user.setEmail(validEmail1);
            station.setId(stationID);
            station.setAvailableBinas(10);
        } catch (EmailExists_Exception | InvalidEmail_Exception e) {
            //TODO SOMETHING HERE
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
        user.setHasBina(false);
        client.returnBina(stationID, validEmail1);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExist() throws UserNotExists_Exception, InvalidStation_Exception,
            FullStation_Exception, NoBinaRented_Exception {
        client.returnBina(stationID, validEmail2);
    }

    @Test(expected = FullStation_Exception.class)
    public void noSlotAvail() throws UserNotExists_Exception, InvalidStation_Exception,
            FullStation_Exception, NoBinaRented_Exception {
        station.setAvailableBinas(0);
        client.returnBina(stationID, validEmail1);
    }


    @After
    public void tearDown(){
        client.testClear();
    }
}
