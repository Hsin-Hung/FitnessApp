package fitnessapp_objects;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.WorkManager;

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
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.*;

/**
 * this class acts as an API to read or write to the firebase
 * A singleton class as there is only one database
 */
public class Database {

    private static Database dataBase_instance = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String TAG = "Database";
    private final int NUM_RANDOM = 5;
    private ListenerRegistration challengeRoomListener, coinChangeListener, statsChangeListener, challengeStatusListener;

    private Database(){

    }

    public static Database getInstance(){

        if (dataBase_instance == null) dataBase_instance = new Database();
        return dataBase_instance;

    }

    public interface OnRoomGetCompletionHandler {

        void challengeRoomsTransfer(Map<String,ChallengeRoom> rooms);

    }
    public interface OnRoomChangeListener {

        void modifyParticipant(ArrayList<Participant> participants);
    }
    public interface UIUpdateCompletionHandler {

        void updateUI(boolean isSuccess, Map<String, String> data, int callbackCode);
    }
    public interface  OnLeaderBoardStatsGetCompletionHandler{
        void statsTransfer(ArrayList<ChallengeStats> stats);
    }

    public interface OnDataPassHandler{

        void passData(Map<String,String> data);

    }

    public interface OnBooleanPromptHandler {

        void passBoolean(boolean check);

    }

    public interface OnPlaceBetHandler{

