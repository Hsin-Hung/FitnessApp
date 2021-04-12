package com.example.fitnessapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.firebase.Timestamp;
import com.stripe.android.model.Card;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import fitnessapp_objects.ChallengeRoom;
import fitnessapp_objects.ChallengeRoomModel;
import fitnessapp_objects.ChallengeType;
import fitnessapp_objects.Database;
import fitnessapp_objects.FirestoreCompletionHandler;

public class JoinChallengeActivity extends AppCompatActivity implements FirestoreCompletionHandler, AdapterView.OnItemClickListener, PasswordDialogFragment.PasswordDialogListener {

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

        String roomPassword = challengeRoomModelArrayList.get(position).getPassword();

        DialogFragment dialog = new PasswordDialogFragment(roomPassword);
        dialog.show(getSupportFragmentManager(), "PasswordDialogFragment");



    }

    @Override
    public void onDialogPositiveClick(boolean success) {


        System.out.println(success);

    }
}