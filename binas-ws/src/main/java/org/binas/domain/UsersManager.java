package org.binas.domain;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.binas.domain.exception.InvalidEmailException;
import org.binas.domain.exception.UserAlreadyExistsException;
import org.binas.domain.exception.UserNotFoundException;
import org.binas.station.ws.*;
import org.binas.station.ws.cli.StationClient;
import org.binas.station.ws.cli.StationClientException;
import org.binas.ws.StationView;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

import javax.xml.ws.Response;

/**
 * Class that manages the Registration and maintenance of Users
 *
 */
public class UsersManager {

	// Singleton -------------------------------------------------------------

	private UsersManager() {
	}



    /**
	 * SingletonHolder is loaded on the first execution of
	 * Singleton.getInstance() or the first access to SingletonHolder.INSTANCE,
	 * not before.
	 */
	private static class SingletonHolder {
		private static final UsersManager INSTANCE = new UsersManager();
	}

	public static synchronized UsersManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	// ------------------------------------------------------------------------

	public static int DEFAULT_INITIAL_BALANCE = 10;
	public AtomicInteger initialBalance = new AtomicInteger(DEFAULT_INITIAL_BALANCE);
	
	/**
	 * Map of existing users <email, User>. Uses concurrent hash table
	 * implementation supporting full concurrency of retrievals and high
	 * expected concurrency for updates.
	 */
	private Map<String, User> registeredUsers = new ConcurrentHashMap<>();

   public void setUserBalance(String email, int value){
       System.out.println("calling setUserBalance in binas");
       // get the current tag of this user's balance
       Vector<TaggedBalance> taggedBalances = null;
       TaggedBalance taggedBalance;

       // check if user exists
       try{
           taggedBalances = getUserBalanceFromAllStations(email);
           taggedBalance = getMostCommonTaggedBalance(taggedBalances);
       } catch(UserNotExist_Exception e){
           taggedBalance = new TaggedBalance();
           taggedBalance.setTag(1);
           taggedBalance.setValue(1);
       }

       // create a new tagged balance, with incremented tag
       TaggedBalance newTaggedBalance = new TaggedBalance();
       newTaggedBalance.setTag(taggedBalance.getTag() + 1);
       newTaggedBalance.setValue(value);

       // write new value to replicated balance in all stations
       updateUserBalanceInStations(email, newTaggedBalance);
   }

   public int getUserBalance(String email){
       Vector<TaggedBalance> taggedBalances = null;
       try{
           taggedBalances = getUserBalanceFromAllStations(email);
       } catch(UserNotExist_Exception e){
           System.out.println("User with specified email does not exist");
       }

       TaggedBalance mostCommonTaggedBalance = getMostCommonTaggedBalance(taggedBalances);

       // Write back to stations, so they are updated as soon as possible
       for(TaggedBalance tb : taggedBalances){
           if(tb.getTag() != mostCommonTaggedBalance.getTag()){
               updateUserBalanceInStations(email, mostCommonTaggedBalance);
           }
       }

       return mostCommonTaggedBalance.getValue();
   }



    /**
     * Updates the current user's balance replicated in stations with a new value and a new tag
     * @param email
     * @param newTaggedBalance
     */
    private void updateUserBalanceInStations(String email, TaggedBalance newTaggedBalance){
        Collection<String> stations = BinasManager.getInstance().getStationsUrl();
        StationClient stationClient;
        Response<SetBalanceResponse> response;
        int count = 6;

        // for each station, set this user's balance
        for(String stationId : stations){
            count = 6;
            try{
                stationClient = new StationClient(stationId);
                do {
                    response = stationClient.setBalanceAsync(email, newTaggedBalance);
                    stationClient.setBalanceAsync(email, newTaggedBalance);
                    count--;
                } while(response == null && count > 0);


            } catch(StationClientException e){
                System.out.println("Problem setting user " + email + " balance in stations");
            }
        }
    }


    /**
     * Get the tagged balance, of which the tag is most common ( majority )
     * @param taggedBalances
     * @return
     */
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

            // if tagged balance tb1 is most common so far, update most common local var
            if(count > maxCount){
                mostCommonTaggedBalanceIndex = taggedBalances.indexOf(tb1);
                maxCount = count;
            }else{
                count = 0;
            }

        }

        return taggedBalances.get(mostCommonTaggedBalanceIndex);
    }

    private Vector<TaggedBalance> getUserBalanceFromAllStations(String email) throws UserNotExist_Exception{

        Collection<String> stations = BinasManager.getInstance().getStationsUrl();

        StationClient stationClient;
        Vector<TaggedBalance> taggedBalances = new Vector<>();

        // for each station, add the taggedBalance to a vector for later comparison for Quorum Consensus
        for(String stationId : stations){
            try{
                stationClient = new StationClient(stationId);
                //call async method
                Response<GetBalanceResponse> response = stationClient.getBalanceAsync(email);
                //get taggedbalance from async method
                taggedBalances.add(response.get().getTaggedBalance());
            } catch(StationClientException e){
                System.out.println("Problem retrieving user " + email + " balance from stations");
            } catch (InterruptedException e) {
                System.out.println("Caught interrupted exception.");
                System.out.print("Cause: ");
                System.out.println(e.getCause());
            } catch (ExecutionException e) {
                System.out.println("Caught execution exception.");
                System.out.print("Cause: ");
                System.out.println(e.getCause());

                if(e.getCause() instanceof UserNotExist_Exception){
                    throw new UserNotExist_Exception("User not exist", new UserNotExist());
                }

            }
        }

        return taggedBalances;
    }

	public User getUser(String email) throws UserNotFoundException{
        if(email == null || email.trim().isEmpty()){
            throw new UserNotFoundException();
        }

        if(registeredUsers.get(email) != null)
		    return registeredUsers.get(email);
		else{
			throw new UserNotFoundException();
		}
	}
	
	public synchronized User RegisterNewUser(String email) throws UserAlreadyExistsException, InvalidEmailException, UserNotFoundException {
		if(email == null || email.trim().length() == 0 || !email.matches("\\w+(\\.?\\w)*@\\w+(\\.?\\w)*")) {
			throw new InvalidEmailException();
		}

		if(UsersManager.getInstance().getUser(email) != null){
		    throw new UserAlreadyExistsException();
        }
		
		try {
            Collection<String> stations = BinasManager.getInstance().getStationsUrl();
            StationClient stationClient;

            for(String station : stations){
                stationClient = new StationClient(station);
                stationClient.registerUser(email);
            }


			return getUser(email);

		} catch (UserNotFoundException e) {
			User user = new User(email,initialBalance.get());
			registeredUsers.put(email, user);
			return user;
		} catch(StationClientException e){
            e.printStackTrace();
        }
        return null;
    }
	
	public synchronized void reset() {
		registeredUsers.clear();
		initialBalance.set(DEFAULT_INITIAL_BALANCE);
	}
	
	public synchronized void init(int newBalance) {
		initialBalance.set(newBalance); 
	}
	
}
