package org.binas.domain;


import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.CoordinatesView;
import org.binas.ws.StationView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINamingException;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDIRecord;

import java.util.Collection;
import java.util.List;
import java.util.Vector;

/* Gestor do binas, possui funcionalidades de administracao */
public class BinasManager {

    // Singleton -------------------------------------------------------------

    private BinasManager() {
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance()
     * or the first access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        private static final BinasManager INSTANCE = new BinasManager();
    }

    public static synchronized BinasManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static synchronized List<StationView> getStations(String uddiUrl) {
        try {
            UDDINaming uddiNaming = new UDDINaming(uddiUrl);

            // get all station records in UDDI , using * wildcard
            Collection<UDDIRecord> UDDIrecords = uddiNaming.listRecords("A58_Station%");

            Vector<String> urls = new Vector<>();
            for (UDDIRecord uddiRecord : UDDIrecords) {
                urls.add(uddiRecord.getUrl());
            }

            Vector<StationView> allStationViews = new Vector<>();
            StationClient stationClient = null;
            for (int i = 0; i < urls.size(); i++) {
                stationClient = new StationClient(urls.elementAt(i));
                StationView temp = convertStations(stationClient);
                allStationViews.addElement(temp);
            }
            return allStationViews;
        } catch (UDDINamingException e) {
            System.out.println("Problem reaching UDDI from Binas");
        } catch (StationClientException e) {
            System.out.println("Problem creating StationClient");
        }
        return null;
    }

    public static synchronized StationView getStationView(String stationId, String uddiUrl) {
        Vector<StationView> allStationViews = (Vector<StationView>) getStations(uddiUrl);
        for (StationView station : allStationViews) {
            if (station.getId().equals(stationId)) {
                return station;
            }
        }
        return null;
    }


    public static synchronized StationView convertStations(StationClient stationClient) {
        StationView temp = new StationView();

        String id = stationClient.getInfo().getId();
        Integer x = stationClient.getInfo().getCoordinate().getX();
        Integer y = stationClient.getInfo().getCoordinate().getY();
        int capacity = stationClient.getInfo().getCapacity();
        int totalGets = stationClient.getInfo().getTotalGets();
        int totalReturns = stationClient.getInfo().getTotalReturns();
        int availableBinas = stationClient.getInfo().getAvailableBinas();
        int freeDocks = stationClient.getInfo().getFreeDocks();
        temp.setId(id);
        temp.setCoordinate(new CoordinatesView());
        temp.getCoordinate().setX(x);
        temp.getCoordinate().setY(y);
        temp.setCapacity(capacity);
        temp.setTotalGets(totalGets);
        temp.setTotalReturns(totalReturns);
        temp.setAvailableBinas(availableBinas);
        temp.setFreeDocks(freeDocks);

        return temp;
    }

    public static synchronized StationClient getStationClient(String stationID, String uddiUrl) {
        List<StationView> stationViews = getStations(uddiUrl);
        for(StationView stationView : stationViews){
            if(stationView.getId().equals(stationID)){
                try{
                    StationClient stationClient = new StationClient(uddiUrl, stationID);

                    return stationClient;
                } catch(StationClientException e){
                    System.out.println("Problem finding station client");
                }
            }
        }
        return null;
    }


}
