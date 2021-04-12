package fitnessapp_objects;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class ChallengeRoom {

    private String name;
    private String description;
    private String password;
    private Timestamp endDate;
    private boolean isBet;
    private int betAmount;
    private ChallengeType type;
    private ArrayList<Participant> participants;
    private boolean started;

    public ChallengeRoom(){

    }

    public ChallengeRoom(String name, ChallengeType type, String description, String password, Timestamp endDate, boolean isBet,
                         int betAmount) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.password = password;
        this.endDate = endDate;
        this.isBet = isBet;
        this.betAmount = betAmount;
        this.started = false;
        participants = new ArrayList<>();

    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStarted() { return started; }

    public void setStarted(boolean started) { this.started = started; }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
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

    public ArrayList<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Participant> participants) {
        this.participants = participants;
    }
    public void addParticipant(Participant participant){
        participants.add(participant);
    }

//
//    public Map<String, Object> getFirestoreChallengeRoomMap(){
//
//        Map<String, Object> challengeRoom = new HashMap<>();
//
//        challengeRoom.put("name", name);
//        challengeRoom.put("type", type.toString());
//        challengeRoom.put("description", description);
//        challengeRoom.put("password", password);
//        challengeRoom.put("endDate", new Timestamp(endDate.getTime()));
//        challengeRoom.put("isBet", isBet);
//        challengeRoom.put("betAmount", betAmount);
//        challengeRoom.put("participants", participants);
//
//
//        return challengeRoom;
//
//
//
//    }
}
