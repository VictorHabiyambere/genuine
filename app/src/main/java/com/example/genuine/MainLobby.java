package com.example.genuine;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.genuine.FirstView.username1;
import static com.example.genuine.MainActivity.usernameID;
import static com.example.genuine.MainLobby.request_sent;
import static com.example.genuine.MainLobby.request_sentID;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.pytorch.Module;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
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

    public void onSwipeRight(int pos) {
        //Do what you want after swiping left to right
        matching = p.matcher(request_sent);
        String who = getName(request_sent, 7);
        //The sender gets a notice that the user has rejected their request sent to them
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
        mDatabase2.child(request_sentID).child("Rejection").setValue(who);
        //Once this information is extracted, the proper prompt will be generated
    }

    public void onSwipeLeft(int pos) {

        //Do what you want after swiping right to left
        matching = p.matcher(request_sent);
        String who = getName(request_sent, 7);
        //The sender gets a notice that the user has rejected their request sent to them
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
        mDatabase2.child(request_sentID).child("Rejection").setValue(who);
        //Once this information is extracted, the proper prompt will be generated
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        private int getPostion(MotionEvent e1) {
            return list.pointToPosition((int) e1.getX(), (int) e1.getY());
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY)
                    && Math.abs(distanceX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight(getPostion(e1));
                else
                    onSwipeLeft(getPostion(e1));
                return true;
            }
            return false;
        }

    }
}

public class MainLobby extends AppCompatActivity {

    public static Module module;

    public static float scaled_val;

    public static float accuracy;

    public static String receiver;

    public static ArrayList<String> arr1 = new ArrayList<>();
    public static ArrayList<String> arr2 = new ArrayList<>();

    public static ArrayList<String> arr3 = new ArrayList<>();

    public static ArrayList<String> arr4 = new ArrayList<>();

    public static ArrayList<Float> arr5 = new ArrayList<>();

    public static ArrayList<Float> arr6 = new ArrayList<Float>();

    public static ArrayList<String> arr7 = new ArrayList<>();

    public static ArrayList<String> arr8 = new ArrayList<>();

    public static Button accept;

    public static Button decline;

    public static String request_sent;

    public static String request_sentID;

    public static float max_trust_value;


    //It's patterns all the way down...

    public Pattern p = Pattern.compile("(\\w+)");//. represents single character
    public Matcher matching;


    public boolean present = false;

    public float MAX_VALUE = (float) Math.pow(10, 4);

    public EditText message;

    public List<String> spinnerArray = new ArrayList<String>();
    public List<String> spinnerArray2 = new ArrayList<String>();
    private MediaPlayer button_sound;

