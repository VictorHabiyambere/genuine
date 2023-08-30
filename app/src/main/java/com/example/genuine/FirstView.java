package com.example.genuine;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;
import static com.example.genuine.MainActivity.usernameID;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FirstView extends AppCompatActivity {

    public static String username1;

    public static MediaPlayer button_sound;

    public static String password1;

    //Check if the user has already logged in, if this is the case,
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        MobileAds.initialize(this, initializationStatus -> {
        });
        isLogged_in();

    }

    public void isLogged_in() {

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {

                File file = new File("Genuine_Username.txt");

                if (!file.exists()) {

                    setContentView(R.layout.activity_first_view);
                    Button button = findViewById(R.id.button5);
                    EditText editText = findViewById(R.id.editTextText4);
                    editText.setVisibility(VISIBLE);
                    button.setVisibility(VISIBLE);
                    return;

                } else if (file.exists() && !file.isDirectory()) {

                    Intent intent = new Intent(FirstView.this, MainLobby.class);

                    startActivity(intent);

                }

                handler.postDelayed(this, 2000);
                handler.removeCallbacks(this);
            }
        });
    }

    public void get_username(View view) throws IOException {

        EditText username = findViewById(R.id.editTextText4);

        EditText password = findViewById(R.id.editTextText2);

        String username_chosen = username.getText().toString();
        String password_chosen = password.getText().toString();

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        button_sound = MediaPlayer.create(this, R.raw.select);
        button_sound.setOnCompletionListener(mp -> {
            mp.release();

        });
        button_sound.start();

        if (!username_chosen.equals("")) {
            byte[] username_;
            username_ = username_chosen.getBytes();
            String FILENAME = "Genuine_Username.txt";

            FileOutputStream outputStream = getApplicationContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);
            outputStream.write(username_);
            outputStream.close();
            username1 = username_chosen;
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signInAnonymously()
                    .addOnCompleteListener(FirstView.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(FirstView.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                usernameID = user.getUid();
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
                //Otherwise, find the usernameID associated with a password and allow user to
                //recover all their account information
                final Handler handler = new Handler();
                final boolean[] status = {false};
                final boolean[] status2 = {false};
                final String[] usernameID2 = {""};
                final String[] username_1 = {""};
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot child : dataSnapshot.getChildren()) {

                                    if (child.child("Password").equals(password_chosen)) {

                                        //Reject
                                        status[0] = true;
                                        return;

                                    } else if (child.getValue().equals(usernameID)) {

                                        status2[0] = true;
                                        return;

                                    } else {
                                        usernameID2[0] = child.getValue().toString();
                                        //Keep the username...
                                        username_1[0] = child.child(child.getValue().toString()).getValue().toString();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });
                        handler.postDelayed(this, 2000);
                    }
                });
                if (status[0]) {
                    //Don't bother processing log-in
                    password.setText("");
                    username.setText("");
                } else if (status2[0]) {
                    db.child("Users").child(usernameID).child(username1).setValue(username1);
                    db.child("Users").child(usernameID).child("Password").setValue(password_chosen);
                    password1 = password_chosen;
                    Intent intent = new Intent(this, MainLobby.class);
                    intent.putExtra("Username:", username1);
                    startActivity(intent);
                } else {
                    //Systematically find the username
                    usernameID = usernameID2[0];
                    //Users cannot change their name, so the username input will be ignored
                    db.child("Users").child(usernameID).child("Password").setValue(password_chosen);
                    password1 = password_chosen;
                    Intent intent = new Intent(this, MainLobby.class);
                    intent.putExtra("Username:", username_1);
                    startActivity(intent);
                }
            }
        }

    }

}