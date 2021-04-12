package fitnessapp_objects;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.*;

// All the firebase calling action will stay in this class, Singleton class
public class Database {

    private static Database dataBase_instance = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String TAG = "Database";
    private final int NUM_RANDOM = 5;
    private FirebaseAuth mAuth;
    private UserAccount userAccount;
    private ListenerRegistration challengeRoomListener;


    private Database(){

        mAuth = FirebaseAuth.getInstance();
        userAccount = UserAccount.getInstance();
    }

    public static Database getInstance(){

        if (dataBase_instance == null) dataBase_instance = new Database();
        return dataBase_instance;

    }

    /**
     *
     * update the firestore with the local user account
     *
     * @param handler: implement and pass this handler if the calling class wants to navigate
     *               to some other screen immediately after the update completes. If not, then
     *               just pass null.
     * @return
     */
    public boolean updateUserAccount(UIUpdateCompletionHandler handler){

        FirebaseUser user = mAuth.getCurrentUser();

        db.collection("users").
                document(user.getUid())
                .set(userAccount.getFirestoreUserMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        if(handler!=null){
                            handler.updateUI(true);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        if(handler!=null){
                            handler.updateUI(false);
                        }

                    }
                });

        return true;
    }


    public String updateChallengeRoom(ChallengeRoom room, UIUpdateCompletionHandler handler){

        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount userAccount = UserAccount.getInstance();
        // Get a new write batch
        WriteBatch batch = db.batch();

        DocumentReference challengeRef = db.collection("challenges").document();

        // add the new challenge room data to firestore first
        batch.set(challengeRef, room);

        DocumentReference userAccountRef = db.collection("users").document(user.getUid());

        // update the current user to have this new challenge room
        batch.update(userAccountRef, "challengesJoined", FieldValue.arrayUnion(challengeRef.getId()));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                userAccount.addNewChallenge(challengeRef.getId());
                handler.updateUI(true);
            }
        });


        return challengeRef.getId();

    }

    /**
     *
     * this attaches a listener to all challenge rooms that contain this current logged in user for real time changes,
     * however, it only cares for the only with the given room ID.
     *
     * @param challengeID: the challenge room ID you want to fetch
     * @param listener: the caller that is listening to the change on firetstore
     */
    public void startChallengeRoomChangeListener(String challengeID, OnRoomChangeListener listener){
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount account = UserAccount.getInstance();

        challengeRoomListener = db.collection("challenges")
                .whereArrayContains("participants", new ParticipantModel(account.getName(),user.getUid()))
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {

                            if(dc.getDocument().getId().equals(challengeID)){
                                Map<String,Object> dataMap = dc.getDocument().getData();
                                ChallengeRoom challengeRoom = dc.getDocument().toObject(ChallengeRoom.class);
                                switch (dc.getType()) {
                                    case ADDED:
                                        Log.d(TAG, "New participant: " + dataMap);
                                        listener.addParticipant(challengeRoom.getParticipants());
                                        break;
                                    case MODIFIED:
                                        Log.d(TAG, "Modified participant: " + dataMap);
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "Removed participant: " + dataMap);
                                        listener.removeParticipant(challengeRoom.getParticipants());
                                        break;
                                }
                            }

                        }
                    }
                });

    }

    public void detachChallengeRoomListener(){

        if(challengeRoomListener!=null)
        challengeRoomListener.remove();


    }


    /**
     *
     * update the local user account with what is stored on firestore
     *
     * @param uid: the uid of the current user
     * @param handler: implement and pass this handler if the calling class wants to navigate
     *                 to some other screen immediately after the update completes. If not, then
     *                 just pass null.
     * @return
     */
    public boolean updateLocalUserAccount(String uid, UIUpdateCompletionHandler handler){

        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        userAccount.setUserID(uid);
                        userAccount.setName(document.get("name").toString());
                        userAccount.setEmail(document.get("email").toString());

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        handler.updateUI(true);
                    } else {
                        Log.d(TAG, "No such document");
                        handler.updateUI(false);
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    handler.updateUI(false);
                }
            }
        });

        return false;
    }

    public boolean getPendingChallengeRooms(FirestoreCompletionHandler handler){

        db.collection("challenges")
                .whereEqualTo("started", false)
                .limit(NUM_RANDOM)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Map<String, ChallengeRoom> challengeRooms = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                ChallengeRoom c = document.toObject(ChallengeRoom.class);
                               challengeRooms.put(document.getId(), c);

                            }
                            handler.challengeRoomsTransfer(challengeRooms);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });



        return false;
    }



}
