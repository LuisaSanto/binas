package org.binas.ws.it;

import org.binas.station.ws.TaggedBalance;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import static junit.framework.TestCase.fail;

public class GetBalanceIT extends BaseIT{
    final String userEmail = "someone@email";
    final TaggedBalance taggedBalance = new TaggedBalance();
    final int value = 2;

    @Before
    public void setUp() throws Exception{
        client.activateUser(userEmail);
    }

    @Test
    public void success(){
        try{
            client.setCredit(userEmail, value);
            int credit = client.getCredit(userEmail);
            Assert.assertEquals(value, credit);
        } catch(UserNotExists_Exception e){
            fail();
            e.printStackTrace();
        }
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExists() throws UserNotExists_Exception{
        client.getCredit(null);
        client.getCredit("");

    }

    @After
    public void tearDown() throws Exception{
        client.testClear();
    }
}
