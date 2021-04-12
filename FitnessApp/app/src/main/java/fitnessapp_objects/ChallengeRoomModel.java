package fitnessapp_objects;


import com.google.firebase.Timestamp;

public class ChallengeRoomModel {

    String id;
    String name;
    ChallengeType type;
    boolean isBet;
    int betAmount;
    Timestamp endDate;
    String password;


    public ChallengeRoomModel(String id, String name, ChallengeType type, boolean isBet, int betAmount, Timestamp endDate, String password) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isBet = isBet;
        this.betAmount = betAmount;
        this.endDate = endDate;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }

    public boolean isBet() {
        return isBet;
    }

    public void setBet(boolean bet) {
        isBet = bet;
    }

    public int getBetAmount() {
        return betAmount;
    }

    public void setBetAmount(int betAmount) {
        this.betAmount = betAmount;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
