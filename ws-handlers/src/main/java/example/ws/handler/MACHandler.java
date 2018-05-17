package example.ws.handler;


import javax.crypto.Mac;
import javax.crypto.SecretKey;
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
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
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
    private static boolean ARE_INTEGRITY_CHECKS_DISABLED = false;

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
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch(SOAPException e){
            e.printStackTrace();
        } catch(InvalidKeyException e){
            e.printStackTrace();
        } catch(TransformerException e){
            e.printStackTrace();
        }


        return true;
    }

    private byte[] generateBodyDigestFromSOAPBody(SOAPBody soapBody) {

        try{
            Transformer transformer = TransformerFactory.newInstance().newTransformer();

            StringWriter sw = new StringWriter();

            // transform the body into a byte[] we can digest
            transformer.transform(new DOMSource(soapBody), new StreamResult(sw));
            byte[] bytesToDigest = sw.toString().getBytes();

            return makeMAC(bytesToDigest, sessionKey);
        } catch(TransformerConfigurationException e){
            e.printStackTrace();
        } catch(TransformerException e){
            e.printStackTrace();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch(InvalidKeyException e){
            e.printStackTrace();
        }
        return null;
    }

    private boolean handleOutboundMessage(SOAPMessageContext smc) throws TransformerException, SOAPException, InvalidKeyException, NoSuchAlgorithmException{

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

        //System.out.println("########## Added digest string: " + digestString);

        // in case we want to test integrity and make it fail, force text content of the message to be garbage
        if(ARE_INTEGRITY_CHECKS_DISABLED){
            setSOAPMessageBodyTextContent(smc, "Hot Singles in your Area!");
        }

        return true;
    }

    private boolean handleInboundMessage(SOAPMessageContext smc) throws SOAPException{
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

        // find the digest header element
        SOAPElement digestSOAPElement = null;
        while(it.hasNext()){
            SOAPElement element = (SOAPElement) it.next();
            if(element.getLocalName().equals(DIGEST_ELEMENT_NAME)){
                digestSOAPElement = element;
               // System.out.println("############# FOUND DIGEST");
                break;
            }
        }

        // convert digest into byte[]
        String digestString = digestSOAPElement.getValue();
        byte[] digest = parseBase64Binary(digestString);
        //System.out.println("########### Obtained digest string " + digestString);

        // generate a new digest from the body of the SOAP message
        byte[] newDigest = generateBodyDigestFromSOAPBody(se.getBody());

        // check if digests are equal to verify integrity of the message
        if(!Arrays.equals(digest, newDigest)){
            throw new RuntimeException("Detected SOAP Message modifications, integrity of message was not verified");
        }

        return true;

    }

    /** Makes a message authentication code. */
    private static byte[] makeMAC(byte[] bytes, SecretKey key) throws NoSuchAlgorithmException, InvalidKeyException{

        Mac cipher = Mac.getInstance(MAC_ALGO);
        cipher.init(key);
        byte[] cipherDigest = cipher.doFinal(bytes);

        return cipherDigest;
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

    /**
     * Change the content of the SOAP message body, primarily used to demonstrate integrity check
     */
    public void setSOAPMessageBodyTextContent(SOAPMessageContext smc, String text) throws SOAPException{
        // get soap envelope
        SOAPMessage msg = smc.getMessage();
        SOAPPart sp = msg.getSOAPPart();
        SOAPEnvelope se = sp.getEnvelope();
        SOAPBody sb = se.getBody();

        Name extraName = se.createName("extra", "ns1", "urn:extra");
        SOAPBodyElement extraElement = sb.addBodyElement(extraName);
        extraElement.addTextNode(text);
    }

    public static void disableIntegrityChecks(){
        ARE_INTEGRITY_CHECKS_DISABLED = true;
    }

    public static void enableIntegrityChecks(){
        ARE_INTEGRITY_CHECKS_DISABLED = false;
    }

}