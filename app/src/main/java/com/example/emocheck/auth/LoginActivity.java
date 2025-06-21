package com.example.emocheck.auth;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.emocheck.MainActivity;
import com.example.emocheck.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    // Views for Username and Password
    EditText loginUsername, loginPassword;
    Button loginButton;
    TextView signupRedirectText;

    // Views for Biometric Authentication
    private ImageView fingerprintImageView;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Views for Username/Password
        loginUsername = findViewById(R.id.login_username);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        loginButton = findViewById(R.id.login_button);

        // Initialize Views for Biometric Authentication
        fingerprintImageView = findViewById(R.id.fingerprint_image_view);

        // Setup for Biometric Authentication
        setupBiometricAuthentication();

        // Login Button for Username/Password
        loginButton.setOnClickListener(view -> {
            if (!validateUsername() || !validatePassword()) {
                // Do nothing if validation fails
            } else {
                checkUser(); // Check user credentials in Firebase
            }
        });

        // Redirect to Sign-Up Page
        signupRedirectText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    // Validate Username input
    public Boolean validateUsername() {
        String val = loginUsername.getText().toString();
        if (val.isEmpty()) {
            loginUsername.setError("Username cannot be empty");
            return false;
        } else {
            loginUsername.setError(null);
            return true;
        }
    }

    // Validate Password input
    public Boolean validatePassword() {
        String val = loginPassword.getText().toString();
        if (val.isEmpty()) {
            loginPassword.setError("Password cannot be empty");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    // Check if the user exists in Firebase Database
    public void checkUser() {
        String userUsername = loginUsername.getText().toString().trim();
        String userPassword = loginPassword.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    loginUsername.setError(null);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    if (passwordFromDB.equals(userPassword)) {
                        loginUsername.setError(null);

                        // Save fingerprint if login is successful
                        saveFingerprintData(userUsername, userPassword);

                        // Pass the data using intent to ProfileActivity
                        String nameFromDB = snapshot.child(userUsername).child("name").getValue(String.class);
                        String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                        String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("name", nameFromDB);
                        intent.putExtra("email", emailFromDB);
                        intent.putExtra("username", usernameFromDB);
                        intent.putExtra("password", passwordFromDB);
                        startActivity(intent);
                    } else {
                        loginPassword.setError("Invalid Credentials");
                        loginPassword.requestFocus();
                    }
                } else {
                    loginUsername.setError("User does not exist");
                    loginUsername.requestFocus();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    // Save fingerprint data after successful login
    private void saveFingerprintData(String username, String password) {
        getSharedPreferences("UserData", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .putString("password", password)
                .apply();
    }

    // Setup Biometric Authentication
    private void setupBiometricAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);

        // Only check for fingerprint support
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("LoginActivity", "App can authenticate using biometrics.");
                break;

            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Device does not support biometric authentication.", Toast.LENGTH_SHORT).show();
                return;

            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(this, "Biometric hardware is currently unavailable.", Toast.LENGTH_SHORT).show();
                return;

            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometrics enrolled. Redirecting to settings.", Toast.LENGTH_SHORT).show();
                final Intent enrollIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                startActivity(enrollIntent);
                return;

            default:
                Toast.makeText(this, "Biometric authentication is not available.", Toast.LENGTH_SHORT).show();
                return;
        }

        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();

                // Auto-fill username and password
                String savedUsername = getSharedPreferences("UserData", MODE_PRIVATE).getString("username", "");
                String savedPassword = getSharedPreferences("UserData", MODE_PRIVATE).getString("password", "");

                loginUsername.setText(savedUsername);
                loginPassword.setText(savedPassword);

                // Validate against Firebase
                checkUser();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setSubtitle("Log in using your fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        // Trigger biometric authentication when fingerprint image is clicked
        fingerprintImageView.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
    }
}
