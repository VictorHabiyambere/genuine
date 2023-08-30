package com.example.genuine;

import static android.content.ContentValues.TAG;
import static com.example.genuine.FirstView.username1;
import static com.example.genuine.MainActivity.usernameID;
import static com.example.genuine.MainLobby.request_sent;
import static com.example.genuine.MainLobby.request_sentID;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OnSwipeTouchListener extends AppCompatActivity implements View.OnTouchListener {

    public Pattern p = Pattern.compile("(\\w+)");//. represents single character
    public Matcher matching;
    ListView list;
    private final GestureDetector gestureDetector;

    public OnSwipeTouchListener(Context ctx, ListView list) {
        gestureDetector = new GestureDetector(ctx, new GestureListener());
        this.list = list;
    }

    public String getName(String in, int N) {
        Matcher m = p.matcher(in);

        for (int i = 0; i < N && m.find(); i++) {
        }

        return m.group(1);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
        //Do what you want after swiping left to right
        matching = p.matcher(request_sent);
        String who = getName(request_sent, 7);
        ListView listview3 = findViewById(R.id.ItemList3);
        //The sender gets a notice that the user has rejected their request sent to them
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
        mDatabase2.child(request_sentID).child("Rejection").setValue(who);
        ArrayAdapter adapter_ = (ArrayAdapter) listview3.getAdapter();
        adapter_.remove(who);
        adapter_.notifyDataSetChanged();
        listview3.setAdapter(adapter_);
        //Once this information is extracted, the proper prompt will be generated
    }

    public void onSwipeLeft() {

        //Do what you want after swiping right to left
        ListView listview3 = findViewById(R.id.ItemList3);
        matching = p.matcher(request_sent);
        String who = getName(request_sent, 7);
        //The sender gets a notice that the user has rejected their request sent to them
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
        mDatabase2.child(request_sentID).child("Rejection").setValue(who);
        ArrayAdapter adapter_ = (ArrayAdapter) listview3.getAdapter();
        adapter_.remove(who);
        adapter_.notifyDataSetChanged();
        listview3.setAdapter(adapter_);
        //Once this information is extracted, the proper prompt will be generated
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            assert e1 != null;
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY)
                    && Math.abs(distanceX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }

    }
}

public class MainLobby extends AppCompatActivity {

    public static Interpreter module;

    public static float scaled_val;

    public static float accuracy;

    public static String receiver;

    public static ArrayList<String> arr1 = new ArrayList<>();
    public static ArrayList<String> arr2 = new ArrayList<>();

    public static ArrayList<String> arr3 = new ArrayList<>();

    public static ArrayList<String> arr4 = new ArrayList<>();

    public static ArrayList<Float> arr5 = new ArrayList<>();

    public static ArrayList<Float> arr6 = new ArrayList<>();

    public static ArrayList<String> arr7 = new ArrayList<>();

    public static ArrayList<String> arr8 = new ArrayList<>();

    public static String request_sent;

    public static String request_sentID;

    public static float max_trust_value;


    //It's patterns all the way down...

    public Pattern p = Pattern.compile("(\\w+)");//. represents single character
    public Matcher matching;


    public boolean present = false;

