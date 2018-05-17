package example.ws.handler;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * MAC - Message Authentication Code
 *
 * Handler intercepts SOAP messages before they enter the network,
 * adding a digest code with SHA-256 algorithm of the body of the message
 * to the header of the SOAP message for outbound messages
 *
 * For inbound messages, a new MAC is generated from the body of the message,
 * and compared with the MAC inside the header of the SOAP message for checking
 * the integrity of the message body.
 *
 */
public class MACHandler implements SOAPHandler<SOAPMessageContext> {
    public static SecretKey sessionKey;

    /** Digest algorithm. */
    private static final String DIGEST_ELEMENT_NAME = "digest";

    /** Message authentication code algorithm. */
    private static final String MAC_ALGO = "HmacSHA256";
    /** Digest algorithm. */
    private static final String DIGEST_ALGO = "SHA-256";

    /** Symmetric cryptography algorithm. */
    private static final String SYM_ALGO = "AES";


    //
    // Handler interface implementation
    //

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
        try{
            if(outbound)
                handleOutboundMessage(smc);
            else
                handleInboundMessage(smc);
        } catch(Exception e){
            e.printStackTrace();
        }


        return true;
    }

    private boolean handleOutboundMessage(SOAPMessageContext smc) throws Exception{

        // get soap envelope
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter sw = new StringWriter();

        // transform the body into a byte[] we can digest
        transformer.transform(new DOMSource(se.getBody()), new StreamResult(sw));
        byte[] bytesToDigest = sw.toString().getBytes();

        // create a digest from the body
        byte[] digest = makeMAC(bytesToDigest, sessionKey);

        String digestString = printBase64Binary(digest);

        // add header
        SOAPHeader sh = se.getHeader();
        if (sh == null)
            sh = se.addHeader();

        // add header element
        Name name = se.createName(DIGEST_ELEMENT_NAME, "ns1", "urn:digest");
        SOAPHeaderElement element = sh.addHeaderElement(name);

        // add header element value
        element.addTextNode(digestString);

        System.out.println("########## Added digest string: " + digestString);

        return true;
    }

    private boolean handleInboundMessage(SOAPMessageContext smc) throws Exception{
        // get soap envelope
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();

        // check header
        SOAPHeader sh = se.getHeader();
        if (sh == null) {
            System.out.println("Header not found.");
            return true;
        }

        Name digestName = se.createName(DIGEST_ELEMENT_NAME);
        sh.getChildElements(digestName);

        Iterator it = sh.getChildElements();;
        // check header element
        if (!it.hasNext()) {
            System.out.printf("Header element %s not found.%n", DIGEST_ELEMENT_NAME);
            return true;
        }

        SOAPElement digestSOAPElement = null;
        while(it.hasNext()){
            SOAPElement element = (SOAPElement) it.next();
            if(element.getLocalName().equals(DIGEST_ELEMENT_NAME)){
                digestSOAPElement = element;
                System.out.println("############# FOUND DIGEST");
                break;
            }
        }

        String digestString = digestSOAPElement.getValue();

        System.out.println("########### Obtained digest string " + digestString);

        return true;

    }




    /** Makes a message authentication code. */
    private static byte[] makeMAC(byte[] bytes, SecretKey key) throws Exception {

        Mac cipher = Mac.getInstance(MAC_ALGO);
        cipher.init(key);
        byte[] cipherDigest = cipher.doFinal(bytes);

        return cipherDigest;
    }

    /**
     * Calculates new digest from text and compare it to the to deciphered
     * digest.
     */
    private static boolean verifyMAC(byte[] cipherDigest, byte[] bytes, SecretKey key) throws Exception {

        Mac cipher = Mac.getInstance(MAC_ALGO);
        cipher.init(key);
        byte[] cipheredBytes = cipher.doFinal(bytes);
        return Arrays.equals(cipherDigest, cipheredBytes);
    }

    private boolean equalByteArrays(byte[] a1, byte[] a2){
        if(a1.length != a2.length){
            System.out.println("diferent length");
            return false;
        }

        for(int i = 0; i < a1.length; i++){
            System.out.println(a1[i] + " == " + a2[i]);
            if(a1[i] != a2[i]){
                return false;
            }
        }
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

}