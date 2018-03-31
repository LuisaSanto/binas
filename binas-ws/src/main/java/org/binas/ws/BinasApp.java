package org.binas.ws;


public class BinasApp {

	public static void main(String[] args) throws Exception{
        // Check arguments
        if (args.length != 3) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: java " + BinasApp.class.getName() + " wsName wsURL uddiURL");
            return;
        }
        String wsName = args[0];
        String wsURL = args[1];
        String uddiUrl = args[2];


        // handle UDDI arguments

        BinasEndpointManager endpoint = new BinasEndpointManager(uddiUrl, wsName, wsURL);

        System.out.println(BinasApp.class.getSimpleName() + " running");

        // start Web Service
        try{
            endpoint.start();
            endpoint.awaitConnections();
        } finally{
            endpoint.stop();
        }
    }

}