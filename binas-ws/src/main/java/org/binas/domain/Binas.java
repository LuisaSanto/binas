package org.binas.domain;

import org.binas.exception.EmailExistsException;
import org.binas.exception.InvalidEmailException;
import org.binas.exception.UserNotExistsException;
import org.binas.ws.UserView;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Binas {
    private static Vector<UserView> users;

    private Binas(){
        users = new Vector<>();
    }

    private static class SingletonHolder {
        private static final Binas INSTANCE = new Binas();
    }

    public static Binas getInstance(){
        return SingletonHolder.INSTANCE;
    }

    public synchronized UserView getUser(String email){
        for(UserView user : users){
            if(user.getEmail().equals(email)){
                return user;
            }
        }
        throw new UserNotExistsException();
    }

    // activate a user by checking if the email is valid and then adding to the vector
    public synchronized UserView activateUser(String email){
        UserView user = new UserView();

        if(isEmailValid(email)){
            if(!userWithEmailExists(email)){
                user.setEmail(email);
                user.setCredit(0);
                users.add(user);

                return user;
            }else{
                throw new EmailExistsException();
            }

        }else{
            throw new InvalidEmailException();
        }
    }

    // Check if an user exists (is active ) with specified email
    public synchronized boolean userWithEmailExists(String email){
        for(UserView user : users){
            if(user.getEmail().equals(email)){
                return true;
            }
        }
        return false;
    }

    // check if the specified email is valid
    private boolean isEmailValid(String email){
        String pattern = "^[a-zA-Z0-9]+(\\.([a-zA-Z0-9])+)*@[a-zA-Z0-9]+(\\.([a-zA-Z0-9])+)*$";
        Pattern pattern1 = Pattern.compile(pattern);

        Matcher matcher = pattern1.matcher(email);

        return matcher.find();

    }

    public void clearUsers(){
        users.clear();
    }
}
