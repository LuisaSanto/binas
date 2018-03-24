package org.binas.station.ws.it;

import org.binas.station.ws.BadInit_Exception;
import org.binas.station.ws.NoBinaAvail_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class StationGetBinaMethodTestIT extends BaseIT {

    @After
    public void tearDown(){
        client.testClear();
    }

    @Test
    public void getBinaNotEnoughBinas(){
        try{
            client.testInit(5,5, 10, 0);
            getBinas(50);
            fail();
        } catch(BadInit_Exception e){

        } catch(NoBinaAvail_Exception nbee){

        }
    }

    @Test
    public void getBinaMoreEnoughBinas(){
        try{
            client.testInit(5,5, 100, 0);
            getBinas(20);
        } catch(BadInit_Exception e){

        } catch(NoBinaAvail_Exception nbee){
            fail();
        }
    }

    @Test
    public void getBinaEnoughBinas(){
        try{
            client.testInit(5,5, 20, 0);
            getBinas(20);
        } catch(BadInit_Exception e){

        } catch(NoBinaAvail_Exception nbee){

        }
    }

   private void getBinas(int times) throws NoBinaAvail_Exception{
       for(int i = 0; i < times; i++){
           client.getBina();
       }

   }
}
