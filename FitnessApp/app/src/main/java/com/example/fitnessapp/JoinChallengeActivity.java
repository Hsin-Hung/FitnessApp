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

public class JoinChallengeActivity extends AppCompatActivity implements Database.OnRoomGetCompletionHandler, AdapterView.OnItemClickListener,
        PasswordDialogFragment.PasswordDialogListener, Database.UIUpdateCompletionHandler, SearchView.OnQueryTextListener {

    private SearchView challengeSV;
    private ListView challengesLV;
    private ArrayList<ChallengeRoomModel> challengeRoomModelArrayList;
    private ChallengeRoomLVAdapter adapter;
    private Database db;
    private ChallengeRoomModel pickedChallengeRoom;

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

    /**
     * get random challenges
     */
    public void randomChallenges(View view) {

        db.getPendingChallengeRooms(this);

    }

    /**
     * update the list view with the challenge rooms fetched from the data base
     *
     * @param rooms
     */
    @Override
    public void challengeRoomsTransfer(Map<String, ChallengeRoom> rooms) {

        challengeRoomModelArrayList.clear();

        for (Map.Entry<String, ChallengeRoom> entry : rooms.entrySet()) {

            ChallengeRoom room = entry.getValue();

            ChallengeRoomModel model = new ChallengeRoomModel(entry.getKey(), room.getDescription(), room.getName(), room.getType(),
                    room.isBet(), room.getBetAmount(), room.getEndDate(), room.getPassword(), room.isStarted());
            challengeRoomModelArrayList.add(model);

        }

        adapter.notifyDataSetChanged();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        pickedChallengeRoom = challengeRoomModelArrayList.get(position);
        DialogFragment dialog = new PasswordDialogFragment(pickedChallengeRoom.getId(), pickedChallengeRoom.getPassword(), pickedChallengeRoom.getChallengeInfoMap());
        dialog.show(getSupportFragmentManager(), "PasswordDialogFragment");

    }

    @Override
    public void onDialogPositiveClick(Map<String, String> roomInfo, boolean success) {

        if (success) db.joinChallengeRoom(roomInfo.get("roomID"), this);

    }

    /**
     * go to the selected challenge
     */
    @Override
    public void updateUI(boolean isSuccess, Map<String, String> data) {
        if (isSuccess) {
            Intent intent = new Intent(this, ChallengeLobbyActivity.class);
            intent.putExtra("challengeInfo", pickedChallengeRoom.getChallengeInfoMap());
            intent.putExtra("endDate", pickedChallengeRoom.getEndDate().toDate().getTime());
            startActivity(intent);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        db.getChallengeRooms("name", query, this);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {


        return false;
    }
}