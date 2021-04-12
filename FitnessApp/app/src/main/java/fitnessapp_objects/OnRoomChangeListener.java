package fitnessapp_objects;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnRoomChangeListener {

    public void addParticipant(ArrayList<Participant> participants);
    public void removeParticipant(ArrayList<Participant> participants);
}
