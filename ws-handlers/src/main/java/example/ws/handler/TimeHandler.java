package example.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
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

	private SOAPHeader getHeader(SOAPEnvelope env) throws SOAPException {
		SOAPHeader header = env.getHeader();
		if (header == null)
			header = env.addHeader();
		return header;
	}

	private boolean handleInbound(SOAPMessageContext smc) {
		try{
			SOAPEnvelope env = smc.getMessage().getSOAPPart().getEnvelope();
			SOAPHeader sh = getHeader(env);
			Name name = env.createName("time", "nsTIME", "uri:time");
			Iterator it = sh.getChildElements(name);

			//check header element
			if(!it.hasNext()){
				System.out.println("Time Handler: element not found!");
				return false;
			}
			SOAPElement el = (SOAPElement) it.next();
			String value = el.getValue();
			Date dateValue = dateFormatter.parse(value);

			Date dateNow = new Date();
			long difference = dateNow.getTime() - dateValue.getTime();

			if(difference > 10*1000){
				System.out.println("Time rejection: interval superior to 10 seconds!");
				return false;
			}

			// Put header in a property context
			String CONTEXT_PROPERTY = "time.property";
			smc.put(CONTEXT_PROPERTY, value);
			// Set property scope to application client/server class can access it
			smc.setScope(CONTEXT_PROPERTY, MessageContext.Scope.APPLICATION);

		} catch (SOAPException | ParseException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean handleOutbound(SOAPMessageContext smc) {
		try{
			SOAPEnvelope env = smc.getMessage().getSOAPPart().getEnvelope();
			SOAPHeader sh = getHeader(env);
			Name name = env.createName("time", "nsTIME", "uri:time");
			SOAPHeaderElement el = sh.addHeaderElement(name);
			el.addTextNode(dateFormatter.format(new Date()));
		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		return false;
	}

	@Override
	public void close(MessageContext context) {

	}
}
