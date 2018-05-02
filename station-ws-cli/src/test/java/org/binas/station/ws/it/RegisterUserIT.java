package org.binas.station.ws.it;

import org.binas.station.ws.RegisterUserResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.ws.Response;

/**
 * Basic test to test if a user is registered properly in a station
 * Email validation is done in Binas, before StationClient, therefore no email verification needed on the stations
 */
public class RegisterUserIT extends BaseIT {
    final String userEmail = "someone@email";

    @Test
    public void success(){
        Response<RegisterUserResponse> response = client.registerUserAsync(userEmail);
        pollResponse(response);
        Assert.assertNotNull(response);
    }

    @After
    public void tearDown() throws Exception{
        client.testClear();
    }
}
