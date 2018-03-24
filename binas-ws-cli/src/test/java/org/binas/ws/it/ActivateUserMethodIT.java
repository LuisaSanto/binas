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
    public void validEmail(){
        String validEmail = "valid.email@valid.domain";

        try{
            client.activateUser(validEmail);
        } catch(EmailExists_Exception e){
            fail();
        } catch(InvalidEmail_Exception e){
            fail();
        }
    }

    @Test
    public void invalidEmail(){
        String validEmail = ".invalid.emai@l@invalid";

        try{
            client.activateUser(validEmail);
            fail();
        } catch(EmailExists_Exception e){
            fail();
        } catch(InvalidEmail_Exception e){

        }
    }

    @Test
    public void emailExists(){
        String validEmail = "valid.email@valid.domain";

        try{
            client.activateUser(validEmail);
            client.activateUser(validEmail);
            fail();
        } catch(EmailExists_Exception e){

        } catch(InvalidEmail_Exception e){
            fail();
        }
    }

    @After
    public void tearDown(){
        client.testClear();
    }
}
