package com.example.genuine;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;
import static com.example.genuine.MainActivity.usernameID;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirstView extends AppCompatActivity {

    public static String username1 = "";

    public static MediaPlayer button_sound = new MediaPlayer();

    //Check if the user has already logged in, if this is the case,
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        MobileAds.initialize(this, initializationStatus -> {
        });
        FirebaseApp.initializeApp(this);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(FirstView.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
            DatabaseReference user_list = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID);
            isLogged_in(user_list);
        }
    }
    public void isLogged_in(DatabaseReference mDatabase) {

        final Handler handler = new Handler();
        final boolean[] not_found = {true};
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {
                            not_found[0] = false;
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
                if (!not_found[0]) {
                    Intent intent = new Intent(FirstView.this, MainLobby.class);
                    startActivity(intent);
                    handler.post(this);
                    handler.removeCallbacks(this);
                } else {
                    setContentView(R.layout.activity_first_view);
                    Button button = findViewById(R.id.button5);
                    EditText editText = (EditText) findViewById(R.id.editTextText4);
                    editText.setVisibility(VISIBLE);
                    button.setVisibility(VISIBLE);
                    handler.post(this);
                    handler.removeCallbacks(this);
                }

            }
        });
    }

    public void get_username(View view) {

        EditText username = findViewById(R.id.editTextText4);

        String username_chosen = username.getText().toString();

        button_sound = MediaPlayer.create(this, R.raw.select);

        button_sound.start();
        button_sound.release();

        //How this will integrate with everything else, is that there will be a layer of abstraction
        //Users who log-into the app will have a username they have chosen for themselves
        //and this username is what will show on a users friends list
        //How I will solve the problem of username ID and username is that
        //this information will be paired up together
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(FirstView.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        if (!username_chosen.equals("")) {
            mDatabase.child("Users").child(usernameID).setValue(username_chosen);
            username1 = username_chosen;
            Intent intent = new Intent(this, MainLobby.class);
            startActivity(intent);
        }

    }

}