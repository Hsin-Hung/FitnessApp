package fitnessapp_objects;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * this class represent a challenge and contains all its info
 */
public class ChallengeRoom {

    private String id;
    private String name;
    private String description;
    private String password;
    private Timestamp endDate;
    private boolean isBet;
    private int betAmount;
    private ChallengeType type;
    private ArrayList<Participant> participants;
    private boolean started;
    private int pot;

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
        this.started = true;
        participants = new ArrayList<>();
        this.pot = 0;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void addParticipant(Participant participant){
        participants.add(participant);

    }

    public int getPot() {
        return pot;
    }

    public void setPot(int pot) {
        this.pot = pot;
    }

    @Exclude
    /**
     *
     * @return the map of all the needed challenge info to display to participants
     */
    public HashMap<String, String> getFirestoreChallengeRoomMap(){

        HashMap<String, String> challengeRoom = new HashMap<>();

        challengeRoom.put("roomID", id);
        challengeRoom.put("name", name);
        challengeRoom.put("type", type.toString());
        challengeRoom.put("description", description);
        challengeRoom.put("endDate", endDate.toDate().toString());
        challengeRoom.put("betAmount", String.valueOf(betAmount));

        return challengeRoom;

    }
}
