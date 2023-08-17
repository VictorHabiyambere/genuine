package com.example.genuine;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.genuine.MainLobby.accuracy;
import static com.example.genuine.MainLobby.module;
import static java.util.Arrays.sort;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.pytorch.IValue;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

    public static Bitmap sent_image = null;

    public static int messages_sent = 0;

    public static Uri sent_video = null;
    public static ArrayList<String> arr1;

    //Note: I will use the raw values, and scale them when processing trust levels
    public static boolean state;
    public static String receiver;
    public static float actual_trustability = 0;
    public Pattern p = Pattern.compile("(\\w+)");
    public Matcher matching;
    public String username_request = "";
    public Button accept;
    public Button decline;
    public boolean present = false;
    public List<String> spinnerArray;
    private MediaPlayer button_sound;

    public static float calc_distance(String message_pred, String message) {
        int word_right = 0;
        for (int i = 0; i < message.length(); i++) {
            if (message_pred.charAt(i) == message.charAt(i)) {
                word_right += 1;
            }
        }
        word_right += 1;
        return word_right / message.length();
    }

    public static float calc_trust(String message, String actual_response) {

        //Infer what the user will say using my chatbot module
        byte[] encodeByte = Base64.decode(message, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(bitmap
                ,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);
        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        String message_pred = outputTensor.toString();
        //Measure trustability
        float trustability = accuracy * calc_distance(message_pred, actual_response);
        return trustability;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        messages_sent = 0;
        listen_for_messages();

    }
    public void listen_for_messages() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final Handler handler = new Handler();
        String[] message_sent1 = new String[1];
        LinearLayout linearLayout = findViewById(R.id.linearLayout5);
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
                            } else if (middle[0].equals("messaging")) {
                                username2[0] = getName(ds1.getKey(), 2);
                                not_found[0] = false;
                                ds = ds1;
                                break;
                            }
                        }

                        if (!not_found[0]) {

                            //Check if the username is present in this key
                            if (username1[0].equals(usernameID)) {
                                String message1 = "";

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
                                Drawable d = new Drawable() {
                                    @Override
                                    public void draw(@NonNull Canvas canvas) {

                                    }

                                    @Override
                                    public void setAlpha(int alpha) {

                                    }

                                    @Override
                                    public void setColorFilter(@Nullable ColorFilter colorFilter) {

                                    }

                                    @Override
                                    public int getOpacity() {
                                        return PixelFormat.UNKNOWN;
                                    }
                                };
                                d = ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox).getConstantState().newDrawable();
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
                                String message1 = "";
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
                                Drawable d = new Drawable() {
                                    @Override
                                    public void draw(@NonNull Canvas canvas) {

                                    }

                                    @Override
                                    public void setAlpha(int alpha) {

                                    }

                                    @Override
                                    public void setColorFilter(@Nullable ColorFilter colorFilter) {

                                    }

                                    @Override
                                    public int getOpacity() {
                                        return PixelFormat.UNKNOWN;
                                    }
                                };
                                d = ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox).getConstantState().newDrawable();
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
                    handler.post(this);
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
        for (int i = 0; i < N && m.find(); i++) {
        }

        if (!m.find()) {
            return "";
        }

        return m.group(1);
    }

    //Allow users to select images and send them so a friend

    public void get_image(View view) {

        if (Build.VERSION.SDK_INT <= 19) {

            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, GALLARY_IMAGE);

        } else if (Build.VERSION.SDK_INT > 19) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLARY_IMAGE);
        }

    }

    public void get_video(View view) {

        button_sound = MediaPlayer.create(this, R.raw.select);

        button_sound.start();
        button_sound.release();

        if (Build.VERSION.SDK_INT <= 19) {

            Intent i = new Intent();
            i.setType("video/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(i, PICK_VIDEO);

        } else if (Build.VERSION.SDK_INT > 19) {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_VIDEO);
        }


    }

    private String getRealPathFromURI(Uri contentURI) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            filePath = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            filePath = cursor.getString(idx);
            cursor.close();
        }
        return filePath;
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
                String selectedImagePath = getRealPathFromURI(selectedImageUri);

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
        button_sound.release();
        if (message_sent == "") {
            return;
        }
        FirebaseDatabase.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        String[] users = new String[]{receiver, usernameID};
        sort(users);
        String setup = users[0] + " messaging " + users[1];
        //Send the sent message to the real-time database
        LinearLayout linearLayout = findViewById(R.id.linearLayout5);
        TextView display = new TextView(getApplicationContext());
        display.setText(message_sent);
        Drawable d = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.UNKNOWN;
            }
        };
        d = ContextCompat.getDrawable(MainActivity.this, R.drawable.messagebox).getConstantState().newDrawable();
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

    ArrayList<String> getFriends(DatabaseReference db) {

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot == null) {
                    return;
                }
                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    if (child != null) {

                        arr1.add((String) child.getValue());

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
        return arr1;


    }

    //I don't know how to configure it such that when a list item is clicked,
    //the message system will be successfully set-up...
    //but I will figure this out when I learn more about android listviews

    //Here,
    public void get_friends(View view) {

        //The username chosen by the user will be selected!!

        DatabaseReference friends_list = FirebaseDatabase.getInstance().getReference().child(usernameID).child("Friends");

        arr1 = getFriends(friends_list);

        for (String s : arr1) {
            spinnerArray.add(s);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray) {
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View v = null;

                if (position == 0) {
                    TextView tv = new TextView(getContext());
                    tv.setHeight(0);
                    tv.setVisibility(View.GONE);
                    v = tv;
                } else {

                    v = super.getDropDownView(position, null, parent);
                }

                parent.setVerticalScrollBarEnabled(false);
                return v;
            }
        };
        ListView FriendsList = findViewById(R.id.ItemList2);
        FriendsList.setAdapter(adapter);
        state = !state;

        if (state && arr1.size() != 0) {
            FriendsList.setVisibility(VISIBLE);
        } else if (!state) {
            FriendsList.setVisibility(GONE);
        }

        arr1 = new ArrayList<String>();

    }


}