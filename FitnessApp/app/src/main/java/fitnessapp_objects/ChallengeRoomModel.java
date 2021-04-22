package fitnessapp_objects;


import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

/**
 * this challenge room model class is used by the adapter to populate list views
 */
public class ChallengeRoomModel {

    private String id;
    private String name;
    private String description;
    private ChallengeType type;
    private boolean isBet;
    private int betAmount;
    private Timestamp endDate;
    private String password;
    private boolean started;


    public ChallengeRoomModel(String id, String description, String name, ChallengeType type, boolean isBet, int betAmount, Timestamp endDate, String password, boolean started) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.type = type;
        this.isBet = isBet;
        this.betAmount = betAmount;
        this.endDate = endDate;
        this.password = password;
        this.started = started;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public HashMap<String, String> getChallengeInfoMap(){

        HashMap<String, String> map = new HashMap<>();

        map.put("roomID", id);
        map.put("name", name);
        map.put("description",description);
        map.put("type", type.toString());
        map.put("betAmount", String.valueOf(betAmount));
        map.put("endDate", endDate.toDate().toString());
        map.put("started", Boolean.toString(started));

        return map;
    }
}
