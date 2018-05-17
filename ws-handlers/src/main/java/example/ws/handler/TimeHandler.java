package example.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.text.SimpleDateFormat;
import java.util.Set;

/**
 * This SOAPHandler outputs the endpoint address of messages, if available.
 */
public class TimeHandler implements SOAPHandler<SOAPMessageContext> {

	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	@Override
	public Set<QName> getHeaders() {
		return null;
	}

	@Override
	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outbound = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		System.out.println("\n\tTime Handler: Handling " + ((outbound) ? "OUT" : "IN") + "bound message.");
		return (outbound) ? handleOutbound(smc) : handleInbound(smc);
	}

	private boolean handleInbound(SOAPMessageContext smc) {
		return false;
	}

	private boolean handleOutbound(SOAPMessageContext smc) {
		return false;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {

	}
}
