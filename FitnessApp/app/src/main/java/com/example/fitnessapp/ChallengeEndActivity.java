package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.WorkManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

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

public class ChallengeEndActivity extends AppCompatActivity implements Database.OnLeaderBoardStatsGetCompletionHandler, Database.UIUpdateCompletionHandler, Database.OnBooleanPromptHandler {

    private ListView leaderBoardLV;
    private TextView challengeResultTV;

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


    public void exit(View view){

        db.quitChallenge(roomID, this);

    }

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

                    participantModels.add(new ParticipantModel(challengeStats.getName(), challengeStats.getId(), ChallengeType.WEIGHTLOSS, challengeStats.getWeight()));

                }
                Collections.sort(participantModels, new Comparator<ParticipantModel>() {
                    @Override
                    public int compare(ParticipantModel o1, ParticipantModel o2) {
                        if(o1.getWeight() > o2.getWeight())return -1;
                        return 1;
                    }
                });
                break;
        }

        adapter.notifyDataSetChanged();


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
    public void passBoolean(boolean check) {

        if(check){

            challengeResultTV.setText(getString(R.string.youWin));
        }else{

            challengeResultTV.setText(getString(R.string.youLose));
        }
    }
}