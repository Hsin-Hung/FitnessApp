package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Map;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeRoomModel;
import fitnessapp_objects.Database;

public class JoinChallengeActivity extends AppCompatActivity implements Database.FirestoreCompletionHandler, AdapterView.OnItemClickListener,
        PasswordDialogFragment.PasswordDialogListener, Database.UIUpdateCompletionHandler, SearchView.OnQueryTextListener {

    SearchView challengeSV;
    ListView challengesLV;
    ArrayList<ChallengeRoomModel> challengeRoomModelArrayList;
    ChallengeRoomLVAdapter adapter;
    Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_challenge);

        challengeSV = (SearchView) findViewById(R.id.chall_room_searchView);
        challengesLV = (ListView) findViewById(R.id.rooms_view);

        challengeRoomModelArrayList = new ArrayList<>();

        adapter = new ChallengeRoomLVAdapter(this, challengeRoomModelArrayList);

        challengesLV.setAdapter(adapter);

        db = Database.getInstance();

        challengesLV.setOnItemClickListener(this);
        challengeSV.setOnQueryTextListener(this);


    }

    public void randomChallenges(View view){

        db.getPendingChallengeRooms(this);

    }

    @Override
    public void challengeRoomsTransfer(Map<String,ChallengeRoom> rooms) {

        challengeRoomModelArrayList.clear();

        for(Map.Entry<String,ChallengeRoom> entry : rooms.entrySet()){

            ChallengeRoom room = entry.getValue();

            ChallengeRoomModel model = new ChallengeRoomModel(entry.getKey(), room.getName(),room.getType(),room.isBet(), room.getBetAmount(),room.getEndDate(), room.getPassword());
            challengeRoomModelArrayList.add(model);

        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ChallengeRoomModel challengeRoomModel = challengeRoomModelArrayList.get(position);
        DialogFragment dialog = new PasswordDialogFragment(challengeRoomModel.getId(), challengeRoomModel.getPassword());
        dialog.show(getSupportFragmentManager(), "PasswordDialogFragment");

    }

    @Override
    public void onDialogPositiveClick(String roomID, boolean success) {

        if(success){

            db.joinChallengeRoom(roomID, this);


        }

    }


    @Override
    public void updateUI(boolean isSuccess, Map<String,String> data) {
        if(isSuccess){

            Intent intent = new Intent(this, ChallengeLobbyActivity.class);
            intent.putExtra("roomID", data.get("roomID"));
            startActivity(intent);

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        db.getChallengeRooms(query, this);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {


        return false;
    }
}