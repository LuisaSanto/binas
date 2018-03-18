package org.binas.station.ws;

import org.binas.station.domain.Station;
import org.binas.station.domain.exception.NoSlotAvailException;
import org.junit.Assert;
import org.junit.Test;

public class StationReturnBinaMethodTest extends BaseStationIT {


    @Test(expected = NoSlotAvailException.class)
    public void returnBina(){
        Station station = Station.getInstance();

        Assert.assertEquals(0,station.getFreeDocks());

        station.returnBina();
    }


}
