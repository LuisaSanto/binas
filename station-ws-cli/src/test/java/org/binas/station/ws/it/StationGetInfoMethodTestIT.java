package org.binas.station.ws.it;

import org.binas.station.ws.StationView;
import org.junit.Assert;
import org.junit.Test;

public class StationGetInfoMethodTestIT extends BaseIT {

    @Test
    public void getInfo() {
        StationView stationView = client.getInfo();

        Assert.assertEquals(0, stationView.getFreeDocks());
        Assert.assertEquals(0, stationView.getTotalGets());
        Assert.assertEquals(0, stationView.getTotalReturns());

    }
}
