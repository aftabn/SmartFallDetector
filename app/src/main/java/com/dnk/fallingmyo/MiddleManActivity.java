package com.dnk.fallingmyo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MiddleManActivity extends ActionBarActivity {

    private Firebase authorizedPatientReference;
    private MiddleManActivity me = this;
    private String accountID;

    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_middle_man);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            String accountID = extras.getString("accountID");
            checkIdAndLaunchActivity(accountID, this);
        }
    }

    public void checkIdAndLaunchActivity(String id, Context context)
    {
         accountID = id;

        Firebase.setAndroidContext(context);
        authorizedPatientReference = new Firebase("https://fallingmyo.firebaseIO.com/Authorized");
        authorizedPatientReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                Map<String, Object> patients = (HashMap<String, Object>) snapshot.getValue();

                List<Object> patientList = new ArrayList<Object>(patients.values());

                for (int i = 0; i < patientList.size(); i++) {

                    HashMap<String, Object> patientHash = (HashMap<String, Object>) patientList.get(i);
                    String patientAccountID = (String) patientHash.get("accountID");

                    if (patientAccountID.equals(accountID)) {
                        Log.d("OnDataChanged", "Patient found!");
                        Intent intent = new Intent(me, HomeScreenActivity.class);
                        intent.putExtra("accountID", accountID);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_middle_man, menu);
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
}
