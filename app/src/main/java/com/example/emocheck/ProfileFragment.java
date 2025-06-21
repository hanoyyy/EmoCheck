package com.example.emocheck;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.emocheck.auth.EditProfileActivity;
import com.example.emocheck.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    TextView profileName, profileEmail, profileUsername, profilePassword;
    TextView titleName, titleUsername;
    Button editProfile, logOutButton;
    FirebaseAuth mAuth;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        profileUsername = view.findViewById(R.id.profileUsername);
        profilePassword = view.findViewById(R.id.profilePassword);
        titleName = view.findViewById(R.id.titleName);
        titleUsername = view.findViewById(R.id.titleUsername);
        editProfile = view.findViewById(R.id.editButton);
        logOutButton = view.findViewById(R.id.log_outButton);
        mAuth = FirebaseAuth.getInstance();

        showUserData();

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passUserData();
            }
        });

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

    }

    private void showUserData() {
        Bundle args = getArguments();
        if (args != null) {
            String nameUser = args.getString("name");
            String emailUser = args.getString("email");
            String usernameUser = args.getString("username");
            String passwordUser = args.getString("password");

            titleName.setText(nameUser);
            titleUsername.setText(usernameUser);
            profileName.setText(nameUser);
            profileEmail.setText(emailUser);
            profileUsername.setText(usernameUser);
            profilePassword.setText(passwordUser);
        }
    }

    private void passUserData() {
        String userUsername = profileUsername.getText().toString().trim();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users");
        Query checkUserDatabase = reference.orderByChild("username").equalTo(userUsername);

        checkUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String nameFromDB = snapshot.child(userUsername).child("name").getValue(String.class);
                    String emailFromDB = snapshot.child(userUsername).child("email").getValue(String.class);
                    String usernameFromDB = snapshot.child(userUsername).child("username").getValue(String.class);
                    String passwordFromDB = snapshot.child(userUsername).child("password").getValue(String.class);

                    Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                    intent.putExtra("name", nameFromDB);
                    intent.putExtra("email", emailFromDB);
                    intent.putExtra("username", usernameFromDB);
                    intent.putExtra("password", passwordFromDB);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error here if necessary
            }
        });
    }

    private void logOut() {
        // Perform Firebase logout
        mAuth.signOut(); // Logout from Firebase Authentication

        // Redirect to login activity
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish(); // Close the current activity to prevent going back to ProfileActivity
    }
}