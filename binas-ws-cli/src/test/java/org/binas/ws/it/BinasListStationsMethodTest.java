package org.binas.ws.it;

import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class BinasListStationsMethodTest extends BaseIT {
    CoordinatesView zeroCoordinatesView = new CoordinatesView();

    @Before
    public void setUp() throws Exception{
        zeroCoordinatesView.setX(0);
        zeroCoordinatesView.setY(0);
    }

    @Test
    public void invalidNumberStations(){
        client.listStations(-1, new CoordinatesView());
    }

    @Test
    public void nullCoordinates(){
        client.listStations(1, null);
    }

    @Test
    public void successKStations(){
        for(int k = 1; k < 5; k ++){
            List<StationView> stationViews = client.listStations(k, zeroCoordinatesView);

            Assert.assertNotNull(stationViews);

            // if there are less stations than k, still valid to:
            Assert.assertTrue(stationViews.size() <= k);

            for(int j = 0; j < stationViews.size(); j++){
                Assert.assertNotNull(stationViews.get(j));
            }
        }
    }



}
