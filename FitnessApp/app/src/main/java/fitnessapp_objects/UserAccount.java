package fitnessapp_objects;

import com.google.firebase.auth.FirebaseUser;

import java.util.*;

public class UserAccount {

    private String userID;
    private String name;
    private String email;
    private FirebaseUser firebaseUser;
    private PaymentInfo paymentInfo;
    private FitnessData fitnessData;
    private ArrayList<Friend> friendList;
    private ArrayList<Achievement> achievements;
    private ArrayList<ChallengeHistory> challengeHistories;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public FirebaseUser getFirebaseUser() {
        return firebaseUser;
    }

    public void setFirebaseUser(FirebaseUser firebaseUser) {
        this.firebaseUser = firebaseUser;
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

    public ArrayList<Friend> getFriendList() {
        return friendList;
    }

    public void setFriendList(ArrayList<Friend> friendList) {
        this.friendList = friendList;
    }

    public ArrayList<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(ArrayList<Achievement> achievements) {
        this.achievements = achievements;
    }

    public ArrayList<ChallengeHistory> getChallengeHistories() {
        return challengeHistories;
    }

    public void setChallengeHistories(ArrayList<ChallengeHistory> challengeHistories) {
        this.challengeHistories = challengeHistories;
    }

    public UserAccount(){


    }

}
