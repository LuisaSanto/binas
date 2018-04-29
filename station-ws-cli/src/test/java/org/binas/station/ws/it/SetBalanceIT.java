package org.binas.station.ws.it;

import org.binas.station.ws.TaggedBalance;
import org.binas.station.ws.UserNotExist_Exception;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class SetBalanceIT extends BaseIT{
    final String userEmail = "someone@email";

    final int value = 2;

    @Test
    public void success(){
        TaggedBalance taggedBalance = new TaggedBalance();
        taggedBalance.setTag(1);
        taggedBalance.setValue(value);

        String response = client.setBalance(userEmail, taggedBalance);
        Assert.assertEquals("ack", response);

        try{
            TaggedBalance responseTaggedBalance = client.getBalance(userEmail);
            Assert.assertEquals(value, responseTaggedBalance.getValue());

        } catch(UserNotExist_Exception e){
            fail();
        }
    }

}
