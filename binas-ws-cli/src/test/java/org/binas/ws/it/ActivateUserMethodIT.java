package org.binas.ws.it;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class ActivateUserMethodIT extends BaseIT{

    @Before
    public void setUp() {
        client.testClear();
    }

    @Test
    public void success(){
        String validEmail = "valid.email@valid.domain";

        try{
            client.activateUser(validEmail);
        } catch(EmailExists_Exception e){
            fail();
        } catch(InvalidEmail_Exception e){
            fail();
        }
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailNotEmpty() throws InvalidEmail_Exception{
        String validEmail = ".invalid.emai@l@invalid";

        try{
            client.activateUser(validEmail);
            fail();
        } catch(EmailExists_Exception e){
            fail();
        }
    }

    @Test(expected = InvalidEmail_Exception.class)
    public void invalidEmailEmpty() throws InvalidEmail_Exception{
        String validEmail = "@invalid";

        try{
            client.activateUser(validEmail);
            fail();
        } catch(EmailExists_Exception e){
            fail();
        }
    }

    @Test(expected = EmailExists_Exception.class)
    public void emailExists() throws EmailExists_Exception{
        String validEmail = "valid.email@valid.domain";

        try{
            client.activateUser(validEmail);
            client.activateUser(validEmail);
            fail();
        } catch(InvalidEmail_Exception e){
            fail();
        }
    }

    @After
    public void tearDown(){
        client.testClear();
    }
}
