package fitnessapp_objects;

import java.util.ArrayList;

public class ChallengeRoom {

    private String name;
    private String description;
    private int password;
    private int ID;
    private Date endDate;
    private boolean isBet;
    private int betAmount;
    private ChallengeType type;
    private ArrayList<ChallengeParticipant> participants;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPassword() {
        return password;
    }

    public void setPassword(int password) {
        this.password = password;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }

    public ArrayList<ChallengeParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<ChallengeParticipant> participants) {
        this.participants = participants;
    }
}
