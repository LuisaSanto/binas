package org.binas.station.ws;

import org.binas.station.domain.Station;
import org.junit.Assert;
import org.junit.Test;

public class StationGetInfoMethodTest {

    @Test
    public void getInfo(){
        StationPortImpl stationPort = new StationPortImpl();
        StationView stationView = stationPort.getInfo();

        Assert.assertEquals(0, stationView.getFreeDocks());
        Assert.assertEquals(0, stationView.getTotalGets());
        Assert.assertEquals(0, stationView.getTotalReturns());

        Assert.assertEquals(Station.getInstance().getId(), stationView.getId());
    }
}
