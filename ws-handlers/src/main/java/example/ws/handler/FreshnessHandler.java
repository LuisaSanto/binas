package example.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;

import static javax.xml.bind.DatatypeConverter.printBase64Binary;

/**
 * This SOAPHandler outputs the endpoint address of messages, if available.
 */
public class FreshnessHandler implements SOAPHandler<SOAPMessageContext> {


	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		System.out.println("\n\tFreshness Handler: Handling " + ((outbound) ? "OUT" : "IN") + "bound message.");
		return (outbound) ? handleOutbound(smc) : handleInbound(smc);
	}

	private SOAPHeader getHeader(SOAPEnvelope env) throws SOAPException{
		SOAPHeader header = env.getHeader();
		if (header == null)
			header = env.addHeader();
		return header;
	}

	private boolean handleOutbound(SOAPMessageContext smc) {
		try{
			long timestamp = new Date().getTime();
			final byte array[] = new byte[16];
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.nextBytes(array);

			SOAPEnvelope env = smc.getMessage().getSOAPPart().getEnvelope();
			SOAPHeader sh = getHeader(env);
			Name name = env.createName("token", "nsID", "uri:token");
			SOAPHeaderElement el = sh.addHeaderElement(name);
			el.addTextNode(printBase64Binary(array));

		} catch (NoSuchAlgorithmException e) {
			System.out.println("SecureRandom algorithm does not exist" + e.getMessage());
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean handleInbound(SOAPMessageContext smc) {
		return false;
	}

	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return false;
	}

	@Override
	public void close(MessageContext smc) {

	}
}
