package com.example.fitnessapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.WorkManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.auth.User;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.Participant;
import fitnessapp_objects.ParticipantModel;
import fitnessapp_objects.UserAccount;
import fitnessapp_objects.WorkManagerAPI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * this class represents the challenge lobby which shows the participants
 */
public class ChallengeLobbyActivity extends AppCompatActivity implements Database.OnRoomChangeListener, Database.UIUpdateCompletionHandler, Database.OnBooleanPromptHandler, Database.OnPlaceBetHandler {

    private final int MY_PERMISSIONS_REQUEST_ACTIVITY = 2;
    private long BTN_DISABLE_TIME = 5000;
    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";
    private final OkHttpClient httpClient = new OkHttpClient();
    private Button beginChallBTN;
    private TextView roomNameTV, beginInfoTV;
    private GridView participants_view;
    private ArrayList<ParticipantModel> participantModelArrayList;
    private ParticipantGVAdapter adapter;
    private String roomID, type;
    private boolean weightPrompt = true;
    private long endDate;
    private HashMap<String, String> challengeInfo;
    private Database db;
    private WorkManagerAPI workManagerAPI;
    private int betAmount = 0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_lobby);

        roomNameTV = (TextView) findViewById(R.id.room_name_info_tv);
        beginInfoTV = (TextView) findViewById(R.id.begin_info_tv);
        participants_view = (GridView) findViewById(R.id.participant_grid);
        beginChallBTN = (Button) findViewById(R.id.begin_chall_btn);
        participantModelArrayList = new ArrayList<>();

        workManagerAPI = WorkManagerAPI.getInstance();
        adapter = new ParticipantGVAdapter(this, participantModelArrayList);
        participants_view.setAdapter(adapter);

        //noinspection unchecked
        challengeInfo = (HashMap<String, String>) getIntent().getSerializableExtra("challengeInfo");
        betAmount = Integer.parseInt(challengeInfo.getOrDefault("betAmount", "0"));
        type = challengeInfo.getOrDefault("type", ChallengeType.DISTANCE.toString());
        endDate = getIntent().getLongExtra("endDate", 0);
        roomID = challengeInfo.get("roomID");

        roomNameTV.setText(challengeInfo.getOrDefault("name", "un-named"));

        db = Database.getInstance();
        checkSensitiveDataAccessPermission();

    }

    @Override
    protected void onStart() {
        super.onStart();
        db.startChallengeRoomChangeListener(roomID, this);
        db.startCoinChangeListener(this);
        beginChallBTN.setEnabled(true);
    }


    @Override
    protected void onPause() {
        super.onPause();
        db.removeCoinChangeListener();
        db.detachChallengeRoomListener();
    }

    /**
     *
     * update the grid view for new participants
     *
     * @param participants: new list of participants
     */
    public void modifyParticipant(ArrayList<Participant> participants) {

        participantModelArrayList.clear();

        for (Participant p : participants) {

            participantModelArrayList.add(new ParticipantModel(p.getName(), p.getId()));

        }

        adapter.notifyDataSetChanged();


    }

    /**
     * begin the challenge
     */
    public void beginChallenge(View view) {

        beginChallBTN.setEnabled(false);

        switch (type) {
            case "DISTANCE":
                if (betAmount > 0) {
                    db.checkHasBet(roomID, this);
                }else{
                    goToDistance();
                }
                break;
            case "WEIGHTLOSS":
                db.checkWeightPrompt(roomID, this);
                return;
            default:
                beginChallBTN.setEnabled(true);
                System.out.println("No such challenge !");
                return;

        }

    }

    /**
     * go and view the challenge info
     */
    public void challengeInfo(View view) {

        Intent intent = new Intent(this, ChallengeInfoActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        startActivity(intent);

    }

    /**
     * quit the challenge
     */
    public void quit(View view) {

        db.quitChallenge(roomID, this);

    }


    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data, int callbackCode) {

        if(isSuccess){

            switch(callbackCode){

                case 0:
                    workManagerAPI.cancelAllTask(WorkManager.getInstance(this), roomID);
                    workManagerAPI.viewAllWork(WorkManager.getInstance(this), roomID);
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    break;

            }


        }

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void checkSensitiveDataAccessPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY);
        } else {
            Toast.makeText(ChallengeLobbyActivity.this, " Successfully granted permissions for ACTIVITY_RECOGNITION and ACCESS_FINE_LOCATION!",
                    Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACTIVITY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Toast.makeText(ChallengeLobbyActivity.this, " Successfully granted permissions for ACTIVITY_RECOGNITION and ACCESS_FINE_LOCATION!",
                            Toast.LENGTH_SHORT).show();

                } else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    AlertDialog.Builder a = new AlertDialog.Builder(this);
                    a.setMessage("You won't be able to do Challenges without given permission!");
//                    a.setPositiveButton("Yes, please.", (dialog, which) -> {
//                        startActivityForResult(new Intent(Settings.ACTION_PRIVACY_SETTINGS), 0);
//                    });
                    a.setNegativeButton("Okay!", (dialog, which) -> {

                    });
                    a.show();

                }
                return;
        }

    }

    /**
     *
     * place the initial bet for the user
     *
     * @param place: indicated whether the user has already placed a bet or not
     */
    public void placeBet(boolean place) {

        // if the user doesn't have enough coin to bet
        if(UserAccount.getInstance().getCoin()<betAmount && !place){
            beginInfoTV.setText(getString(R.string.notEnoughCoin));
            beginChallBTN.setEnabled(true);
            return;
        }

        // if the user has already placed the bet, then simply go to the
        // corresponding screen
        if (place || betAmount<=0) {
            goToScreen();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        String json = "{"
                + "\"betAmount\":" + betAmount + ","
                + "\"userID\":" + "\"" + user.getUid() + "\","
                + "\"roomID\":" + "\"" + roomID + "\""
                + "}";
        System.out.println(json);
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "place-bet")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new ChallengeLobbyActivity.PayCallback(this));
    }

    /**
     *
     * check if the weight challenge user needs the enter weight prompt again
     *
     * @param weightPrompt: indicate whether the user needs to input new weight img or not
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void passBoolean(boolean weightPrompt) {


        this.weightPrompt = weightPrompt;
        if (betAmount > 0) {
            db.checkHasBet(roomID, this);
        } else {
            goToWeightScreen();
        }


    }

    /**
     * PayCallback for the request to our backend server
     */
    private static final class PayCallback implements Callback {
        @NonNull
        private final WeakReference<ChallengeLobbyActivity> activityRef;

        PayCallback(@NonNull ChallengeLobbyActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final ChallengeLobbyActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(() ->
                    Toast.makeText(
                            activity, "Error: " + e.toString(), Toast.LENGTH_LONG
                    ).show()
            );
            activity.runOnUiThread(() ->
                    activity.beginChallBTN.setEnabled(true)
            );
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final ChallengeLobbyActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                activity.runOnUiThread(() ->
                        Toast.makeText(
                                activity, "Error: " + response.toString(), Toast.LENGTH_LONG
                        ).show()
                );
                activity.runOnUiThread(() ->
                        activity.beginChallBTN.setEnabled(true)
                        );


            } else {
                UserAccount.getInstance().bet(activity.betAmount);
                activity.goToScreen();
            }
        }
    }

    /**
     * direct the user to the correct challenge screen
     */
    public void goToScreen(){

        switch(type){

            case "DISTANCE":
                goToDistance();
                break;
            case "WEIGHTLOSS":
                goToWeightScreen();
                break;
        }



    }

    /**
     * go the to distance challenge main screen
     */
    public void goToDistance() {

        Intent intent = new Intent(this, DistanceChallengeActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        intent.putExtra("endDate", endDate);
        startActivity(intent);
    }

    /**
     * go to the weight loss challenge main screen
     */
    public void goToWeightScreen() {
        Intent intent;
        if (weightPrompt) {
            intent = new Intent(this, WeightLossChallengeInitActivity.class);
        } else {
            intent = new Intent(this, WeightChallengeActivity.class);
        }
        intent.putExtra("challengeInfo", challengeInfo);
        intent.putExtra("endDate", endDate);
        startActivity(intent);

    }
}