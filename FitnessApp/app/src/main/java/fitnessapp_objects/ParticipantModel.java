package fitnessapp_objects;

public class ParticipantModel{

    private String name;
    private String id;
    private ChallengeType type;
    private float distance;
    private float weight;

    public ParticipantModel(String name, String id){

        this.name = name;
        this.id = id;

    }
    public ParticipantModel(String name, String id, float distance, ChallengeType type){
        this(name, id);
        this.distance = distance;
        this.type = type;

    }

    public ParticipantModel(String name, String id, ChallengeType type, float weight ){
        this(name, id);
        this.weight = weight;
        this.type = type;
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

    public ChallengeType getType() {
        return type;
    }

    public void setType(ChallengeType type) {
        this.type = type;
    }


}
