package com.dnk.fallingmyo;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;


public class LoginActivity extends ActionBarActivity {

    private LoginActivity me = this;

    private final String ROOT_FIREBASE_URL = "https://fallingmyo.firebaseIO.com";
    private Firebase rootReference;

    private final String UNAUTHORIZED_FIREBASE_URL = ROOT_FIREBASE_URL + "/Unauthorized";
    private Firebase unauthorizedReference;

    private RelativeLayout newPatientLayout;
    private EditText newPatientName;
    private EditText newPatientEmail;
    private EditText newPatientPassword;
    private EditText newPatientPasswordConfirm;

    private RelativeLayout loginLayout;
    private EditText patientEmail;
    private EditText patientPassword;
    private TextView loginErrorMessage;

    private boolean isRegisteringNewPatient = false;

    private TextView newPatientErrorMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeFirebaseReference(this);
        updateViewVisibility();
    }

    private void initializeViews() {
        newPatientLayout = (RelativeLayout) findViewById((R.id.newPatientLayout));
        newPatientName = (EditText) findViewById(R.id.newPatientNameEditText);
        newPatientEmail = (EditText) findViewById(R.id.newPatientEmailEditText);
        newPatientPassword = (EditText) findViewById(R.id.newPatientPasswordEditText);
        newPatientPasswordConfirm = (EditText) findViewById(R.id.newPatientPasswordConfirmEditText);
        newPatientErrorMessage = (TextView) findViewById(R.id.newPatientErrorMessageTextView);

        loginLayout = (RelativeLayout) findViewById(R.id.loginLayout);
        patientEmail = (EditText) findViewById(R.id.patientEmailEditText);
        patientPassword = (EditText) findViewById(R.id.patientPasswordEditText);
        loginErrorMessage = (TextView) findViewById(R.id.loginErrorMessageTextView);
    }

    private void initializeFirebaseReference(Context context) {
        Firebase.setAndroidContext(this);
        rootReference = new Firebase(ROOT_FIREBASE_URL);
    }

    private void updateViewVisibility() {
        if (isRegisteringNewPatient) {
            newPatientLayout.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        } else {
            newPatientLayout.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void registerNewPatient(View view) {
        try {
            confirmAllFieldsRegistrationFilled();
            confirmPasswordsMatch();
            final String name = getEditTextString(newPatientName);
            final String email = getEditTextString(newPatientEmail);
            final String password = getEditTextString(newPatientPassword);
            rootReference.createUser(email, password,
                    new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            unauthorizedReference = new Firebase(UNAUTHORIZED_FIREBASE_URL);

                            String accountID = (String)result.get("uid");
                            Patient newPatient = new Patient(name, accountID, "", email, "",
                                    false, Assistance.NONE, false);

                            unauthorizedReference.push().setValue(newPatient);
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            displayNewPatientErrorMessage(firebaseError.getMessage());
                        }
                    });
        } catch (Exception exception) {
            displayNewPatientErrorMessage(exception.getMessage());
        }
    }

    private void confirmAllFieldsRegistrationFilled() throws Exception {
        if (isFieldEmpty(newPatientName) || isFieldEmpty(newPatientEmail) ||
                isFieldEmpty(newPatientPassword) || isFieldEmpty(newPatientPasswordConfirm)) {
            throw new Exception("Please fill in all the required fields.");
        }
    }

    private boolean isFieldEmpty(EditText editText) {
        return getEditTextString(editText).length() == 0;
    }

    public void confirmPasswordsMatch() throws Exception {
        if (!doPasswordsMatch()) {
            throw new Exception("The passwords do not match.");
        }
    }

    private boolean doPasswordsMatch() {
        return getEditTextString(newPatientPassword).equals(getEditTextString(newPatientPasswordConfirm));
    }

    private String getEditTextString(EditText editText) {
        return editText.getText().toString().trim();
    }

    private void displayNewPatientErrorMessage(String message) {
        newPatientErrorMessage.setText(message);
    }

    public void toLoginScreen(View view) {
        isRegisteringNewPatient = false;
        updateViewVisibility();
    }

    public void toRegisterNewPatientScreen(View view) {
        isRegisteringNewPatient = true;
        updateViewVisibility();
    }

    public void loginPatient(View view) {
        try {
            confirmLoginFieldsFilled();
            loginPatient(getEditTextString(patientEmail), getEditTextString(patientPassword));
        } catch (Exception exception) {
            displayLoginErrorMessage(exception.getMessage());
        }
    }

    private void confirmLoginFieldsFilled() throws Exception {
        if (isFieldEmpty(patientEmail) || isFieldEmpty(patientPassword)) {
            throw new Exception("Please fill in all fields.");
        }
    }

    private void loginPatient(String patientEmail, String patientPassword) {
        rootReference.authWithPassword(patientEmail, patientPassword,
                new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        String accountID = authData.getUid();

                        Log.d("LoginActivity", accountID);

                        Intent middleManIntent = new Intent(me, MiddleManActivity.class);
                        Log.d("LoginActivity", "created intent");

                        middleManIntent.putExtra("accountID", accountID);
                        startActivity(middleManIntent);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {
                        displayLoginErrorMessage(firebaseError.getMessage());
                    }
                });
    }

    private void displayLoginErrorMessage(String message) {
        loginErrorMessage.setText(message);
    }
}
