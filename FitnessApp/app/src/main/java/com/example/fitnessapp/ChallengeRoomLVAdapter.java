package com.example.fitnessapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import fitnessapp_objects.ChallengeRoomModel;

public class ChallengeRoomLVAdapter extends ArrayAdapter<ChallengeRoomModel> {

    public ChallengeRoomLVAdapter(@NonNull Context context, ArrayList<ChallengeRoomModel> challengeRoomArrayList) {
        super(context, 0, challengeRoomArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.challenge_item, parent, false);
        }
        ChallengeRoomModel challengeRoomModel = getItem(position);
        TextView challengeRoomTV = listitemView.findViewById(R.id.room_name_tv),
                challengeTypeTV = listitemView.findViewById(R.id.chall_type_tv),
                betTV = listitemView.findViewById(R.id.bet_tv),
                endDateTV = listitemView.findViewById(R.id.end_date_tv);

        challengeRoomTV.setText(challengeRoomModel.getName());
        challengeTypeTV.setText(challengeRoomModel.getType().toString());
        betTV.setText("bet amount: " + String.valueOf(challengeRoomModel.getBetAmount()));
        endDateTV.setText("end date: "+ challengeRoomModel.getEndDate().toDate().toString());

        return listitemView;
    }
    
    
}
