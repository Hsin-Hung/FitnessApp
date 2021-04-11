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
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.participant_item, parent, false);
        }
        ChallengeRoomModel challengeRoomModel = getItem(position);
        TextView challengeRoomTV = listitemView.findViewById(R.id.room_name_tv);
        challengeRoomTV.setText(challengeRoomModel.getName());
        return listitemView;
    }
    
    
}
