package org.binas.station.ws.it;

import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.TaggedBalance;
import org.binas.station.ws.UserNotExist_Exception;
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
            Assert.assertEquals(taggedBalance.getValue(), responseTaggedBalance.get().getTaggedBalance().getValue());

        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }

    @Test(expected = ExecutionException.class)
    public void userNotExists() throws ExecutionException, InterruptedException{
        Response<GetBalanceResponse> response = client.getBalanceAsync(null);

        // ask 10 times, if not done by then fail
        pollResponse(response);

        // trigger the UserNotExists_Exception received in the SOAP as ExecutionException
        response.get();
    }




}
