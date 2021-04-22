package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fitnessapp_objects.ChallengeStats;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.ParticipantModel;
import fitnessapp_objects.WorkManagerAPI;

/**
 * This class represent the end result screen after a challenge ends. It shows whether the user wins or loses, the final leaderboard, and how much coins they have gained or lost.
 */
public class ChallengeEndActivity extends AppCompatActivity implements Database.OnLeaderBoardStatsGetCompletionHandler, Database.UIUpdateCompletionHandler, Database.OnDataPassHandler {

    private ListView leaderBoardLV;
    private TextView challengeResultTV, coinResultTV;

    private ArrayList<ParticipantModel> participantModels;
    private LeaderBoardParticipantLVAdapter adapter;

    private HashMap<String,String> challengeInfo;
    private Database db;
    private String type, roomID;
    private WorkManagerAPI workManagerAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_end);

        leaderBoardLV = (ListView) findViewById(R.id.lb_result_lv);
        challengeResultTV = (TextView) findViewById(R.id.chall_result_tv);
        coinResultTV = (TextView) findViewById(R.id.coin_res_tv);
        participantModels = new ArrayList<>();
        adapter = new LeaderBoardParticipantLVAdapter(this, participantModels);
        challengeInfo = (HashMap<String,String>) getIntent().getSerializableExtra("challengeInfo");
        type = challengeInfo.get("type");
        roomID = challengeInfo.get("roomID");
        db = Database.getInstance();
        workManagerAPI = WorkManagerAPI.getInstance();
        leaderBoardLV.setAdapter(adapter);
        db.getLeaderBoardStats(challengeInfo.get("roomID"), this);
        db.checkChallengeResult(roomID, this);
    }

    /**
     * exit the challenge
     */
    public void exit(View view){

        db.quitChallenge(roomID, this);

    }

    /**
     *
     * handler that is used to retrieve leaderboard data from database
     *
     * @param stats: leader board stats that are passed back from the database
     */
    @Override
    public void statsTransfer(ArrayList<ChallengeStats> stats) {

        participantModels.clear();

        switch (type){
            case "DISTANCE":
                for(ChallengeStats challengeStats: stats){

                    participantModels.add(new ParticipantModel(challengeStats.getName(), challengeStats.getId(), challengeStats.getDistance(), ChallengeType.DISTANCE));

                }
                Collections.sort(participantModels, new Comparator<ParticipantModel>() {
                    @Override
                    public int compare(ParticipantModel o1, ParticipantModel o2) {
                        if(o1.getDistance() > o2.getDistance())return -1;
                        return 1;
                    }
                });
                break;
            case "WEIGHTLOSS":
                for(ChallengeStats challengeStats: stats){
                    ParticipantModel participantModel = new ParticipantModel(challengeStats.getName(), challengeStats.getId(), ChallengeType.WEIGHTLOSS, challengeStats.getWeight());
                    participantModel.setInitWeight(challengeStats.getInitWeight());
                    participantModels.add(participantModel);

                }
                Collections.sort(participantModels, new Comparator<ParticipantModel>() {
                    @Override
                    public int compare(ParticipantModel o1, ParticipantModel o2) {
                        if(o1.getWeightDiff() > o2.getWeightDiff())return -1;
                        return 1;
                    }
                });
                break;
        }

        adapter.notifyDataSetChanged();


    }

    /**
     * go back to lobby screen
     */
    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data, int callbackCode) {

        if(isSuccess){
            workManagerAPI.cancelAllTask(WorkManager.getInstance(this), roomID);
            workManagerAPI.viewAllWork(WorkManager.getInstance(this), roomID);
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

    }



    @Override
    public void onBackPressed() {

    }

    /**
     * handler to pass the win or lose result from database
     */
    @Override
    public void passData(Map<String, String> data) {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        boolean isWin = data.get("winner").equals(user.getUid());
        String pot = data.get("pot"), betAmount = data.get("betAmount");
        if(isWin){

            challengeResultTV.setText(getString(R.string.youWin));
            challengeResultTV.setTextColor(Color.GREEN);
            String coinResult = "you've gained " + pot + " coins";
            coinResultTV.setText(coinResult);

        }else{

            challengeResultTV.setText(getString(R.string.youLose));
            challengeResultTV.setTextColor(Color.RED);
            String coinResult = "you've lost " + betAmount + " coins";
            coinResultTV.setText(coinResult);

        }


    }
}