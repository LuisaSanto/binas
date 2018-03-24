package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.binas.station.ws.NoSlotAvail_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class StationReturnBinaMethodTestIT extends BaseIT {

    @Before
    public void setUp(){
        client.testClear();
    }

    @Test
    public void returnBinaMoreThanFreeDocks(){
        try{
            // when the test starts there should be no free docks yet
            client.returnBina();
            fail();
        }catch(NoSlotAvail_Exception nsae){

        }
    }

    @Test
    public void returnBinaLessThanFreeDocks(){
        try{
            client.testInit(5,5,20,0);
            client.getBina();
        } catch(BadInit_Exception e){
            e.printStackTrace();
        } catch(NoBinaAvail_Exception nbae){

        }

        try{
            // we only retrieved 1 bina from station, so returning one should not return exception
            client.returnBina();
        }catch(NoSlotAvail_Exception nsae){
            fail();
        }
    }

    @After
    public void tearDown() {
        client.testClear();
    }
}