    public static boolean find_string(String str, List<String> container) {

        for (String val : container) {

            if (val == str) {

                return true;

            }

        }
        return false;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main_lobby);
        scaled_val = -1f;
        max_trust_value = 0;
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
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
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position,
                                            long id) {

                        //Use the list position
                        ListAdapter adapter1 = listview.getAdapter();
                        //Get the username of the friend located at that position
                        receiver = arr3.get(position);
                        Intent intent = new Intent(MainLobby.this, MainActivity.class);
                        //based on item add info to intent
                        startActivity(intent);
                    }
                });

        ListView listview2 = findViewById(R.id.ItemList);
        listview2.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position,
                                            long id) {
                        //Use the list position
                        ListAdapter adapter1 = listview2.getAdapter();
                        String user_to_connect = adapter1.getItem(position).toString();
                        request_sentID = arr4.get(position);
                        //Get the username of the friend located at that position
                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        String message_sent = "Do you want to connect with " + username1 + "?";
                        mDatabase.child("Users").child(request_sentID).child("Requests").setValue(message_sent);
                        mDatabase.child("Users").child(request_sentID).child("Requested").setValue(usernameID);
                    }
                });

        ListView listview3 = findViewById(R.id.ItemList3);

        listview3.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapter, View view, int position,
                                            long id) {
                        //Use the list position
                        ListAdapter adapter1 = listview3.getAdapter();
                        String user_to_connect = adapter1.getItem(position).toString();
                        request_sentID = arr7.get(position);
                        LinearLayout layout1 = findViewById(R.id.linearLayout3);

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
                        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID);
                        mDatabase.child("Users").child(usernameID).child("Requests").removeValue();
                        mDatabase.child("Users").child(usernameID).child("Requested").removeValue();
                        mDatabase2.child("Friends").child(who).setValue(request_sentID);
                        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users").child(request_sentID);
                        mDatabase3.child("Friends").child(username1).setValue(usernameID);
                        final String[] who_username = {""};
                        //Extract the username associated with a user ID
                        accept.setVisibility(GONE);
                        decline.setVisibility(GONE);
                    }
                });

        listview3.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext(), listview3));
        //Made sure that it
        //runs as a background thread
        //Listen for messages

        //Okay, the mechanism for extracting responses has been set-up.
        //The problem now is accumulating messages, as messages get overwritten
        //My approach to solving this problem will be to introduce a button that allows
        //you to go back 1 message back in time
        //and another button that allows you to go 1 message forward in time
        //and finally a input interface, using a list of people you are connected with,
        //that will allow you to get their latest message
        //A messaging system similar to IMessage might be implemented later,
        //but I really don't want my app to be like to other messaging apps
        //especially since I have the choice of making it however I want it to be

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
        button_sound = MediaPlayer.create(this, R.raw.select);

        button_sound.start();
        if (!button_sound.isPlaying()) {
            button_sound.release();
        }
        //Erase what was written
        editText.setText("");
        float match_score = 0f;
        //60% is the trust score
        //40% is the similarity score
        ListView listview = findViewById(R.id.ItemList);
        ArrayAdapter<String> adapter1 = (ArrayAdapter<String>) listview.getAdapter();
        ArrayList<Float> scores = new ArrayList<Float>();
        DatabaseReference trust_list = FirebaseDatabase.getInstance().getReference().child("Scaled Trust:");
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
            }
        });
        if (adapter1 != null) {
            //For loop to analyze the contents of the ArrayAdapter
            for (int x = 0; x < adapter1.getCount(); x++) {

                float similarity1 = similarity(adapter1.getItem(x), search1);
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
                .addOnCompleteListener(MainLobby.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainLobby.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
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
                        } else if (snapshot.getKey().equals(usernameID)) {

                            scaled_val = (float) snapshot.getValue();
                            //Display the scaled trust value
                            TextView display = findViewById(R.id.textView7);
                            String net_display = "Score:" + scaled_val;
                            display.setText(net_display);

                        }
                        //Play a winning sound when estimated trustability rises!
                        if (scaled_val != -1f && scaled_val < (float) snapshot.getValue() && snapshot.getKey().equals(usernameID)) {

                            MediaPlayer win = MediaPlayer.create(getApplicationContext(), R.raw.win);
                            win.start();
                            if (!win.isPlaying()) {
                                win.release();
                            }

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
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot == null) {
                            return;
                        }
                        for (DataSnapshot child : dataSnapshot.getChildren()) {

                            if (child != null) {

                                //Make sure to not be able to friend oneself
                                if (!child.getKey().equals(usernameID)) {
                                    arr1.add((String) child.getValue().toString());
                                    arr4.add((String) child.getKey());
                                } else if (arr1 == null || arr1.size() == 0) {
                                    return;
                                }
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);

                        if (arr1.size() > adapter.getCount()) {

                            for (String s : arr1) {
                                spinnerArray.add(s);
                            }
                            //Sort the spinnerArray values from highest scaled trust level
                            //to lowest scaled trust level
                            if (arr5.size() != 0) {
                                spinnerArray.sort(Comparator.comparingDouble(arr5::indexOf));
                            }

                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            ListView PeopleList = (ListView) findViewById(R.id.ItemList);
                            adapter.notifyDataSetChanged();
                            PeopleList.setAdapter(adapter);
                            arr1 = new ArrayList<String>();
                            arr4 = new ArrayList<String>();
                            arr5 = new ArrayList<Float>();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
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

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        Dictionary<String, Integer> dict = (Dictionary<String, Integer>) dataSnapshot.getValue();
                        //Text of the Document
                        String textToWrite = "bla bla bla";

                        //Checking the availability state of the External Storage.
                        String state = Environment.getExternalStorageState();
                        if (!Environment.MEDIA_MOUNTED.equals(state)) {

                            //If it isn't mounted - we can't write into it.
                            return;
                        }

                        //Create a new file that points to the root directory, with the given name:
                        File file = new File(getExternalFilesDir(null), "GenuineTrustPredictor.ptl");

                        //This point and below is responsible for the write operation
                        FileOutputStream outputStream = null;
                        try {
                            file.createNewFile();
                            //second argument of FileOutputStream constructor indicates whether
                            //to append or create new file if one exists
                            outputStream = new FileOutputStream(file, true);
                            //Create a ptl file:
                            outputStream.write(dict.toString().getBytes());
                            outputStream.flush();
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        module = Module.load("GenuineTrustPredictor.ptl");
                        //Extract the best performing model, and use it!
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
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
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() == null) {
                                    return;
                                }
                                max_trust_value = (float) dataSnapshot.getValue();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
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
                        mDatabase2.child("Scaled Trust:").child(snapshot.getKey()).setValue(scaled_trust);
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
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
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }
                        //Extracted the accuracy, tada!!
                        accuracy = (float) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
            }
        });

    }

    public void listen_for_requests() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");

                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainLobby.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            usernameID = user.getUid();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        }
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID).child("Requests");
        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID).child("Requested");
        final Handler handler = new Handler();
        String[] request_sent1 = {""};
        handler.post(new Runnable() {
            @Override
            public void run() {

                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() == null) {
                            return;
                        }

                        request_sent1[0] = dataSnapshot.getValue().toString();
                        request_sent = request_sent1[0];
                        MediaPlayer notif = MediaPlayer.create(getApplicationContext(), R.raw.notification);
                        notif.start();
                        if (!notif.isPlaying()) {
                            notif.release();
                        }
                        ListView listview_ = findViewById(R.id.ItemList3);
                        matching = p.matcher(request_sent);
                        String who = getName(request_sent, 7);
                        arr8.add(who);
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                MainLobby.this, android.R.layout.simple_spinner_item, arr8);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.notifyDataSetChanged();
                        listview_.setAdapter(adapter);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
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
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                            return;
                        }
                        request_sent1[0] = dataSnapshot.getValue().toString();
                        TextView display = findViewById(R.id.textView);
                        display.setVisibility(VISIBLE);
                        display.setText("Your request to friend " + request_sent1[0] + " was rejected.");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);
            }
        });

    }

    //Now, my algorithm sorts people based on how trustable they can be
    public void listen_for_friends() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(usernameID)
                .child("Friends");

        final Handler handler = new Handler();
        String[] request_sent1 = {""};
        DatabaseReference trust_list = FirebaseDatabase.getInstance().getReference().child("Scaled Trust:");
        handler.post(new Runnable() {
            @Override
            public void run() {
                mDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                            return;
                        }
                        for (DataSnapshot dS : dataSnapshot.getChildren()) {
                            arr2.add((String) dS.getKey());
                            arr3.add((String) dS.getValue());
                        }

                        if (arr2 == null) {
                            return;
                        }

                        for (String s : arr2) {
                            spinnerArray2.add(s);
                        }

                        trust_list.addChildEventListener(new ChildEventListener() {


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
                                //Make sure this individual is amongst your friends list...
                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                System.out.println("The read failed: " + databaseError.getCode());
                            }

                        });
                        if (arr6.size() != 0) {
                            //Sort friends from most trustable to least trustable
                            spinnerArray.sort(Comparator.comparingInt(arr6::indexOf));
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                MainLobby.this, android.R.layout.simple_spinner_item, spinnerArray2);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        ListView PeopleList = (ListView) findViewById(R.id.ItemList2);
                        PeopleList.setAdapter(adapter);
                        arr2 = new ArrayList<String>();
                        spinnerArray2 = new ArrayList<String>();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.out.println("The read failed: " + databaseError.getCode());
                    }
                });
                handler.post(this);

            }
        });

    }
}