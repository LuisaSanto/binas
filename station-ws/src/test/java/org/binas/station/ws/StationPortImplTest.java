package org.binas.station.ws;

import org.binas.station.domain.Station;
import org.binas.station.domain.exception.NoBinaAvailException;
import org.binas.station.domain.exception.NoSlotAvailException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.fail;

public class StationPortImplTest {
    StationPortImpl stationPort;

    @Before
    public void setUp() throws Exception{
        Station.getInstance().reset();
    }

    @Test
    public void getInfo(){
        stationPort = new StationPortImpl();
        StationView stationView = stationPort.getInfo();

        Assert.assertEquals(0, stationView.getFreeDocks());
        Assert.assertEquals(0, stationView.getTotalGets());
        Assert.assertEquals(0, stationView.getTotalReturns());

        Assert.assertEquals(Station.getInstance().getId(), stationView.getId());
    }

    @Test(expected = NoBinaAvailException.class)
    public void getBina(){
        Station station = Station.getInstance();

        while (station.getFreeDocks() <= station.getMaxCapacity()){
            station.getBina();
        }
    }

    @Test(expected = NoSlotAvailException.class)
    public void returnBina(){
        Station station = Station.getInstance();

        Assert.assertEquals(0,station.getFreeDocks());

        station.returnBina();
    }

    @After
    public void tearDown() throws Exception{
        Station.getInstance().reset();
    }
}
