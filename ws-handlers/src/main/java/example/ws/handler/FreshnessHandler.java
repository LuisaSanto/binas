package example.ws.handler;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.*;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
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

	private boolean validToken(String path, String token) {
		try{
			FileInputStream is = new FileInputStream(path);

			BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset())); //is it?
			String line;
			while((line = reader.readLine()) != null){
				if(line.trim().equals(token)){
					System.out.println("Rejecting... Token found!");
					return false;
				}
			}
			reader.close();
			is.close();
			return true;
		} catch (FileNotFoundException e) {
			//TODO
			//create file
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Error reading file. ");
		}
		return true;
	}

	private void addToken(String path, String token) {
		try{
			BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
			output.append(token);
			output.newLine();
			output.close();
		} catch (IOException e) {
			System.out.println("Error writing file. ");
		}
	}


	private boolean handleInbound(SOAPMessageContext smc) {
		try{
			SOAPEnvelope env = smc.getMessage().getSOAPPart().getEnvelope();
			SOAPHeader sh = getHeader(env);
			Name name = env.createName("token", "nsID", "uri:token");
			Iterator it = sh.getChildElements(name);
			if(!it.hasNext()) return true;

			SOAPElement el = (SOAPElement) it.next();
			String token = el.getValue();

			String path = "target/tokens.tsv";
			if(!validToken(path, token)){
				System.out.println("Freshness Handler: Invalid token. Rejecting message.");
				return false;
			}

			addToken(path, token);

			//TODO CONTEXT_PROPERTU

		} catch (SOAPException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean handleFault(SOAPMessageContext smc) {
		return false;
	}

	@Override
	public void close(MessageContext smc) {

	}
}
