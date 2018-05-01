package org.binas.station.ws.it;

import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.TaggedBalance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.ws.Response;

import java.util.concurrent.ExecutionException;

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
        client.setBalanceAsync(userEmail, taggedBalance);


        try{
            Response<GetBalanceResponse> responseTaggedBalance = client.getBalanceAsync(userEmail);
            int value = 0;
            value = responseTaggedBalance.get().getUserView().getValue();
            Assert.assertEquals(value, value);

        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void nullTaggedBalance() throws ExecutionException, InterruptedException {
        TaggedBalance taggedBalance = null;
        client.setBalance(userEmail, taggedBalance);

        Response<SetBalanceResponse> response = client.setBalanceAsync(userEmail, taggedBalance);
        Assert.assertNull(response.get().getAck());
    }

    @Test
    public void nullEmail() throws ExecutionException, InterruptedException {
        Response<SetBalanceResponse> response = client.setBalanceAsync(null, taggedBalance);
        Assert.assertNull(response.get().getAck());
    }

    @Test
    public void emptyEmail() throws ExecutionException, InterruptedException {
        Response<SetBalanceResponse> response = client.setBalanceAsync("", taggedBalance);
        Assert.assertNull(response.get().getAck());
    }


}
