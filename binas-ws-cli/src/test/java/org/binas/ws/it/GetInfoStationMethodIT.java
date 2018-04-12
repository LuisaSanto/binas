package org.binas.ws.it;

import org.binas.ws.BadInit_Exception;
import org.binas.ws.InvalidStation_Exception;
import org.binas.ws.StationView;
import org.junit.Assert;
import org.junit.Test;

public class GetInfoStationMethodIT extends BaseIT{
    private String stationID = "A58_Station1";
    private String noStationID = "station ID";

    @Test
    public void notNull() throws InvalidStation_Exception{
        Assert.assertNotNull(client.getInfoStation(stationID));
    }

    @Test
    public void success() throws BadInit_Exception, InvalidStation_Exception{
        client.testInitStation(stationID, 1, 1, 15, 2);
        Assert.assertEquals(15, client.getInfoStation(stationID).getCapacity());

        client.testInitStation(stationID, 1, 2, 7, 2);
        StationView stationView = client.getInfoStation(stationID);

        Assert.assertEquals(7, stationView.getCapacity());
        Assert.assertEquals(1, stationView.getCoordinate().getX().intValue());
    }

    @Test
    public void noStationIsNull() throws InvalidStation_Exception{
        Assert.assertNull(client.getInfoStation(noStationID));
    }

}
