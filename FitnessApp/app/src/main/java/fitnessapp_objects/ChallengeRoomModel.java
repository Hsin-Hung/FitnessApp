package fitnessapp_objects;


import com.google.firebase.Timestamp;

public class ChallengeRoomModel {

    String name;
    ChallengeType type;
    boolean isBet;
    int betAmount;
    Timestamp endDate;


    public ChallengeRoomModel(String name, ChallengeType type, boolean isBet, int betAmount, Timestamp endDate) {
        this.name = name;
        this.type = type;
        this.isBet = isBet;
        this.betAmount = betAmount;
        this.endDate = endDate;
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
}
