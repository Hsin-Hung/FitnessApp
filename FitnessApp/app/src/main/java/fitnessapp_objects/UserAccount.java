package fitnessapp_objects;

import com.google.firebase.auth.FirebaseUser;

import java.util.*;

//UserAccount is a singleton, call UserAccount.getInstance() to get the user account.
public class UserAccount {

    private String userID;
    private String name;
    private String email;
    private PaymentInfo paymentInfo;
    private FitnessData fitnessData;
    private ArrayList<String> friendList;
    private ArrayList<Achievement> achievements;
    private ChallengeHistory challengeHistory;

    private static UserAccount userAccount_instance = null;

    private UserAccount() {
    }

    public static UserAccount getInstance(){

        if (userAccount_instance == null) userAccount_instance = new UserAccount();
        return userAccount_instance;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public FitnessData getFitnessData() {
        return fitnessData;
    }

    public void setFitnessData(FitnessData fitnessData) {
        this.fitnessData = fitnessData;
    }

    public ArrayList<String> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<String> friendList) {
        this.friendList = friendList;
    }

    public ArrayList<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(ArrayList<Achievement> achievements) {
        this.achievements = achievements;
    }

    public ChallengeHistory getChallengeHistories() {
        return challengeHistory;
    }

    public void setChallengeHistories(ChallengeHistory challengeHistories) {
        this.challengeHistory = challengeHistories;
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

        // ...

        return user;
    }

}
