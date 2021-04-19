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
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fitnessapp_objects.Database;
import fitnessapp_objects.Participant;
import fitnessapp_objects.ParticipantModel;
import fitnessapp_objects.WorkManagerAPI;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChallengeLobbyActivity extends AppCompatActivity implements Database.OnRoomChangeListener, Database.UIUpdateCompletionHandler, Database.OnBooleanPromptHandler, Database.OnPlaceBetHandler {


    private static final String BACKEND_URL = "https://fitnessapp501.herokuapp.com/";
    private OkHttpClient httpClient = new OkHttpClient();
    final int MY_PERMISSIONS_REQUEST_ACTIVITY = 2;
    TextView roomNameTV;
    GridView participants_view;
    ArrayList<ParticipantModel> participantModelArrayList;
    ParticipantGVAdapter adapter;
    String roomID;
    String type;
    boolean weightPrompt = true;
    long endDate;
    HashMap<String,String> challengeInfo;
    Database db;
    WorkManagerAPI workManagerAPI;
    int betAmount = 0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_lobby);

        roomNameTV = (TextView) findViewById(R.id.room_name_info_tv);
        participants_view = (GridView) findViewById(R.id.participant_grid);

        participantModelArrayList = new ArrayList<>();
        workManagerAPI = WorkManagerAPI.getInstance();
        adapter = new ParticipantGVAdapter(this, participantModelArrayList);
        participants_view.setAdapter(adapter);
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");

        betAmount = Integer.parseInt(challengeInfo.getOrDefault("betAmount","0"));
        type = challengeInfo.get("type");

        endDate = getIntent().getLongExtra("endDate", 0);
        roomNameTV.setText(challengeInfo.get("name"));

        roomID = challengeInfo.get("roomID");
        db = Database.getInstance();
        db.startChallengeRoomChangeListener(roomID, this);
        checkSensitiveDataAccessPermission();

    }

    @Override
    protected void onDestroy() {
        db.detachChallengeRoomListener();

        super.onDestroy();
    }

    public void modifyParticipant(ArrayList<Participant> participants){

        participantModelArrayList.clear();

        for(Participant p: participants){

           participantModelArrayList.add(new ParticipantModel(p.getName(), p.getId()));

        }

        adapter.notifyDataSetChanged();


    }

    public void beginChallenge(View view){
        Intent intent;
        System.out.println(challengeInfo.get("type"));
        switch(challengeInfo.get("type")){

            case "DISTANCE":
                if(betAmount>0){
                  db.checkHasBet(roomID, this);
                  return;
                }
                intent = new Intent(this, DistanceChallengeActivity.class);
                break;
            case "WEIGHTLOSS":
                db.checkWeightPrompt(roomID, this);
                return;
            default:
                System.out.println("No such challenge !");
                return;

        }
        intent.putExtra("challengeInfo", challengeInfo);
        intent.putExtra("endDate",endDate);
        startActivity(intent);

    }

    public void challengeInfo(View view){

        Intent intent = new Intent(this, ChallengeInfoActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        startActivity(intent);

    }


    public void quit(View view){

        db.quitChallenge(roomID, this);

    }


    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {

        workManagerAPI.cancelAllTask(WorkManager.getInstance(this), roomID);
        workManagerAPI.viewAllWork(WorkManager.getInstance(this), roomID);
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void checkSensitiveDataAccessPermission(){

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACTIVITY);
        }else{
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
                        onBackPressed();
                    });
                    a.show();

                }
                return;
        }

    }


    public void goToWeightScreen() {
        Intent intent;
        if(weightPrompt){
            intent = new Intent(this, WeightLossChallengeInitActivity.class);
        }else{
            intent = new Intent(this, WeightChallengeActivity.class);
        }
        intent.putExtra("challengeInfo", challengeInfo);
        intent.putExtra("endDate",endDate);
        startActivity(intent);

    }

    public void placeBet(boolean place){

        if(place){
            if(type.equals("DISTANCE")){
                goToDistance();
            }else{
                goToWeightScreen();
            }

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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void passBoolean(boolean weightPrompt) {


                this.weightPrompt = weightPrompt;
                if(betAmount>0){
                    db.checkHasBet(roomID, this);
                }else{
                    goToWeightScreen();
                }


    }

    /**
     * PayCallback for the request to our backend server
     */
    private static final class PayCallback implements Callback {
        @NonNull private final WeakReference<ChallengeLobbyActivity> activityRef;

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
            } else {
                if(activity.type.equals("DISTANCE")){
                    activity.goToDistance();
                }else{
                    activity.goToWeightScreen();
                }

            }
        }
    }

    public void goToDistance(){

        Intent intent = new Intent(this, DistanceChallengeActivity.class);
        intent.putExtra("challengeInfo", challengeInfo);
        intent.putExtra("endDate",endDate);
        startActivity(intent);
    }
}