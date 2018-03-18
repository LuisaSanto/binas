package org.binas.station.ws;

import org.binas.station.domain.Station;
import org.binas.station.domain.exception.NoBinaAvailException;
import org.junit.Test;

public class StationGetBinaMethodTest {

    @Test(expected = NoBinaAvailException.class)
    public void getBina(){
        Station station = Station.getInstance();

        while (station.getFreeDocks() <= station.getMaxCapacity()){
            station.getBina();
        }
    }
}
