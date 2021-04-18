package fitnessapp_objects;

public class ChallengeStats {

    String name;
    String id;
    float distance;
    float weight;

    public ChallengeStats(){


    }
    public ChallengeStats(String id, String name){

        this.id = id;
        this.name = name;
        distance = 0;
        weight = 0;
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
}