    public float MAX_VALUE = (float) Math.pow(10, 4);
    public static MediaPlayer sound;
    public List<String> spinnerArray = new ArrayList<>();
    public List<String> spinnerArray2 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main_lobby);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainLobby.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(usernameID);
            final Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {

                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.getValue() == null) {
                                return;
                            }
                            for (DataSnapshot child1 : dataSnapshot.getChildren()) {

                                username1 = Objects.requireNonNull(child1.getValue()).toString();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });
                    handler.postDelayed(this, 2000);
                    handler.removeCallbacks(this);
                }
            });

        }
        scaled_val = -1f;
        max_trust_value = 0;
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, initializationStatus -> {
        });

        listen_for_model();
        listen_for_accuracy();
        listen_for_trustlevels();
        getPeople();
        listen_for_requests();
        listen_for_rejection();
        listen_for_friends();

        ListView listview = findViewById(R.id.ItemList2);
        listview.setOnItemClickListener(
                (adapter, view, position, id) -> {

                    //Use the list position
                    //Get the username of the friend located at that position
                    receiver = arr3.get(position);
                    Intent intent = new Intent(MainLobby.this, MainActivity.class);
                    //based on item add info to intent
                    startActivity(intent);
                });

        ListView listview2 = findViewById(R.id.ItemList);
        listview2.setOnItemClickListener(
                (adapter, view, position, id) -> {
                    //Use the list position
                    request_sentID = arr4.get(position);
                    //Get the username of the friend located at that position
                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                    String message_sent = "Do you want to connect with " + username1 + "?";
                    mDatabase.child("Users").child(request_sentID).child("Requests").setValue(message_sent);
                    mDatabase.child("Users").child(request_sentID).child("Requested").setValue(usernameID);
                });

        ListView listview3 = findViewById(R.id.ItemList3);

        listview3.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position,
                                            long id) {
                        //Use the list position
                        request_sentID = arr7.get(position);

                        //Add the user to the list of people it can contact
                        //The effect is a display where users can access to message
                        //For now, this will be a simple listview of usernames
                        //once a username is clicked, it is sent to a new view, where the user can chat with the other
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        //Set up a regex that parses for the right value
                        matching = p.matcher(request_sent);
                        present = p.matcher(request_sent).matches();
                        String who = getName(request_sent, 7);

                        //Add the friend to friends list
                        //Remove friend request from listview
                        ArrayAdapter adapter_ = (ArrayAdapter) listview3.getAdapter();
                        adapter_.remove(who);
                        adapter_.notifyDataSetChanged();
                        listview3.setAdapter(adapter_);
                        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID);
                        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
                        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
                        mDatabase2.child("Friends").child(who).setValue(request_sentID);
                        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users").child(request_sentID);
                        mDatabase3.child("Friends").child(username1).setValue(usernameID);
                        DatabaseReference mDatabase_ = FirebaseDatabase.getInstance().getReference().child(usernameID);
                        final Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                mDatabase_.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        if (dataSnapshot.getValue() == null) {
                                            return;
                                        }
                                        for (DataSnapshot child1 : dataSnapshot.getChildren()) {

                                            username1 = Objects.requireNonNull(child1.getValue()).toString();
                                            mDatabase3.child("Friends").child(username1).setValue(usernameID);

                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        System.out.println("The read failed: " + databaseError.getCode());
                                    }
                                });
                                handler.postDelayed(this, 2000);
                                handler.removeCallbacks(this);
                            }
                        });
                    }
                });

        listview3.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext(), listview3));

    }

    public float similarity(@NonNull String candidate, @NonNull String target) {

        int sims = 0;
        int g = 0;

        while (g < target.length()) {

            if (candidate.charAt(g) == (target.charAt(g))) {

                sims += 1;

            }
            g += 1;

        }

        if (target.length() != 0) {
            return (float) (sims / target.length());
        }
        return 0;

    }

    public void searchUser(View view) {

        //Calculate the similarity score of the users
        //Then, sort them according to their match score
        EditText editText = findViewById(R.id.editText2);
        String search1 = editText.getText().toString();
        sound = MediaPlayer.create(this, R.raw.select);
        sound.setOnCompletionListener(MediaPlayer::release);
        sound.start();

        //Erase what was written
        editText.setText("");
        float match_score;
        //60% is the trust score
        //40% is the similarity score
        ListView listview = findViewById(R.id.ItemList);
        ArrayAdapter<String> adapter1 = (ArrayAdapter<String>) listview.getAdapter();
        ArrayList<Float> scores = new ArrayList<>();
        DatabaseReference trust_list = FirebaseDatabase.getInstance().getReference().child("Scaled Trust:");
        final Handler handler = new Handler();
        handler.post(() -> trust_list.addChildEventListener(new ChildEventListener() {


            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                //In this case, a child has changed
                if (snapshot.getValue() == null) {
                    return;
                }
                Float scaled_value = (Float) snapshot.getValue();
                //Now, use this scaled value to sort,
                arr5.add(scaled_value);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }

        }));
        if (adapter1 != null) {
            //For loop to analyze the contents of the ArrayAdapter
            for (int x = 0; x < adapter1.getCount(); x++) {

                float similarity1 = similarity(Objects.requireNonNull(adapter1.getItem(x)), search1);
                if (arr5.size() != 0) {
                    match_score = arr5.get(x) * 0.6f + 0.4f * similarity1;
                } else {
                    match_score = similarity1;
                }
                scores.add(match_score);

            }
            //Now, sort the user list according to this match_score order...
            scores.sort(Comparator.comparingDouble(arr5::indexOf));
            //Then, update the listview
            adapter1.notifyDataSetChanged();
            listview.setAdapter(adapter1);
        }
    }

    public String getName(String in, int N) {
        Matcher m = p.matcher(in);

        for (int i = 0; i < N && m.find(); i++) {
        }

        return m.group(1);
    }
    //Receiving a message does not happen by itself-> the app will need to constantly
    //listen for a potential message in an intelligent manner
    //any message that the user is part of conversation will be received
    //That means I need to create an efficient algorithm that curates from a
    //list of potential message pipelines and receives messages accordingly
    //I believe this can be done by exploiting parallelism, where
    //an algorithm systematically searches through a real-time database, independently
    //and tries to find messages it has received

    //I will employ parallelism later,now, I simply want to continuously listen for updates

    //Now, my algorithm sorts people based on how trustable they can be
    public void getPeople() {

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference people_list = FirebaseDatabase.getInstance().getReference().child("Users");
        DatabaseReference trust_list = FirebaseDatabase.getInstance().getReference().child("Scaled Trust:");
        mAuth.signInAnonymously()
                .addOnCompleteListener(MainLobby.this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainLobby.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
        }

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                trust_list.addChildEventListener(new ChildEventListener() {


                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        //In this case, a child has changed
                        if (snapshot.getValue() == null) {
                            return;
                        } else if (Objects.equals(snapshot.getKey(), usernameID)) {

                            scaled_val = (float) snapshot.getValue();
                            //Display the scaled trust value
                            TextView display = findViewById(R.id.textView7);
                            String net_display = "Score:" + scaled_val;
                            display.setText(net_display);

                        }
                        //Play a winning sound when estimated trustability rises!
                        if (scaled_val != -1f && scaled_val < (float) snapshot.getValue() && Objects.equals(snapshot.getKey(), usernameID)) {

                            sound = MediaPlayer.create(getApplicationContext(), R.raw.win);
                            sound.setOnCompletionListener(MediaPlayer::release);
                            sound.start();

                        }
                        Float scaled_value = (Float) snapshot.getValue();

                        //Now, use this scaled value to sort,
                        arr5.add(scaled_value);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }

                });

                people_list.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                            if (child != null) {

                                //Make sure to not be able to friend oneself
                                if (!Objects.equals(child.getKey(), usernameID)) {

                                    for (DataSnapshot child1 : child.getChildren()) {

                                        if (!Objects.equals(child1.getKey(), "Requests") && !Objects.equals(child1.getKey(), "Requested") && !Objects.equals(child.getKey(), "Friends")) {

                                            arr1.add(Objects.requireNonNull(child1.getValue()).toString());

                                        }

                                    }
                                    arr4.add(child.getKey());
                                }
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);

                        if (arr1.size() > adapter.getCount()) {

                            spinnerArray.addAll(arr1);
                            //Sort the spinnerArray values from highest scaled trust level
                            //to lowest scaled trust level
                            if (arr5.size() != 0) {
                                spinnerArray.sort(Comparator.comparingDouble(arr5::indexOf));
                            }

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            ListView PeopleList = findViewById(R.id.ItemList);
                            adapter.notifyDataSetChanged();
                            PeopleList.setAdapter(adapter);
                            arr1 = new ArrayList<>();
                            arr4 = new ArrayList<>();
                            arr5 = new ArrayList<>();
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

    }

    public void listen_for_model() {

        //Each time the model updates, load up the model and use the best iteration of it
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("GenuineTrustPredictor");
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                        .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                        .build();
                FirebaseModelDownloader.getInstance()
                        .getModel("GenuineTrustPredictor", DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                        .addOnSuccessListener(new OnSuccessListener<CustomModel>() {
                            @Override
                            public void onSuccess(CustomModel model) {
                                File modelFile = model.getFile();
                                if (modelFile != null) {
                                    module = new Interpreter(modelFile);
                                }
                            }
                        });
                handler.postDelayed(this, 2000);

            }
        });

    }

    public void listen_for_trustlevels() {

        //Each time the model updates, load up the model and use the best iteration of it
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("GenuineTrustPredictor");
        final Handler handler = new Handler();
        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Maximum Trust:");
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase3.addValueEventListener(

                        new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() == null) {
                                    return;
                                }
                                max_trust_value = (float) dataSnapshot.getValue();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }
                        });

                mDatabase.addChildEventListener(new ChildEventListener() {

                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        //In this case, a child has changed
                        if (snapshot.getValue() == null) {
                            return;
                        }
                        float trust_level = (float) snapshot.getValue();
                        //Scale the trust_level amongst other trust levels
                        //To do this, I will keep track of the maximum value
                        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference();
                        //Update the max trust value accordingly
                        if (trust_level > max_trust_value) {
                            max_trust_value = trust_level;
                            mDatabase2.child("Maximum Trust:").setValue(max_trust_value);
                        }
                        //Now scale the value...
                        float scaled_trust = trust_level - max_trust_value / MAX_VALUE;
                        mDatabase2.child("Scaled Trust:").child(Objects.requireNonNull(snapshot.getKey())).setValue(scaled_trust);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.postDelayed(this, 2000);
            }
        });

    }

    public void listen_for_accuracy() {

        //Each time the model updates, load up the model and use the best iteration of it
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("GenuineTrustAccuracy");
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        //Extracted the accuracy, tada!!
                        accuracy = (float) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.postDelayed(this, 2000);
            }
        });

    }

    public void listen_for_requests() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success");

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(MainLobby.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
        }
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID).child("Requests");
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID).child("Requested");
        final Handler handler = new Handler();
        String[] request_sent1 = {""};
        final boolean[] status = {true};
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        } else if (request_sentID != null) {

                            if (request_sentID.equals(dataSnapshot.getValue().toString())) {
                                status[0] = false;
                                return;

                            }
                        }

                        request_sentID = dataSnapshot.getValue().toString();

                        arr7.add(request_sentID);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

                handler.postDelayed(this, 2000);

                if (!status[0]) {
                    return;
                }

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }

                        request_sent1[0] = dataSnapshot.getValue().toString();
                        request_sent = request_sent1[0];
                        sound = MediaPlayer.create(getApplicationContext(), R.raw.notification);
                        sound.setOnCompletionListener(MediaPlayer::release);
                        sound.start();
                        ListView listview_ = findViewById(R.id.ItemList3);
                        matching = p.matcher(request_sent);
                        String who = getName(request_sent, 7);
                        arr8.add(who);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainLobby.this, android.R.layout.simple_spinner_item, arr8);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                        listview_.setAdapter(adapter);


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });

                handler.postDelayed(this, 2000);
            }
        });
    }

    public void listen_for_rejection() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID)
                .child("Rejection");
        final Handler handler = new Handler();
        String[] request_sent1 = {""};
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        request_sent1[0] = dataSnapshot.getValue().toString();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.postDelayed(this, 2000);
            }
        });

    }

    //Now, my algorithm sorts people based on how trustable they can be
    public void listen_for_friends() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID)
                .child("Friends");

        final Handler handler = new Handler();
        DatabaseReference trust_list = FirebaseDatabase.getInstance().getReference().child("Scaled Trust:");
        handler.post(new Runnable() {
            @Override
            public void run() {
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        for (DataSnapshot dS : dataSnapshot.getChildren()) {
                            arr2.add(dS.getKey());
                            arr3.add((String) dS.getValue());
                        }

                        if (arr2 == null) {
                            return;
                        }

                        spinnerArray2.addAll(arr2);

                        trust_list.addChildEventListener(new ChildEventListener() {


                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                //In this case, a child has changed
                                snapshot.getValue();
                                //Make sure this individual is amongst your friends list...
                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }

                        });
                        if (arr6.size() != 0) {
                            //Sort friends from most trustable to least trustable
                            spinnerArray.sort(Comparator.comparingInt(arr6::indexOf));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                MainLobby.this, android.R.layout.simple_spinner_item, spinnerArray2);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ListView PeopleList = findViewById(R.id.ItemList2);
                        PeopleList.setAdapter(adapter);
                        arr2 = new ArrayList<>();
                        spinnerArray2 = new ArrayList<>();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.postDelayed(this, 2000);

            }
        });

    }
}