package org.binas.domain;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.binas.domain.exception.InsufficientCreditsException;
import org.binas.domain.exception.UserAlreadyHasBinaException;
import org.binas.domain.exception.UserHasNoBinaException;
import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.TaggedBalance;
import org.binas.station.ws.UserNotExist_Exception;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;

import javax.xml.ws.Response;

/**
 * 
 * Domain class that represents the User and deals with their creation, balance manipulation, email manipulation, etc.
 * 
 *
 */
public class User {

	private String email;
	private AtomicBoolean hasBina = new AtomicBoolean(false);
	
	public User(String email, int initialBalance) {
		this.email = email;
		setCredit(initialBalance);
	}
	
	public synchronized void decrementBalance() throws InsufficientCreditsException{
		int currentUserBalance = getCredit();

	    if(currentUserBalance > 0) {
			 setCredit(currentUserBalance - 1);
		 } else {
			 throw new InsufficientCreditsException();
		 }
	}

	
	public synchronized void incrementBalance(int amount){
        int currentUserBalance = getCredit();

        if( amount > 0 )
            setCredit(currentUserBalance + amount);
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
                System.out.println("Problem setting user " + this.email + " balance in stations");
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

		// Write back to stations
        setCredit(taggedBalance.getValue());

		return taggedBalance.getValue();
	}

	private TaggedBalance getMostCommonTaggedBalance(Vector<TaggedBalance> taggedBalances){
        int mostCommonTaggedBalanceIndex = 0;
        int maxCount = 0;
        int count = 0;

        // Compare each balance with every balance in taggedBalances, and count number of occurences of each tag
        for(TaggedBalance tb1 : taggedBalances){
            for(TaggedBalance tb2 : taggedBalances){

                if(tb1.getTag() == tb2.getTag()){
                    count++;
                }
            }

            if(count > maxCount){
                mostCommonTaggedBalanceIndex = taggedBalances.indexOf(tb1);
                maxCount = count;
            }else{
                count = 0;
            }

        }

        return taggedBalances.get(mostCommonTaggedBalanceIndex);
    }

	private Vector<TaggedBalance> getUserBalanceFromAllStations(){
        Collection<String> stations = BinasManager.getInstance().getStations();
        StationClient stationClient;
	    Vector<TaggedBalance> taggedBalances = new Vector<>();

        // for each station, add the taggedBalance to a vector for later comparison for Quorum Consensus
        for(String stationId : stations){
            try{
                stationClient = new StationClient(stationId);
                //call async method
                Response<GetBalanceResponse> response = stationClient.getBalanceAsync(this.email);
                //get taggedbalance from async method
                taggedBalances.add(response.get().getUserView());
            } catch(StationClientException e){
                System.out.println("Problem retrieving user " + this.email + " balance from stations");
            } catch (InterruptedException e) {
                System.out.println("Caught interrupted exception.");
                System.out.print("Cause: ");
                System.out.println(e.getCause());
            } catch (ExecutionException e) {
                System.out.println("Caught execution exception.");
                System.out.print("Cause: ");
                System.out.println(e.getCause());
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
