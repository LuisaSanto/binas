package org.binas.station.ws.it;

import org.binas.station.ws.TaggedBalance;
import org.binas.station.ws.UserNotExist_Exception;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class SetBalanceIT extends BaseIT{
    final String userEmail = "someone@email";
    final TaggedBalance taggedBalance = new TaggedBalance();
    final int value = 2;

    @Before
    public void setUp() throws Exception{
        taggedBalance.setTag(1);
        taggedBalance.setValue(value);
    }

    @Test
    public void success(){
        client.setBalance(userEmail, taggedBalance);


        try{
            TaggedBalance responseTaggedBalance = client.getBalance(userEmail);
            Assert.assertEquals(value, responseTaggedBalance.getValue());

        } catch(UserNotExist_Exception e){
            fail();
        }
    }

    @Test
    public void nullTaggedBalance(){
        TaggedBalance taggedBalance = null;

        //String response = client.setBalance(userEmail, taggedBalance);
        //Assert.assertNull(response);
    }

    @Test
    public void nullEmail(){
        //String response = client.setBalance(null, taggedBalance);
        //Assert.assertNull(response);
    }

    @Test
    public void emptyEmail(){
        //String response = client.setBalance("", taggedBalance);
        //Assert.assertNull(response);
    }


}
