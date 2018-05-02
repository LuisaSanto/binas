package org.binas.ws.it;

import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.TaggedBalance;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.ws.Response;

import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.fail;

public class SetBalanceIT extends BaseIT{
    final String userEmail = "someone@email";
    final int value = 2;

    @Before
    public void setUp() throws Exception{
        client.activateUser(userEmail);
    }


    @Test
    public void success(){

        try{
            int credit = client.getCredit(userEmail);

        } catch(UserNotExists_Exception e){
            e.printStackTrace();
        }
    }


    @Test(expected = ExecutionException.class)
    public void userNotExists() {
        try{
            client.setCredit(userEmail, value);
            fail();
        } catch(UserNotExists_Exception e){
            e.printStackTrace();
        }


    }


    @After
    public void tearDown() throws Exception{
        client.testClear();
    }
}
