package example.ws.handler;

import pt.ulisboa.tecnico.sdis.kerby.*;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClient;
import pt.ulisboa.tecnico.sdis.kerby.cli.KerbyClientException;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import static example.ws.handler.KerberosClientHandler.authenticatorForServer;
import static example.ws.handler.KerberosClientHandler.mensagemCifradaEnviada;
import static example.ws.handler.KerberosClientHandler.ticketForServer;

/**
 *  This SOAP handler intecerpts the remote calls done by binas-ws-cli for authentication,
 *  and creates a KerbyClient to authenticate with the kerby server in RNL
 */
public class KerberosServerHandler implements SOAPHandler<SOAPMessageContext> {
    public static final String VALID_SERVER_PASSWORD = "nhdchdps";
    public static final String TICKET_PROPERTY = "ticketProperty";
    public static final String AUTH_PROPERTY = "authProperty";


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

        if(outbound)
            handleOutboundMessage(smc);
        else
            handleInboundMessage(smc);

        return true;
    }



    /** Handles outbound messages */
    private boolean handleOutboundMessage(SOAPMessageContext smc){



        return true;
    }

    /** Handles inbound messages */
    private boolean handleInboundMessage(SOAPMessageContext smc){
        // TODO BinasAuthorizationHandler


        CipheredView ticketFromKerberosClientHandler = (CipheredView) smc.get(TICKET_PROPERTY);
        CipheredView authenticatorFromKerberosClientHandler = (CipheredView) smc.get(AUTH_PROPERTY);


        // TODO should this validation be on the actual Server? Binas?  send to binas and do validation there
        try{
            // 1. O servidor abre o ticket com a sua chave (Ks) e deve validá-lo.
            Key serverKey = SecurityHelper.generateKeyFromPassword(VALID_SERVER_PASSWORD);
            Ticket ticket = new Ticket(ticketFromKerberosClientHandler, serverKey);

            ticket.validate();
            Key sessionKey = ticket.getKeyXY();

            // Authxy = {x, Treq}Kxy
            // so: Authcs = {c, Treq}Kcs  - ciphered with the session key between client and server

            // 2. Depois deve abrir o autenticador com a chave de sessão (Kcs) e validá-lo.
            Auth auth = new Auth(authenticatorFromKerberosClientHandler, sessionKey);

            // 3 TODO verificar integridade atraves MACHandler


            // TODO validar request time
            // 4. O servidor responde ao cliente com uma instância da classe RequestTime (da kerby-lib).
            // responde com {Treq}Kc,s   response
            RequestTime requestTime = new RequestTime(new Date());
            requestTime.cipher(sessionKey); // cipher with session key Kc,s

            // TODO efetuar operacao pedida pelo cliente no Binas

            // TODO adicionar coisas contexto

        } catch(KerbyException e){
            e.printStackTrace();
        } catch(NoSuchAlgorithmException e){
            e.printStackTrace();
        } catch(InvalidKeySpecException e){
            e.printStackTrace();
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