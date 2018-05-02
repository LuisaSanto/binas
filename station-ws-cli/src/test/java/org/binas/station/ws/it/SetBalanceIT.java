package org.binas.station.ws.it;

import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
import org.binas.station.ws.TaggedBalance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.text.html.HTML;
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

        client.registerUser(userEmail);
    }


    @Test
    public void success(){
        client.setBalanceAsync(userEmail, taggedBalance);

        try{
            Response<GetBalanceResponse> responseTaggedBalance = client.getBalanceAsync(userEmail);
            Assert.assertEquals(taggedBalance.getValue(), responseTaggedBalance.get().getTaggedBalance().getValue());

        } catch (InterruptedException | ExecutionException e) {
            fail();
        }
    }


    @Test(expected = ExecutionException.class)
    public void userNotExists() throws ExecutionException, InterruptedException {
        TaggedBalance taggedBalance = new TaggedBalance();
        taggedBalance.setValue(0);
        taggedBalance.setTag(0);

        Response<SetBalanceResponse> response = client.setBalanceAsync(null, taggedBalance);

        // ask 10 times, if not done by then fail
        pollResponse(response);

        // trigger the UserNotExists_Exception received in the SOAP as ExecutionException
        response.get();
    }


    @After
    public void tearDown() throws Exception{
        client.testClear();
    }
}
