package example.ws.handler;

import org.binas.ws.cli.BinasClient;
import org.w3c.dom.Node;
import pt.ulisboa.tecnico.sdis.kerby.*;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;

import java.io.StringWriter;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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

    public static final String TICKET_ELEMENT_NAME = "ticket";
    public static final String AUTH_ELEMENT_NAME = "auth";


    private static final String VALID_SERVER_NAME = "binas@A58.binas.org";

    private static CipheredView ticket;
    private static CipheredView auth;
    private static Key kcsSessionKey;


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
            if(!isTicketValid()){
                requestNewTicketAndSessionKey();
            }

            initCipheredTicketAndAuth();
            return handleOutboundMessage(smc);
        }else{
            // TODO adicionar o novo ticket ao TicketCollection do binasclient aqui

            return handleInboundMessage(smc);
        }

    }

    /**
     * retira um SessionKeyAndTicketView do binasClient ticket collection
     * e obtem o CipheredView correspondente ao ticket, a key de sessao
     * e cria um novo auth
     */
    private void initCipheredTicketAndAuth(){
        SessionKeyAndTicketView sessionKeyAndTicketView = BinasClient.ticketCollection.getTicket(BinasClient.VALID_SERVER_NAME);
        ticket = sessionKeyAndTicketView.getTicket();

        try{
            Key clientKey = SecurityHelper.generateKeyFromPassword(BinasClient.VALID_CLIENT_PASSWORD);
            kcsSessionKey = new SessionKey(sessionKeyAndTicketView.getSessionKey(), clientKey).getKeyXY();
            auth = new Auth(BinasClient.VALID_CLIENT_NAME, new Date()).cipher(kcsSessionKey);

        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch(InvalidKeySpecException e){
            e.printStackTrace();
        } catch(KerbyException e){
            e.printStackTrace();
        }
    }

    /** Request new ticket and session key from Kerby */
    private void requestNewTicketAndSessionKey(){
        try{
            KerbyClient kerbyClient = new KerbyClient(KERBY_WS_URL);

            // nounce to prevent replay attacks
            long nounce = randomGenerator.nextLong();

            // 1. authenticate user and get ticket and session key by requesting a ticket to kerby
            SessionKeyAndTicketView sessionKeyAndTicketView = kerbyClient.requestTicket(VALID_CLIENT_NAME, VALID_SERVER_NAME, nounce, VALID_DURATION);

            // 2. generate a key from alice's password, to decipher and retrieve the session key with the Kc (client key)
            Key aliceKey = SecurityHelper.generateKeyFromPassword(BinasClient.VALID_CLIENT_PASSWORD);

            // NOTE: SessionKey : {Kc.s , n}Kc
            // to get the actual session key, we call getKeyXY
            kcsSessionKey = new SessionKey(sessionKeyAndTicketView.getSessionKey(), aliceKey).getKeyXY();

            // 3. save ticket for server
            ticket = sessionKeyAndTicketView.getTicket();

            // 4. create authenticator (Auth)
            Auth authToBeCiphered = new Auth(VALID_CLIENT_NAME, new Date());
            // cipher the auth with the session key Kcs
            auth = authToBeCiphered.cipher(kcsSessionKey);

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
    // TODO MAC HANDLER ??? separate class file, gets called in the chain after authentication!! not here


    /** Handles outbound messages */
    private boolean handleOutboundMessage(SOAPMessageContext smc){
        addTicketAndAuthToMessage(smc);

        return true;
    }

    private void addTicketAndAuthToMessage(SOAPMessageContext smc){
        CipherClerk clerk = new CipherClerk();
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

            // ----------------- TICKET ----------------------

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter sw = new StringWriter();

            // criar node XML
            Node ticketNode = clerk.cipherToXMLNode(ticket, TICKET_ELEMENT_NAME);

            Name ticketName = se.createName(TICKET_ELEMENT_NAME, "ns1" ,"urn:ticket");
            SOAPHeaderElement element = sh.addHeaderElement(ticketName);

            // serializar e o ticketNode á mensagem SOAP
            transformer.transform(new DOMSource(ticketNode), new StreamResult(sw));
            element.addTextNode(sw.toString());

            // -----------------  AUTH  ----------------------

            // criar node XML
            Node authNode = clerk.cipherToXMLNode(auth, AUTH_ELEMENT_NAME);

            Name authName = se.createName(AUTH_ELEMENT_NAME, "ns1" ,"urn:auth");
            SOAPHeaderElement element2 = sh.addHeaderElement(authName);

            // serializar o authNode
            sw = new StringWriter();
            transformer.transform(new DOMSource(authNode), new StreamResult(sw));
            element2.addTextNode(sw.toString());

        } catch(SOAPException e){
            e.printStackTrace();
        } catch(JAXBException e){
            e.printStackTrace();
        } catch(TransformerConfigurationException e){
            e.printStackTrace();
        } catch(TransformerException e){
            e.printStackTrace();
        } catch(Exception e){
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

    /** verifica se cliente está autenticado verificando se existe um ticket valido */
    private boolean isTicketValid(){
        return BinasClient.ticketCollection.getTicket(BinasClient.VALID_SERVER_NAME) != null;
    }

    /** SOAP to DOM and DOM to SOAP methods */


}