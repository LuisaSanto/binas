package org.binas.domain;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.binas.domain.exception.InsufficientCreditsException;
import org.binas.domain.exception.UserAlreadyHasBinaException;
import org.binas.domain.exception.UserHasNoBinaException;
import org.binas.station.ws.TaggedBalance;
import org.binas.station.ws.UserNotExist_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

/**
 * 
 * Domain class that represents the User and deals with their creation, balance manipulation, email manipulation, etc.
 * 
 *
 */
public class User {

	private String email;
	private AtomicInteger balance;
	private AtomicBoolean hasBina = new AtomicBoolean(false);
	
	public User(String email, int initialBalance) {
		this.email = email;


	}
	
	public synchronized void decrementBalance() throws InsufficientCreditsException{
		 if(balance.get() > 0) {
			 balance.decrementAndGet();
		 } else {
			 throw new InsufficientCreditsException();
		 }
	}

	
	public synchronized void incrementBalance(int amount){
		 balance.getAndAdd(amount);
	}
	
	public String getEmail() {
		return email;
	}
	
	public boolean getHasBina() {
		return hasBina.get();
	}

    /**
     * Set a user's credit to credit, in all replicated stations, (Quorum Consensus write operation)
     * @param credit new user credit
     */
    public void setCredit(int credit){
        // get the current tag of this user's balance
        Vector<TaggedBalance> taggedBalances = getUserBalanceFromAllStations();
        TaggedBalance previousTaggedBalance = getMostCommonTaggedBalance(taggedBalances);

        // create a new tagged balance, with incremented tag
        TaggedBalance newTaggedBalance = new TaggedBalance();
        newTaggedBalance.setTag(previousTaggedBalance.getTag() + 1);
        newTaggedBalance.setValue(credit);

        // write new value to replicated balance in all stations
        updateReplicatedStationBalances(newTaggedBalance);
    }

    /**
     * Updates the current user's balance replicated in stations with a new value and a new tag
     * @param newTaggedBalance
     */
    private void updateReplicatedStationBalances(TaggedBalance newTaggedBalance){
        Collection<String> stations = BinasManager.getInstance().getStations();
        StationClient stationClient = null;

        // for each station, set this user's balance
        for(String stationId : stations){
            try{
                stationClient = new StationClient(stationId);

                stationClient.setBalance(this.email, newTaggedBalance);
            } catch(StationClientException e){
                // TODO
                e.printStackTrace();
            }
        }
    }


    /**
     * Get a User's credit from the replicated stations, following the Quorum Consensus protocol
     * @return user credit
     */
	public int getCredit() {
		Vector<TaggedBalance> taggedBalances = getUserBalanceFromAllStations();

		TaggedBalance taggedBalance = getMostCommonTaggedBalance(taggedBalances);

		return taggedBalance.getValue();
	}

	private TaggedBalance getMostCommonTaggedBalance(Vector<TaggedBalance> taggedBalances){
        int mostCommonTaggedBalanceIndex = 0;
        int count = 0;

        // Compare each balance with every balance in taggedBalances, and count number of occurences of each tag
        for(TaggedBalance tb1 : taggedBalances){
            for(TaggedBalance tb2 : taggedBalances){

                if(tb1.getTag() == tb2.getTag()){
                    count++;
                }
            }

            // update mostCommonTaggedBalanceIndex with the most common tag's respective vector index
            if(count > mostCommonTaggedBalanceIndex){
                mostCommonTaggedBalanceIndex = count;
            }else{
                count = 0;
            }
        }

        return taggedBalances.get(mostCommonTaggedBalanceIndex);
    }

	private Vector<TaggedBalance> getUserBalanceFromAllStations(){
        Collection<String> stations = BinasManager.getInstance().getStations();
        StationClient stationClient = null;
	    Vector<TaggedBalance> taggedBalances = new Vector<>();

        // for each station, add the taggedBalance to a vector for later comparison for Quorum Consensus
        for(String stationId : stations){
            try{
                stationClient = new StationClient(stationId);
                taggedBalances.add(stationClient.getBalance(this.email));
            } catch(StationClientException e){
                e.printStackTrace();
                // TODO
            } catch(UserNotExist_Exception e){
                e.printStackTrace();
                // TODO
            }
        }

        return taggedBalances;
    }

	public synchronized void validateCanRentBina() throws InsufficientCreditsException, UserAlreadyHasBinaException{
		if(getHasBina()) {
			throw new UserAlreadyHasBinaException();
		}
		if(getCredit() <= 0) {
			throw new InsufficientCreditsException();
		}
		
	}
	public synchronized void validateCanReturnBina() throws UserHasNoBinaException {
		if( ! getHasBina()) {
			throw new UserHasNoBinaException();
		}
	}

	public synchronized void effectiveRent() throws InsufficientCreditsException {
		decrementBalance();
		hasBina.set(true);
	}

	public synchronized void effectiveReturn(int prize) throws UserHasNoBinaException {
		if( ! getHasBina()) {
			throw new UserHasNoBinaException();
		}
		hasBina.set(false);
		incrementBalance(prize);
	}


	
}
