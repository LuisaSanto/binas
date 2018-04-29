package org.binas.station.domain;

import java.util.concurrent.atomic.AtomicInteger;

/** User object to manage an association between an user's email and the user's balance in each station**/
public class User {
    private String email;
    private AtomicInteger balance;
    private String mostRecentTag;

    public User(String email, int initialBalance, String tag) {
        this.email = email;
        balance = new AtomicInteger(initialBalance);
        this.mostRecentTag = tag;
    }

    public String getMostRecentTag(){
        return mostRecentTag;
    }

    public void setMostRecentTag(String tag){
        this.mostRecentTag = tag;
    }


    public String getEmail() {
        return email;
    }

    public int getBalance() {
        return balance.get();
    }

    public void setBalance(int balance){
        this.balance.set(balance);
    }
}