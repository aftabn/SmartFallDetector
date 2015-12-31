package com.dnk.fallingmyo;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Vector3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
public class BackgroundService extends Service {
    private Firebase authorizedPatientReference;
    private static final String TAG = "BackgroundService";
    private static final String MYO = "MyoStatus";
    private static final String ACCEL = "AccelValue";
    private static final String GYRO = "GyroVal";
    private int accelCount;
    private String accountID;
    private double accelVal;
    private int sampleCount = 0;
    private int currentMax;
    private boolean patientRequiresHelp;
    private boolean startCheckingMyo = false;
    private boolean startCheckingGyro = false;
    private boolean isFirstValue = true;
    private int ignoreCount = 0;
    private Vector3 firstGyroSample;
    private String patientKey;
    private Patient patient;


    // Classes that inherit from AbstractDeviceListener can be used to receive events from Myo devices.
    // If you do not override an event, the default behavior is to do nothing.
    private DeviceListener mListener = new AbstractDeviceListener() {

        @Override
        public void onConnect(Myo myo, long timestamp) {
            Log.d(MYO, "Connected to Myo!");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            Log.d(MYO, "Disconnected from Myo!");
        }
        // onPose() is called whenever the Myo detects that the person wearing it has changed their pose, for example,
        // making a fist, or not making a fist anymore.

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Show the name of the pose in a toast.
            Log.d(MYO, "Just initiated " + pose.toString());
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            //TODO: read accel.length() and update db "help" field of patient
            if (startCheckingMyo) {
                accelVal = accel.length();
                Log.d(ACCEL, "AccelValue is " + accelVal);
                if (accelVal > 2) {
                    Log.d("onAccelerometerData", "Passed threshold of 2. Val = " + accel.length());
                    if (!startCheckingGyro)
                    {
                        startCheckingGyro = true;
                        sampleCount = 0;
                        ignoreCount = 0;
                    }
                    /*final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 10s = 10000ms
                            //TODO: check if person not moving
                            // IE accelValue is < 1.5
                        }
                    }, 10000);*/
                    //TODO: update fire base with value of patientRequiresHelp
                }
            }
        }

        @Override
        public void onGyroscopeData (Myo myo, long timestamp, Vector3 gyro) {
            if (startCheckingGyro)
            {
                if(ignoreCount < 150)
                {
                    ignoreCount++;
                    return;
                }

                if (isFirstValue)
                {
                    firstGyroSample = gyro;
                    isFirstValue = false;
                }
                else if (sampleCount < 200)
                {
                    if (ifVectorsWithinRange(firstGyroSample, gyro, 10))
                    {
                        sampleCount++;
                        Log.d(GYRO, "Gyro: " + gyro);
                        return;
                    }
                    else
                    {
                        startCheckingGyro = false;
                    }
                }
                else
                {
                    setEmergencyTrue();
                    startCheckingGyro = false;
                }
            }
        }
    };

    private void setEmergencyTrue()
    {
        Log.d("setEmergencyTrue", "Setting emergency to true");
        patient.setEmergency(true);
        authorizedPatientReference.child(patientKey).setValue(patient);
    }

    public void createPatient(Context context)
    {

        Firebase.setAndroidContext(context);
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

    private boolean ifVectorsWithinRange(Vector3 v1, Vector3 v2, double range)
    {
        Vector3 differenceVector = new Vector3();
        differenceVector.set(v1);
        differenceVector.subtract(v2);
        double x = Math.abs(differenceVector.x());
        double y = Math.abs(differenceVector.x());
        double z = Math.abs(differenceVector.x());
        Log.d("ifVectorsWithinRange", "values: " + x + ", " + y + ", " + z);
        if (x < range && y < range && z < range)
        {
            return true;
        }
        return false;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
        if (extras != null){
            accountID = extras.getString("accountID");
        }

        Log.d("onStartCommand", "Entered service. Id is " + accountID);

        createPatient(this);
        Log.d("onStartCommand", "Created patient within service");
        startCheckingMyo = true;
        startCheckingGyro = true;

        return Service.START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        accelCount = 0;
        currentMax = 0;
        accelVal = 0;
        patientRequiresHelp = false;
        //FallFollowUpTimerTask timerTask = new FallFollowUpTimerTask();
        //Timer followUpTimer = new Timer();
        //followUpTimer. schedule(timerTask, 3000, 500);
        super.onCreate();
        // First, we initialize the Hub singleton with an application identifier.
        Hub hub = Hub.getInstance();
        if (!hub.init(this, getPackageName())) {
            Log.d(MYO, "Couldn't initialize Hub");
            stopSelf();
            return;
        }

        // Disable standard Myo locking policy. All poses will be delivered.
        hub.setLockingPolicy(Hub.LockingPolicy.NONE);
        // Next, register for DeviceListener callbacks.
        hub.addListener(mListener);
        // Finally, scan for Myo devices and connect to the first one found that is very near.
        hub.attachToAdjacentMyo();
        Log.d("onCreateService", "Added listeners to the myo");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // We don't want any callbacks when the Service is gone, so unregister the listener.
        Hub.getInstance().removeListener(mListener);
        Hub.getInstance().shutdown();
    }

    public class FallFollowUpTimerTask extends TimerTask {
        public void run() {
        }
    }
}