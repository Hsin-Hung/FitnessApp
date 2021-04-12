package com.example.fitnessapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class JoinChallengeActivity extends AppCompatActivity {

    private ListView rooms_view;     //Reference to the listview GUI component
    private ListAdapter room_adaptor;   //Reference to the Adapter used to populate the listview.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_challenge);

        rooms_view = findViewById(R.id.rooms_view);
        room_adaptor = new ChallengeRoomAdapter(getApplicationContext());
        rooms_view.setAdapter(room_adaptor);
    }

    class ChallengeRoomAdapter extends BaseAdapter {

        private
        String[] title_array;
        String[] descriptions_array;

        Context context;

        public ChallengeRoomAdapter(Context aContext) {
            context = aContext;
            title_array = new String[]{"title 1", "title 2"};
            descriptions_array = new String[]{"Description 1", "Description 2"};
        }


        @Override
        public int getCount() {
            return title_array.length;
        }

        @Override
        public Object getItem(int position) {
            return title_array[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row;

            if (convertView == null){  //indicates this is the first time we are creating this row.
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  //convert xml to Java Objects
                row = inflater.inflate(R.layout.listview_row, parent, false);
            }
            else
            {
                row = convertView;
            }

            TextView title_tv = row.findViewById(R.id.title_tv);
            TextView description_tv = row.findViewById(R.id.description_tv);

            title_tv.setText(title_array[position]);
            description_tv.setText(descriptions_array[position]);


            return row;


        }
}}