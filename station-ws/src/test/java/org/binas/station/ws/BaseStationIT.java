package org.binas.station.ws;

import org.binas.station.domain.Station;
import org.junit.After;
import org.junit.Before;

public class BaseStationIT {

    @Before
    public void setUp() throws Exception{
        Station.getInstance().reset();
    }

    @After
    public void tearDown() throws Exception{
        Station.getInstance().reset();
    }
}
