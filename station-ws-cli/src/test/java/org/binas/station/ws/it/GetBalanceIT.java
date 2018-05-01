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

public class GetBalanceIT extends BaseIT{
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
            Response<GetBalanceResponse> responseTaggedBalance = client.getBalanceAsync(userEmail);
            int value = 0;
            value = responseTaggedBalance.get().getUserView().getValue();
            Assert.assertEquals(value, value);

        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test
    public void nullEmail(){
        Response<GetBalanceResponse> response = client.getBalanceAsync(null);
        Assert.assertNull(response);
    }

    @Test
    public void emptyEmail(){
        Response<GetBalanceResponse> response = client.getBalanceAsync("");
        Assert.assertNull(response);
    }


}
