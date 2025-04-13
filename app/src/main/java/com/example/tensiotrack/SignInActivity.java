package com.example.tensiotrack;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.sql.Array;

public class SignInActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = SignInActivity.class.getName();
    private static final String PREF_KEY = SignInActivity.class.getPackage().toString();
    private static final int SECRET_KEY = 32;

    EditText nameET;
    EditText usernameET;
    EditText emailET;
    EditText dateofbirthET;
    RadioGroup genderRG;
    EditText weightET;
    EditText heightET;
    EditText password1ET;
    EditText password2ET;
    Spinner weightSpinner;
    Spinner heightSpinner;

    private SharedPreferences preferences;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int secret_key = getIntent().getIntExtra("SECRET_KEY", 0);

        if (secret_key != 32) {
            finish();
        }

        nameET = findViewById(R.id.edittextName);
        usernameET = findViewById(R.id.edittextUsername);
        emailET = findViewById(R.id.edittextEmail);
        dateofbirthET = findViewById(R.id.edittextDateofBirth);
        genderRG = findViewById(R.id.radioGender);
        genderRG.check(R.id.radioWoman);
        weightET = findViewById(R.id.edittextWeight);
        heightET = findViewById(R.id.edittextHeight);
        password1ET = findViewById(R.id.edittextPassword1);
        password2ET = findViewById(R.id.edittextPassword2);
        weightSpinner = findViewById(R.id.spinnerWeight);
        heightSpinner = findViewById(R.id.spinnerHeight);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        usernameET.setText(username);
        password1ET.setText(password);
        password2ET.setText(password);

        weightSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> weight_adapter = ArrayAdapter.createFromResource(this, R.array.weight_modes, android.R.layout.simple_spinner_item);
        weight_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weightSpinner.setAdapter(weight_adapter);

        heightSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> height_adapter = ArrayAdapter.createFromResource(this, R.array.height_modes, android.R.layout.simple_spinner_item);
        height_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        heightSpinner.setAdapter(height_adapter);

        mAuth = FirebaseAuth.getInstance();

        Log.i(LOG_TAG, "onCreate");
    }

    public void register(View view) {
        String name = nameET.getText().toString();
        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        String dateofbirth = dateofbirthET.getText().toString();

        int checkedID = genderRG.getCheckedRadioButtonId();
        RadioButton radioButton = genderRG.findViewById(checkedID);
        String genderType = radioButton.getText().toString();

        String weight = weightET.getText().toString();
        String height = heightET.getText().toString();
        String password1 = password1ET.getText().toString();
        String password2 = password2ET.getText().toString();

        if (!password1.equals(password2)) {
            Log.e(LOG_TAG, "A jelszavak nem egyeznek!");
            return;
        }

        String weightType = weightSpinner.getSelectedItem().toString();
        String heightType = heightSpinner.getSelectedItem().toString();

        Log.i(LOG_TAG, "Regisztrált: " + name + ", username: " + username);
        mAuth.createUserWithEmailAndPassword(email, password1).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Log.d(LOG_TAG, "Felhasználó létrehozása sikeres!");
                    startTrack();
                } else {
                    Log.d(LOG_TAG, "Felhasználó létrehozása sikertelen!");
                    Toast.makeText(SignInActivity.this, "Felhasználó létrehozása sikertelen: " + task.getException().getMessage() , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void cancel(View view) {
        finish();
    }

    private void startTrack(/* registered data */) {
        Intent intent = new Intent(this, HomeActivity.class);
        // intent.putExtra("SECRET_KEY", SECRET_KEY );
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedItem = parent.getItemAtPosition(position).toString();
        Log.i(LOG_TAG, selectedItem);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //TODO.
    }
}