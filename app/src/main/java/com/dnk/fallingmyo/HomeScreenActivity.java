package com.dnk.fallingmyo;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeScreenActivity extends ActionBarActivity {

    private String patientResponse = "0";

    private String accountID;

    private Firebase authorizedPatientReference;

    private String patientKey;

    private Patient patient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // use this to start and trigger a service
        Intent backgroundServiceIntent = new Intent(this, BackgroundService.class);
        // potentially add data to the intent
        //TODO:
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            accountID = extras.getString("accountID");
            backgroundServiceIntent.putExtra("accountID", accountID);
        }
        HomeScreenActivity.this.startService(backgroundServiceIntent);

        Firebase.setAndroidContext(this);
            authorizedPatientReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");

            authorizedPatientReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                Map<String, Object> patients = (HashMap<String, Object>) snapshot.getValue();

                Set<String> keys = patients.keySet();

                for (String key : keys) {
                    HashMap<String, Object> patientHashMap = (HashMap<String, Object>) patients.get(key);
                    if (((String) patientHashMap.get("accountID")).equals(accountID)) {
                        patientKey = key;
                        patient = new Patient(
                                (String) patientHashMap.get("name"),
                                (String) patientHashMap.get("accountID"),
                                (String) patientHashMap.get("macAddress"),
                                (String) patientHashMap.get("email"),
                                (String) patientHashMap.get("roomNumber"),
                                (boolean) patientHashMap.get("emergency"),
                                Assistance.valueOf((String) patientHashMap.get("assistanceRequired")),
                                (boolean) patientHashMap.get("disable"));
                    }
                }
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void clickedHelp(View view) {
        patientResponse = "HELP";
        patient.setEmergency(true);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
    public void clickedSpeedDial(View view){
        //TODO: call number in phone
    }
    public void clickedMeds(View view){
        patientResponse = "1";
        patient.setAssistanceRequired(Assistance.Medication);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
    public void clickedFood(View view) {
        patientResponse = "2";
        patient.setAssistanceRequired(Assistance.Food);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
    public void clickedWashroom(View view){
        patientResponse = "3";
        patient.setAssistanceRequired(Assistance.Washroom);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
    public void clickedLinens(View view) {
        patientResponse = "4";
        patient.setAssistanceRequired(Assistance.Linens);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }
}