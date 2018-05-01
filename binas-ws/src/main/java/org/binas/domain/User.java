package org.binas.domain;

import java.util.Collection;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.binas.domain.exception.InsufficientCreditsException;
import org.binas.domain.exception.UserAlreadyHasBinaException;
import org.binas.domain.exception.UserHasNoBinaException;
import org.binas.station.ws.GetBalanceResponse;
import org.binas.station.ws.SetBalanceResponse;
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

	public User(String email){
	    this.email = email;
	    setCredit(UsersManager.getInstance().initialBalance.get());
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
        UsersManager.getInstance().setUserBalance(email, credit);
    }

    /**
     * Get a User's credit from the replicated stations, following the Quorum Consensus protocol
     * @return user credit
     */
	public int getCredit() {
        return UsersManager.getInstance().getUserBalance(email);
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
