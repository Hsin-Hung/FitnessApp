package fitnessapp_objects;

public class ChallengeStats {

    float distance;
    float weight;

    public ChallengeStats(){

        distance = 0;
        weight = 0;
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
}
