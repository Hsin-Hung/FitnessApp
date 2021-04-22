package fitnessapp_objects;

import com.google.firebase.auth.FirebaseUser;

import java.util.*;

/**
 * this class represents the user account object, which all the user information.
 * A singleton class as there will only be one signed in user at any time.
 */
public class UserAccount {

    private String userID; // can get it from FirebaseAuth.getInstance().getCurrentUser().getUid()
    private String name;
    private String email;
    private int coin;
    private ArrayList<String> challenges; // all the challenges this user currently joined, represent by challenge doc ID

    private static UserAccount userAccount_instance = null;

    private UserAccount() {

        challenges = new ArrayList<>();
    }

    public static UserAccount getInstance(){

        if (userAccount_instance == null) userAccount_instance = new UserAccount();
        return userAccount_instance;

    }

    public void eraseAccount(){
        userAccount_instance = new UserAccount();
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getChallenges() { return challenges; }

    public void setChallenges(ArrayList<String> challenges) { this.challenges = challenges; }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getCoin() { return coin; }

    public void setCoin(int coin) { this.coin = coin; }

    public void addNewChallenge(String challengeID){
        challenges.add(challengeID);
    }

    public void removeChallenge(String challengeID) {

        challenges.remove(challengeID);

    }
    public void bet(int amount){
        coin-=amount;
    }

    /**
     *
     * this method will generate the Map needed for Database to update to firestore.
     * Add more fields to the Map to store more fields on firestore.
     *
     * @return
     */
    public Map<String, Object> getFirestoreUserMap(){

        Map<String, Object> user = new HashMap<>();

        user.put("name", name);
        user.put("email", email);
        user.put("challengesJoined", challenges);
        user.put("coins",coin);
        // ...

        return user;
    }

}
