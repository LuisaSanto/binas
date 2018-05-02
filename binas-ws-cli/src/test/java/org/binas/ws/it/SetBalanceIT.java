package org.binas.ws.it;

import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;



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
            client.setCredit(userEmail, 2);
            int credit = client.getCredit(userEmail);
            Assert.assertEquals(2, credit);

        } catch(UserNotExists_Exception e){
            e.printStackTrace();
        }
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExists1() throws UserNotExists_Exception{
        client.setCredit(null, value);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExists2() throws UserNotExists_Exception{
        client.setCredit("", value);
    }


    @After
    public void tearDown() throws Exception{
        client.testClear();
    }
}
