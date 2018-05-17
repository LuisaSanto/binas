package handlers;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * This SOAPHandler shows how to set/get values from headers in inbound/outbound
 * SOAP messages.
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */
public class BinasAuthorizationHandler implements SOAPHandler<SOAPMessageContext> {
    public static String userEmailInTicket;
    public static String userEmailInAuth;

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

        if(!userEmailInAuth.equals(userEmailInTicket)){
            throw new RuntimeException("User email in ticket differs from user email in the Authenticator. Request Denied");
        }

        if(!outbound){
            try{
                // obter body da mensagem
                SOAPMessage msg = smc.getMessage();
                SOAPPart sp = msg.getSOAPPart();
                SOAPEnvelope se = sp.getEnvelope();
                SOAPBody sb = se.getBody();

                // procurar no body o email do utilizador recursivamente
                String emailInBody = findValueOfTag(sb, "email");

                // verificar se o email coincide com o email do ticket ( o do auth é comparado com ticket previamente )
                if(!emailInBody.isEmpty() && !emailInBody.equals(userEmailInTicket) ){
                    throw new RuntimeException("User email in ticket differs from user email specified in the body of the SOAP Message. Request Denied");
                }

                return true;
            } catch(SOAPException e){
                e.printStackTrace();
            }

        }
        return true;
    }

    /** Procura no nodo SOAP e nos seus filhos pelo campo com a tag pretendida e retorna o valor */
    private String findValueOfTag(Node node, String tag){
        StringBuilder email = new StringBuilder();

        if(node.getLocalName().equals(tag)){
            return node.getTextContent();
        }

        if(node.hasChildNodes()){
            NodeList nodeList = node.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); i++){
                email.append(findValueOfTag(nodeList.item(i), tag));
                return email.toString();
            }
        }
        return "";
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