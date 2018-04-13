package org.binas.ws.it;

import org.binas.ws.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MultipleStationsIT extends BaseIT {
    private String validEmail1 = "valid.email@valid.domain";
    private String validEmail2 = "valid1.email@valid.domain";
    private String[] stationIDs = { "A58_Station1", "A58_Station2" , "A58_Station3" };
    StationView station1, station2, station3;
    UserView user1, user2;

    @Before
    public void setUp() throws Exception{
        user1 = null;
        user2 = null;

        client.testInit(10);
        user1 = client.activateUser(validEmail1);
        user2 = client.activateUser(validEmail2);

        client.testInitStation(stationIDs[0], 22, 7, 6, 2);
        client.testInitStation(stationIDs[1], 80, 20, 12, 1);
        client.testInitStation(stationIDs[2], 50, 50, 20, 0);

        station1 = client.getInfoStation(stationIDs[0]);
        station2 = client.getInfoStation(stationIDs[1]);
        station3 = client.getInfoStation(stationIDs[2]);
    }

    @Test
    public void returnBinaCorrectBonus() throws NoBinaAvail_Exception, NoCredit_Exception, InvalidStation_Exception, AlreadyHasBina_Exception, UserNotExists_Exception, FullStation_Exception, NoBinaRented_Exception{
        client.rentBina(stationIDs[1],validEmail1);
        Assert.assertEquals(9, client.getCredit(validEmail1));

        client.rentBina(stationIDs[0],validEmail2);
        Assert.assertEquals(9, client.getCredit(validEmail1));

        client.returnBina(stationIDs[0], validEmail1);
        Assert.assertEquals(11, client.getCredit(validEmail1));

        client.returnBina(stationIDs[1], validEmail2);
        Assert.assertEquals(12, client.getInfoStation(stationIDs[1]).getCapacity());
        Assert.assertEquals(10, client.getCredit(validEmail2));

    }

    @After
    public void tearDown(){
        client.testClear();
    }

}
