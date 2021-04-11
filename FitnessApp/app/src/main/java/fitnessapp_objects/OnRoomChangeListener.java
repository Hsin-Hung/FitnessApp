package fitnessapp_objects;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnRoomChangeListener {

    public void addParticipant(ArrayList<HashMap<String,String>> participants);
    public void removeParticipant(ArrayList<HashMap<String,String>> participants);
}
