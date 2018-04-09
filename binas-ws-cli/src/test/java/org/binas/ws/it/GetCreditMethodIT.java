package org.binas.ws.it;

import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class GetCreditMethodIT extends BaseIT {

    @Before
    public void setUp() {
        client.testClear();
    }

    @Test
    public void validEmail(){
        String validEmail = "valid.email@valid.domain";

        try{
            client.getCredit(validEmail);
        } catch(UserNotExists_Exception e){
            fail();
        }
    }

    @Test
    public void invalidEmail(){
        String invalidEmail = ".invalid.emai@l@invalid";

        try{
            client.getCredit(invalidEmail);
        } catch(UserNotExists_Exception e) {
            fail();
        }
    }

    @After
    public void tearDown(){
        client.testClear();
    }
}
