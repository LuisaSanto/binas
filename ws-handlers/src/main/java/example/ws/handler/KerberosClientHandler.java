package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.*;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 *  This SOAP handler intecerpts the remote calls done by binas-ws-cli for authentication,
 *  and creates a KerbyClient to authenticate with the kerby server in RNL
 */
public class KerberosClientHandler implements SOAPHandler<SOAPMessageContext> {
    public static final String KERBY_WS_URL = "http://sec.sd.rnl.tecnico.ulisboa.pt:8888/kerby";
    private static final String VALID_CLIENT_NAME = "alice@A58.binas.org";
    private static final String VALID_CLIENT_PASSWORD = "r6p67xdOV";
    private static SecureRandom randomGenerator = new SecureRandom();
    private static final int VALID_DURATION = 30;

    private static final String VALID_SERVER_NAME = "binas@A58.binas.org";

    private static CipheredView ticket;
    private static CipheredView auth;


    /**
     * Gets the header blocks that can be processed by this Handler instance. If
     * null, processes all.
     */
    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    /**
     * The handleMessage method is invoked for normal processing of inbound and
     * outbound messages.
     */
    @Override
    public boolean handleMessage(SOAPMessageContext smc) {
        Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if(outbound){

            // garantir que esta autenticado ao kerby e o limite de validade do ticket nao foi ultrapassado
            if(!isAuthenticatedToKerby()){
                authenticateWithKerby();
            }
            return handleOutboundMessage(smc);
        }else{
            return handleInboundMessage(smc);
        }

    }


    // TODO MAC HANDLER ??? separate class file, gets called in the chain after authentication!! not here


    /** Handles outbound messages */
    private boolean handleOutboundMessage(SOAPMessageContext smc){
        addTicketAndAuthToMessage(smc);


        return true;
    }

    private void addTicketAndAuthToMessage(SOAPMessageContext smc){
        try{
            // get soap envelope
            SOAPMessage msg = smc.getMessage();
            SOAPPart sp = msg.getSOAPPart();
            SOAPEnvelope se = sp.getEnvelope();

            // add header if there is none ( se.getHeader() is null if the header doesn't exist )
            SOAPHeader sh = se.getHeader();
            if(sh == null){
                sh = se.addHeader();
            }

            Name ticketName = se.createName("ticket" );
            SOAPHeaderElement element = sh.addHeaderElement(ticketName);

            CipherClerk clerk = new CipherClerk();

            // add ticket
            element.addTextNode(clerk.cipherToString(ticket));

            // add auth
            element.addTextNode(clerk.cipherToString(auth));

        } catch(SOAPException e){
            e.printStackTrace();
        }
    }

    /** Authenticate with Kerby */
    private void authenticateWithKerby(){
        try{
            KerbyClient kerbyClient = new KerbyClient(KERBY_WS_URL);

            // nounce to prevent replay attacks
            long nounce = randomGenerator.nextLong();

            // 1. authenticate user and get ticket and session key by requesting a ticket to kerby
            SessionKeyAndTicketView sessionKeyAndTicketView = kerbyClient.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);

            // 2. generate a key from alice's password, to decipher and retrieve the session key with the Kc (client key)
            Key aliceKey = SecurityHelper.generateKeyFromPassword(VALID_CLIENT_PASSWORD);

            // NOTE: SessionKey : {Kc.s , n}Kc
            // to get the actual session key, we call getKeyXY
            Key sessionKey = new SessionKey(sessionKeyAndTicketView.getSessionKey(), aliceKey).getKeyXY();

            // 3. save ticket for server
            ticket = sessionKeyAndTicketView.getTicket();

            // 4. create authenticator (Auth)
            Auth authToBeCiphered = new Auth(VALID_CLIENT_NAME, new Date());
            // cipher the auth with the session key Kcs
            auth = authToBeCiphered.cipher(sessionKey);

            // TODO contexto resposta do KerberosServerHandler

        } catch(KerbyClientException e){
            e.printStackTrace();
        } catch(BadTicketRequest_Exception e){
            e.printStackTrace();
        } catch(KerbyException e){
            e.printStackTrace();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch(InvalidKeySpecException e){
            e.printStackTrace();
        }

    }

    /** Handles inbound messages received from KerberosServerHandler */
    private boolean handleInboundMessage(SOAPMessageContext smc){


        return true;
    }


    /** The handleFault method is invoked for fault message processing. */
    @Override
    public boolean handleFault(SOAPMessageContext smc) {
        System.out.println("Ignoring fault message...");
        return true;
    }

    /**
     * Called at the conclusion of a message exchange pattern just prior to the
     * JAX-WS runtime dispatching a message, fault or exception.
     */
    @Override
    public void close(MessageContext messageContext) {
        // nothing to clean up
    }

    /** verifica se cliente está autenticado */
    private boolean isAuthenticatedToKerby(){
        // TODO verificar limite validade ticket tbm
        return ticket != null;
    }

}