package handlers;

import org.binas.ws.cli.BinasClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import pt.ulisboa.tecnico.sdis.kerby.CipherClerk;
import pt.ulisboa.tecnico.sdis.kerby.CipheredView;
import pt.ulisboa.tecnico.sdis.kerby.KerbyException;
import pt.ulisboa.tecnico.sdis.kerby.SecurityHelper;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.printHexBinary;

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
public class MACClientHandler implements SOAPHandler<SOAPMessageContext> {
    /** Digest algorithm. */
    private static final String DIGEST_ALGO = "SHA-256";
    private static final String DIGEST_ELEMENT_NAME = "digest";

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

        CipherClerk clerk = new CipherClerk();

        if(outbound){
            try{
                // get soap envelope
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();

                SOAPHeader sh = se.getHeader();

                // generate a digest of the body of the SOAP message
                String bodyToDigest = msg.getSOAPBody().getValue();
                String bodyDigest = generateDigest(bodyToDigest);

                // cipher the digest with the session key
                CipheredView cipheredDigest = SecurityHelper.cipher(String.class, bodyDigest, BinasClient.kcsSessionKey);

                // Add digest to SOAP header
                Node digestNode = clerk.cipherToXMLNode(cipheredDigest, DIGEST_ELEMENT_NAME);

                Name digestName = se.createName(DIGEST_ELEMENT_NAME, "ns1" ,"urn:ticket");
                SOAPHeaderElement element = sh.addHeaderElement(digestName);

                // serializar e o digestNode e adicionar ao cabeçalho da mensagem SOAP
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                StringWriter sw = new StringWriter();
                transformer.transform(new DOMSource(digestNode), new StreamResult(sw));
                element.addTextNode(sw.toString());

            } catch(SOAPException e){
                e.printStackTrace();
            } catch(KerbyException e){
                e.printStackTrace();
            } catch(TransformerException e){
                e.printStackTrace();
            } catch(JAXBException e){
                e.printStackTrace();
            }


        }else{

            try{
                // get SOAP envelope header
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPHeader sh = null;
                sh = se.getHeader();

                // check header
                if (sh == null) {
                    System.out.println("Header not found.");
                    return true;
                }
                Name digestName = se.createName(DIGEST_ELEMENT_NAME);

                Iterator it = sh.getChildElements();
                // check header element
                if (!it.hasNext()) {
                    System.out.printf("Header element %s not found.%n", DIGEST_ELEMENT_NAME);
                    return true;
                }

                // Obter o CipheredView correspondente ao digest no cabeçalho da mensagem
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                SOAPElement ticketSOAPElement = (SOAPElement) it.next();
                Document ticketDocument = builder.parse(new InputSource(new StringReader(ticketSOAPElement.getValue())));

                DOMSource ticketDOMSource = new DOMSource(ticketDocument);
                Node ticketNode = ticketDOMSource.getNode();

                CipheredView cipheredDigest = clerk.cipherFromXMLNode(ticketNode);

                // decifrar o digest com chave de sessão
                String digest = SecurityHelper.decipher(String.class, cipheredDigest, BinasClient.kcsSessionKey);

                // gerar um novo digest a partir do corpo da mensagem SOAP
                String bodyToDigest = msg.getSOAPBody().getValue();
                String bodyDigest = generateDigest(bodyToDigest);

                // comparar os dois digest, se forem diferentes a integridade não foi mantida
                boolean digestsAreEqual = Arrays.equals(digest.getBytes(), bodyDigest.getBytes());

                if(!digestsAreEqual){
                    throw new RuntimeException("MAC digest not equal, failed integrity");
                }
            } catch(SOAPException e){
                e.printStackTrace();
            } catch(ParserConfigurationException e){
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch(JAXBException e){
                e.printStackTrace();
            } catch(SAXException e){
                e.printStackTrace();
            } catch(KerbyException e){
                e.printStackTrace();
            }


        }

        return true;
    }


    public static String generateDigest(String message){
        // get a message digest object using the specified algorithm
        MessageDigest messageDigest = null;
        try{
            messageDigest = MessageDigest.getInstance(DIGEST_ALGO);
            messageDigest.update(message.getBytes());

            return printHexBinary(messageDigest.digest()); // printHexBinary converts byte[] to String
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
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