        void placeBet(boolean place);
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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        UserAccount userAccount = UserAccount.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        db.collection("users").
                document(user.getUid())
                .set(userAccount.getFirestoreUserMap())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        if(handler!=null){
                            handler.updateUI(true, null, 0);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        if(handler!=null){
                            handler.updateUI(false, null, 0);
                        }

                    }
                });

        return true;
    }

    /**
     *
     * update a new challenge room when created
     *
     * @param room
     * @param handler
     * @return
     */
    public String updateChallengeRoom(ChallengeRoom room, UIUpdateCompletionHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount userAccount = UserAccount.getInstance();
        // Get a new write batch
        WriteBatch batch = db.batch();

        DocumentReference challengeRef = db.collection("challenges").document();

        // add the new challenge room data to firestore first
        room.setId(challengeRef.getId());
        batch.set(challengeRef, room);

        DocumentReference dataRef = db.collection("challenges").document(challengeRef.getId()).collection("stats").document(user.getUid());

        batch.set(dataRef, new ChallengeStats(user.getUid(), userAccount.getName()));

        DocumentReference userAccountRef = db.collection("users").document(user.getUid());

        // update the current user to have this new challenge room
        batch.update(userAccountRef, "challengesJoined", FieldValue.arrayUnion(challengeRef.getId()));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Map<String, String> roomID = new HashMap<>();
                    roomID.put("roomID", challengeRef.getId());
                    userAccount.addNewChallenge(challengeRef.getId());
                    handler.updateUI(true, roomID, 0);
                }

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

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount account = UserAccount.getInstance();

        challengeRoomListener = db.collection("challenges")
                .whereArrayContains("participants", new Participant(account.getName(),user.getUid()))
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
                                        listener.modifyParticipant(challengeRoom.getParticipants());
                                        break;
                                    case MODIFIED:
                                        Log.d(TAG, "Modified participant: " + dataMap);
                                        listener.modifyParticipant(challengeRoom.getParticipants());
                                        break;
                                    case REMOVED:
                                        Log.d(TAG, "Removed participant: " + dataMap);
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

        UserAccount userAccount = UserAccount.getInstance();

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
                        userAccount.setCoin(Integer.parseInt(document.get("coins").toString()));

                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        handler.updateUI(true, null, 0);
                    } else {
                        Log.d(TAG, "No such document");
                        handler.updateUI(false, null, 0);
                    }

                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    handler.updateUI(false, null, 0);
                }
            }
        });

        return false;
    }

    /**
     * get all the started challenges from firebase and pass the result back to the calling class
     * @param handler
     * @return
     */
    public boolean getPendingChallengeRooms(OnRoomGetCompletionHandler handler){

        db.collection("challenges")
                .whereEqualTo("started", true)
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

    /**
     *
     * join a challenge room of a given room ID and update the calling class's UI accordingly
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean joinChallengeRoom(String roomID, UIUpdateCompletionHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount account = UserAccount.getInstance();
        Participant participant = new Participant(account.getName(), user.getUid());

        WriteBatch batch = db.batch();

        DocumentReference challengeRef = db.collection("challenges").document(roomID);

        // add the new challenge room data to firestore first
        batch.update(challengeRef, "participants", FieldValue.arrayUnion(participant));

        DocumentReference userRef = db.collection("challenges").document(roomID).collection("stats").document(user.getUid());

        batch.set(userRef, new ChallengeStats(user.getUid(), account.getName()));

        DocumentReference userAccountRef = db.collection("users").document(user.getUid());

        // update the current user to have this new challenge room
        batch.update(userAccountRef, "challengesJoined", FieldValue.arrayUnion(challengeRef.getId()));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    account.addNewChallenge(roomID);
                    handler.updateUI(true, null, 0);
                }
            }
        });


       return true;
    }

    /**
     *
     * get all the challenge rooms of a specific field and value and pass the result back to the calling class
     *
     * @param field
     * @param targetValue
     * @param handler
     * @return
     */
    public boolean getChallengeRooms(String field, String targetValue, OnRoomGetCompletionHandler handler){

        db.collection("challenges")
                .whereEqualTo(field, targetValue)
                .whereEqualTo("started", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            Map<String, ChallengeRoom> challengeRooms = new HashMap<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " yay => " + document.getData());

                                ChallengeRoom c = document.toObject(ChallengeRoom.class);
                                challengeRooms.put(document.getId(), c);

                            }
                            handler.challengeRoomsTransfer(challengeRooms);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


        return true;
    }

    /**
     *
     * get all my challenges and pass the result back to the calling class
     *
     * @param handler
     * @return
     */
    public boolean getMyChallenges(OnRoomGetCompletionHandler handler){


        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount account = UserAccount.getInstance();

        db.collection("challenges")
                .whereArrayContains("participants", new Participant(account.getName(),user.getUid()))
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

        return true;
    }

    /**
     *
     * quit the challenge of a given room ID and update the calling class's UI accordingly
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean quitChallenge(String roomID, UIUpdateCompletionHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount account = UserAccount.getInstance();

        WriteBatch batch = db.batch();

        DocumentReference challengeRef = db.collection("challenges").document(roomID);

        // add the new challenge room data to firestore first
        batch.update(challengeRef, "participants", FieldValue.arrayRemove(new Participant(account.getName(),user.getUid())));

        DocumentReference dataRef = db.collection("challenges").document(roomID).collection("stats").document(user.getUid());

        batch.delete(dataRef);

        DocumentReference userAccountRef = db.collection("users").document(user.getUid());

        // update the current user to have this new challenge room
        batch.update(userAccountRef, "challengesJoined", FieldValue.arrayRemove(roomID));

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    account.removeChallenge(roomID);
                    handler.updateUI(true, null, 0);
                    cleanUpEmptyChallenges(roomID);
                }
            }
        });

        return true;
    }

    /**
     *
     * listen for coin changes in order to update the calling class's coin UI in real time
     *
     * @param handler
     * @return
     */
    public boolean startCoinChangeListener(UIUpdateCompletionHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        UserAccount userAccount = UserAccount.getInstance();
        coinChangeListener = db.collection("users").document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(value.exists()){
                                userAccount.setCoin(Integer.parseInt(value.getData().get("coins").toString()));
                                handler.updateUI(true, null, 1);

                            }else{
                                Log.d(TAG, "No such document");
                                handler.updateUI(false, null, 1);

                            }
                    }
                });


        return true;
    }

    public void removeCoinChangeListener(){

        if(coinChangeListener!=null)
        coinChangeListener.remove();

    }

    /**
     *
     * update a participant's challenge stats
     *
     * @param roomID
     * @param newStats
     * @param type
     * @return
     */
    public boolean updateChallengeStats(String roomID, float newStats, ChallengeType type){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference challengeRef = db.collection("challenges").document(roomID);
        final DocumentReference challengeStatsRef = db.collection("challenges").document(roomID).collection("stats").document(user.getUid());

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(challengeRef);

                // Note: this could be done without a transaction
                //       by updating the population using FieldValue.increment()
                boolean started = snapshot.getBoolean("started");

                if(started){

                    if(type == ChallengeType.DISTANCE){
                        transaction.update(challengeStatsRef, "distance", newStats);
                    }else if(type == ChallengeType.WEIGHTLOSS){
                        transaction.update(challengeStatsRef, "weight", newStats);
                    }


                }

                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });

        return true;
    }

    /**
     *
     * get the leader board stats of a given challenge room ID and pass the info back to the calling class
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean getLeaderBoardStats(String roomID, OnLeaderBoardStatsGetCompletionHandler handler){

        db.collection("challenges").document(roomID).collection("stats").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            ArrayList<ChallengeStats> challengeStatsArrayList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                               ChallengeStats challengeStats = document.toObject(ChallengeStats.class);
                                challengeStatsArrayList.add(challengeStats);
                            }
                            handler.statsTransfer(challengeStatsArrayList);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                    });


        return true;
    }

    /**
     *
     * listen to real time challenge stats changes for a given challenge
     *
     * @param roomID
     * @param type
     * @param handler
     * @return
     */
    public boolean startStatsChangeListener(String roomID, ChallengeType type, UIUpdateCompletionHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        statsChangeListener = db.collection("challenges").document(roomID).collection("stats").document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value.exists()){
                            Map<String,String> data = new HashMap<>();
                            switch (type){

                                case DISTANCE:
                                    data.put("distance", value.get("distance").toString());
                                    break;
                                case WEIGHTLOSS:
                                    data.put("weight", value.get("weight").toString());
                                    break;
                                default:
                                    System.out.println("No such challenge type !");
                                    return;
                            }
                            handler.updateUI(true, data, 0);

                        }else{
                            Log.d(TAG, "No such document");
                            handler.updateUI(false, null, 0);

                        }
                    }

                });



      return true;
    }

    public void removeStatsChangeListener(){

        if(statsChangeListener!=null)
        statsChangeListener.remove();

    }

    /**
     *
     * garbage collect all empty challenges
     *
     * @param roomID
     * @return
     */
    public boolean cleanUpEmptyChallenges(String roomID){

        final DocumentReference challengeRef = db.collection("challenges").document(roomID);

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(challengeRef);

                // Note: this could be done without a transaction
                //       by updating the population using FieldValue.increment()
                ArrayList<Participant> participantArrayList = (ArrayList<Participant>) snapshot.get("participants");

                if(participantArrayList.size()==0){
                    transaction.delete(challengeRef);
                }

                // Success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Transaction failure.", e);
                    }
                });




        return true;
    }

    /**
     *
     * upload the weight images to firebase storage
     *
     * @param ref
     * @param metadata
     * @param data
     * @param handler
     * @return
     */
    public boolean uploadImg(StorageReference ref, StorageMetadata metadata, byte[] data, UIUpdateCompletionHandler handler){

        UploadTask uploadTask = ref.putBytes(data, metadata);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                handler.updateUI(false,null, 0);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                StorageMetadata metadata1 = taskSnapshot.getMetadata();
                Map<String,String> data = new HashMap<>();
                data.put("imgPath", metadata1.getReference().getPath());
                data.put("weight", metadata1.getCustomMetadata("weight"));
                handler.updateUI(true, data, 0);
            }
        });

        return true;
    }

    /**
     *
     * set the weight prompt the participant of the given challenge room ID
     *
     * @param roomID
     * @return
     */
    public boolean setWeightPrompt(String roomID){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        db.collection("challenges").document(roomID).collection("stats").document(user.getUid())
                .update("weightPrompt", true);

        return true;
    }

    /**
     *
     * check to see if the participant should be prompted to take and submit a new weight image or not
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean checkWeightPrompt(String roomID, OnBooleanPromptHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        db.collection("challenges").document(roomID).collection("stats").document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            Boolean weightPrompt = task.getResult().getBoolean("weightPrompt");
                            handler.passBoolean(weightPrompt);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return true;
    }

    /**
     *
     * check to see if the participant has already bet or not
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean checkHasBet(String roomID, OnPlaceBetHandler handler){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        final DocumentReference statsRef = db.collection("challenges").document(roomID).collection("stats").document(user.getUid());

        statsRef
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            Boolean hasBet = task.getResult().getBoolean("hasBet");
                            handler.placeBet(hasBet);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        return true;
    }

    /**
     *
     * check the result of a given challenge and pass the result back to the calling class
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean checkChallengeResult(String roomID, OnDataPassHandler handler){

        db.collection("challenges").document(roomID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String,String> data = new HashMap<>();
                            data.put("betAmount", task.getResult().get("betAmount").toString());
                            data.put("pot", task.getResult().get("pot").toString());
                            data.put("winner", task.getResult().getString("winner"));
                            handler.passData(data);

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        return true;
    }

    /**
     *
     * testing method to assign random distances to distance challenge participants (for GRADER)
     *
     * @param roomID
     * @return
     */
    public boolean assignRandomDIstance(String roomID){

        db.collection("challenges").document(roomID).collection("stats").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());

                                Random rand = new Random();
                                db.collection("challenges").document(roomID).
                                        collection("stats").document(document.getId()).update("distance", rand.nextInt(200));

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        return true;
    }

    /**
     *
     * listen for end challenge notification in order to direct the participant to the challenge end screen
     *
     * @param roomID
     * @param handler
     * @return
     */
    public boolean startChallengeStatusListener(String roomID, OnBooleanPromptHandler handler){

        challengeStatusListener = db.collection("challenges").document(roomID).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value.exists()){
                    Boolean started = value.getBoolean("started");
                    handler.passBoolean(started);
                }else{
                    Log.d(TAG, "No such document");

                }

            }
        });

        return true;
    }

    public void removeChallengeStatusListener(){

        if(challengeStatusListener!=null){
            Log.d(TAG, "Remove status change listener");
            challengeStatusListener.remove();
        }


    }

}
