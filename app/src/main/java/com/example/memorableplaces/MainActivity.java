package com.example.memorableplaces;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    
    static ArrayList<LatLng> locations;
    static ArrayList<String> places;
    static  ArrayAdapter arrayAdapter;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);

        places = new ArrayList<>();
        locations = new ArrayList<>();

        sharedPreferences = this.getSharedPreferences("com.example.memorableplaces" , Context.MODE_PRIVATE);


        ArrayList<String> latitude = new ArrayList<>();
        ArrayList<String> longitude = new ArrayList<>();

        places.clear();
        locations.clear();
        latitude.clear();
        longitude.clear();

        try{
            places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places" , ObjectSerializer.serialize(new ArrayList<String>())));

            latitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitude" , ObjectSerializer.serialize(new ArrayList<>())));

            longitude = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitude" , ObjectSerializer.serialize(new ArrayList<String>())));


 //            locations = (ArrayList<LatLng>) ObjectSerializer.deserialize(sharedPreferences.getString("location" , ObjectSerializer.serialize(new ArrayList<LatLng>())));
        } catch (Exception e){
            Log.e(TAG, "onCreate: error " + e.getMessage() );
        }


        if(places.size()>0 && latitude.size()>0 && longitude.size()>0){

            if(places.size() == latitude.size() && latitude.size() == longitude.size()){

                for(int i=0;i<places.size();i++){
                    locations.add(new LatLng(Double.parseDouble(latitude.get(i)) , Double.parseDouble(longitude.get(i))));
                }

            }

        }

        places.add(0 , "Add a new place...");
        locations.add(0 , new LatLng(0,0));


        Log.d(TAG, "onCreate: size " + places.size()+" "+locations.size());
        arrayAdapter = new ArrayAdapter(this , android.R.layout.simple_list_item_1 , places);




        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(MainActivity.this , " " + position , Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this , MapsActivity.class);
                Log.d(TAG, "onItemClick: position " + position);
                intent.putExtra("placeNumber" , position);
                startActivity(intent);
            }
        });
    }
}
