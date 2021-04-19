package fitnessapp_objects;

public class ChallengeStats {

    String name;
    String id;
    boolean hasBet;
    boolean hasBegin;
    boolean weightPrompt;
    float distance;
    float weight;

    public ChallengeStats(){


    }
    public ChallengeStats(String id, String name){

        this.id = id;
        this.name = name;
        distance = 0;
        weight = 0;
        hasBet = false;
        hasBegin = false;
        weightPrompt = true;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean isHasBet() {
        return hasBet;
    }

    public void setHasBet(boolean hasBet) {
        this.hasBet = hasBet;
    }

    public boolean isHasBegin() {
        return hasBegin;
    }

    public void setHasBegin(boolean hasBegin) {
        this.hasBegin = hasBegin;
    }

    public boolean isWeightPrompt() {
        return weightPrompt;
    }

    public void setWeightPrompt(boolean weightPrompt) {
        this.weightPrompt = weightPrompt;
    }
}
