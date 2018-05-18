package org.binas.ws.it;


import com.sun.xml.ws.fault.ServerSOAPFaultException;
import org.binas.ws.EmailExists_Exception;
import org.binas.ws.InvalidEmail_Exception;
import org.binas.ws.UserNotExists_Exception;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/*
 * Class tests if the Binas access control is rejecting request where the user email is not consistent
 */
public class BinasAuthorizationIT extends BaseIT  {

    @Before
    public void setUp() {
        client.testClear();
    }

    @Test
    public void successActivateUser() throws EmailExists_Exception, InvalidEmail_Exception{
        client.activateUser("alice@A58.binas.org");
    }
/*
    @Test
    public void setCreditOfMe() throws UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception{
        client.activateUser("alice@A58.binas.org");
        client.setCredit("alice@A58.binas.org", 5);
    }

    @Test(expected = ServerSOAPFaultException.class)
    public void setCreditOfAnotherUser() throws UserNotExists_Exception, EmailExists_Exception, InvalidEmail_Exception{
        client.activateUser("alice@A58.binas.org");
        client.activateUser("bob@A58.binas.org");
        client.setCredit("bob@A58.binas.org", 5);
    }

    @After
    public void tearDown() {
        client.testClear();
    }
    */
}
