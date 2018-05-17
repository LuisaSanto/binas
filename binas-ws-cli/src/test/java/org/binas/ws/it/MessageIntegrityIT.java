package org.binas.ws.it;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import example.ws.handler.MACHandler;
import org.junit.After;
import org.junit.Test;

public class MessageIntegrityIT extends BaseIT {
    public final String VALID_SERVER_NAME = "binas@A58.binas.org";
    public final String VALID_CLIENT_NAME = "alice@A58.binas.org";
    public final String VALID_CLIENT_PASSWORD = "r6p67xdOV";

    @Test
    public void success(){
        client.testPing("Cucumber");
        client.testPing("The Cake is a Lie");
    }

    @Test(expected = ServerSOAPFaultException.class)
    public void failIntegrity(){
        MACHandler.disableIntegrityChecks();
        client.testPing("Potato");
    }

    @After
    public void tearDown() throws Exception{
        MACHandler.enableIntegrityChecks();
    }
}
