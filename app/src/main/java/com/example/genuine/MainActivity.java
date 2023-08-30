package com.example.genuine;

import static android.content.ContentValues.TAG;
import static com.example.genuine.MainLobby.accuracy;
import static com.example.genuine.MainLobby.module;
import static java.util.Arrays.sort;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.MobileAds;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final int GALLARY_IMAGE = 100;
    private static final int PICK_VIDEO = 1;
    /*
    Progress today
    What "Genuine"` should do in the far future, when I master every other feature
    "Genuine" should be able to detect if the AI is overfitting, and minimize it's
    overfitting(can probably be done in the near future)
    "Genuine" should be able to use quantum computing to perform database search exponentially faster than traditional methods!
    "Genuine" should have layouts that look good in all types of android devices
     */
    public static String usernameID;

    public static String message_sent2;

    public static Map<Integer, String> word_base;

    public static Bitmap sent_image = null;

    public static int messages_sent = 0;

    public static Uri sent_video = null;

    public static String receiver;
    public static float actual_trustability = 0;
    public Pattern p = Pattern.compile("(\\w+)");
    public static MediaPlayer button_sound;

    public static float calc_distance(String message_pred, String message) {
        int word_right = 0;
        for (int i = 0; i < message.length(); i++) {
            if (message_pred.charAt(i) == message.charAt(i)) {
                word_right += 1;
            }
        }
        word_right += 1;
        if (message.length() != 0) {
            return word_right / message.length();
        }
        return 0;
    }

    //Done
    public static float calc_trust(String message, String actual_response) {

        String[] message1 = message.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        String[] response1 = actual_response.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        StringBuilder response2 = new StringBuilder();
        for (String str : response1) {
            response2.append(str).append(" ");
        }
        int i1 = (message1.length / 10) + 1;
        float[][] input = new float[i1][10];
        float[][] output = new float[1][word_base.size()];
        int i = 0;
        StringBuilder final_ = new StringBuilder();
        StringBuilder messageBuilder = new StringBuilder(message);
        for (int x = 0; x != response1.length; x++) {
            for (int i2 = 0; i2 != i1; i2++) {
                for (String str : message1) {

                    if (word_base.containsValue(str)) {

                        //Get its associated key
                        int key = 0;
                        for (Map.Entry<Integer, String> entry : word_base.entrySet()) {
                            if (entry.getValue().equals(str)) {
                                key = entry.getKey();
                            }
                        }
                        if (i < 10) {
                            input[i2][i] = (float) key;
                            i += 1;
                        }
                    }

                }
            }
            module.run(input, output);
            //Measure trustability
            //Convert output to
            String word = word_base.get(output);
            final_.append(word).append(" ");
            messageBuilder.append(word).append(" ");
            message1 = messageBuilder.toString().replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
        }
        //Calculate the distance between prediction and reality
        return accuracy * calc_distance(final_.toString(), response2.toString());
    }

    public void listen_for_knowledge() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("WordKnowledgeBase");
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            return;
                        }
                        word_base = (Map<Integer, String>) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });

                handler.postDelayed(this, 2000);

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        messages_sent = 0;
        listen_for_messages();
        listen_for_knowledge();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Sign-out the user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
    }

    public void listen_for_messages() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final Handler handler = new Handler();
        String[] message_sent1 = new String[1];
        final boolean[] not_found = {true};
        String[] username1 = {""};
        String[] username2 = {""};
        String[] middle = {""};
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        DataSnapshot ds = dataSnapshot;
                        for (DataSnapshot ds1 : dataSnapshot.getChildren()) {
                            username1[0] = getName(ds1.getKey(), 0);
                            middle[0] = getName(ds1.getKey(), 1);
                            if (!middle[0].equals("messaging")) {
                                not_found[0] = true;
                            } else {
                                username2[0] = getName(ds1.getKey(), 2);
                                not_found[0] = false;
                                ds = ds1;
                                break;
                            }
                        }

                        if (!not_found[0]) {

                            //Check if the username is present in this key
                            if (username1[0].equals(usernameID)) {
                                String message1;

                                message1 = (String) ds.child(username2[0]).getValue();
                                message_sent1[0] = message1;
                                if (message_sent1[0] == null) {
                                    return;
                                }
                                float trustability = calc_trust(message_sent2, message_sent2);
                                //The really dope part:
                                DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference();
                                actual_trustability += trustability;
                                mDatabase3.child("Trustability Levels").child(username2[0]).setValue(actual_trustability / messages_sent);
                                LinearLayout linearLayout = findViewById(R.id.linearLayout5);
                                TextView display = new TextView(getApplicationContext());
                                display.setText(message_sent1[0]);
                                mDatabase.child("Dataset").child("Response " + username2[0]).setValue(message_sent1[0]);
                                Drawable d;
                                d = Objects.requireNonNull(Objects.requireNonNull(ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox)).getConstantState()).newDrawable();
                                display.setPadding(30, 30, 30, 30);
                                display.setBackground(d);
                                display.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.tint_color));
                                display.setTextColor((ContextCompat.getColor(MainActivity.this, R.color.black)));
                                linearLayout.addView(display);
                                View view1 = new View(getApplicationContext());
                                view1.setMinimumHeight(15);
                                view1.setMinimumWidth(display.getWidth());
                                linearLayout.addView(view1);
                                DatabaseReference mDatabase2 = ds.child(username2[0]).getRef();
                                mDatabase2.removeValue();
                                return;
                            }
                            if (username2[0].equals(usernameID)) {
                                String message1;
                                message1 = (String) ds.child(username1[0]).getValue();
                                message_sent1[0] = message1;
                                if (message_sent1[0] == null) {
                                    return;
                                }
                                float trustability = calc_trust(message_sent2, message_sent2);
                                actual_trustability += trustability;
                                DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference();
                                mDatabase3.child("Trustability Levels").child(username2[0]).setValue(actual_trustability / messages_sent);

                                LinearLayout linearLayout = findViewById(R.id.linearLayout5);
                                TextView display = new TextView(getApplicationContext());
                                display.setText(message_sent1[0]);
                                mDatabase.child("Dataset").child("Response " + username1[0]).setValue(message_sent1[0]);
                                Drawable d;
                                d = Objects.requireNonNull(Objects.requireNonNull(ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox)).getConstantState()).newDrawable();
                                display.setPadding(30, 30, 30, 30);
                                display.setBackground(d);
                                display.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.tint_color));
                                display.setTextColor((ContextCompat.getColor(MainActivity.this, R.color.black)));
                                linearLayout.addView(display);
                                View view1 = new View(getApplicationContext());
                                view1.setMinimumHeight(15);
                                view1.setMinimumWidth(display.getWidth());
                                linearLayout.addView(view1);
                                DatabaseReference mDatabase2 = ds.child(username1[0]).getRef();
                                mDatabase2.removeValue();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
                if (!not_found[0]) {
                    handler.postDelayed(this, 2000);
                }

            }
        });

    }

    public void getVideoChat(View view) {

        Intent intent = new Intent(MainActivity.this, VideoChat.class);
        //based on item add info to intent
        startActivity(intent);

    }


    public String getName(String in, int N) {
        Matcher m = p.matcher(in);
        for (int i = 0; i < N && m.find(); i++) ;

        if (!m.find()) {
            return "";
        }

        return m.group(1);
    }

    //Allow users to select images and send them so a friend

    public void get_image(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLARY_IMAGE);

    }

    public void get_video(View view) {

        button_sound = MediaPlayer.create(this, R.raw.select);
        button_sound.start();
        button_sound.setOnCompletionListener(MediaPlayer::release);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_VIDEO);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == GALLARY_IMAGE) {

                Uri selectedImageUri = data.getData();
                try {
                    sent_image = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (requestCode == PICK_VIDEO) {

                sent_video = data.getData();

            }

        }

    }

    public void send_message(View view) {

        EditText message = findViewById(R.id.editTextText);
        String message_sent = message.getText().toString();
        button_sound = MediaPlayer.create(this, R.raw.select);
        button_sound.start();
        button_sound.setOnCompletionListener(MediaPlayer::release);
        if (message_sent.equals("")) {
            return;
        }
        FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
        }

        String[] users = new String[]{receiver, usernameID};
        sort(users);
        String setup = users[0] + " messaging " + users[1];
        //Send the sent message to the real-time database
        LinearLayout linearLayout = findViewById(R.id.linearLayout5);
        TextView display = new TextView(getApplicationContext());
        display.setText(message_sent);
        Drawable d;
        d = Objects.requireNonNull(Objects.requireNonNull(ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox)).getConstantState()).newDrawable();
        display.setPadding(30, 30, 30, 30);
        display.setBackground(d);
        display.setTextColor((ContextCompat.getColor(MainActivity.this, R.color.white)));
        //Images take first priority when set
        if (sent_image != null) {
            ImageView image = new ImageView(getApplicationContext());
            image.setImageBitmap(sent_image);
            linearLayout.addView(image);
            //Set back to null
            sent_image = null;
        } else if (sent_video != null) {
            VideoView video = new VideoView(getApplicationContext());
            video.setVideoURI(sent_video);
            linearLayout.addView(video);
            sent_video = null;

        }
        linearLayout.addView(display);
        View view1 = new View(this);
        view1.setMinimumHeight(15);
        view1.setMinimumWidth(display.getWidth());
        linearLayout.addView(view1);
        message_sent2 = message_sent;
        mDatabase.child(setup).child(usernameID).setValue(message_sent);
        //Create a dataset of messages
        mDatabase.child("Dataset").child("Message" + " " + usernameID).setValue(message_sent);
        message.setText("");
        messages_sent += 1;

    }


    //Shows the 10 friends of a user
    //Next, I plan to place them in the order of trustability

    //I don't know how to configure it such that when a list item is clicked,
    //the message system will be successfully set-up...
    //but I will figure this out when I learn more about android listviews


}