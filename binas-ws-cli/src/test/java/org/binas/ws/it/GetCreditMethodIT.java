package org.binas.ws.it;

import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.binas.ws.UserView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.fail;

public class GetCreditMethodIT extends BaseIT {
    UserView user;
    String validEmail1 = "valid.email@valid.domain";
    String validEmail2 = "valid1.email@valid.domain";
    String invalidEmail1 = ".valid.email@valid.domain";

    @Before
    public void setUp() {
        client.testClear();
        try {
            user = client.activateUser(validEmail1);
        } catch (EmailExists_Exception | InvalidEmail_Exception e) {
            //TODO SOMETHING HERE
        }

    }

    @Test
    public void userExist() {
        try {
            client.getCredit(validEmail1);
        } catch (UserNotExists_Exception e) {
            fail();
        }
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExistInvalidEmail() throws UserNotExists_Exception {
        client.getCredit(invalidEmail1);
    }

    @Test(expected = UserNotExists_Exception.class)
    public void userNotExistNotActivated() throws UserNotExists_Exception {
        client.getCredit(validEmail2);
    }


    @After
    public void tearDown(){
        client.testClear();
    }
}
