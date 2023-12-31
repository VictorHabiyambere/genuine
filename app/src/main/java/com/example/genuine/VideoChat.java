package com.example.genuine;

import static com.example.genuine.FirstView.username1;
import static com.example.genuine.MainActivity.usernameID;
import static com.example.genuine.MainLobby.request_sentID;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class VideoChat extends AppCompatActivity {

    public static final Integer RecordAudioRequestCode = 1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final String LOG_TAG = "AudioRecordTest";
    public ImageView image1;
    public static Bitmap theImage;
    public static String net_text;
    private static final String fileName = null;
    public MediaRecorder mediaRecorder = new MediaRecorder();
    public TextView textview;
    private MediaPlayer player;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_video_chat);
        net_text = "Dialogue" + '\n';
        textview = findViewById(R.id.textView8);
        //Add the text
        textview.append(net_text);
        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        image1 = findViewById(R.id.imageView);
        listen_for_image();
        listen_for_audio();
        listen_for_transcribed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS);
        }
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            //An entire dialogue should be in the process of being built...

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                assert data != null;
                net_text = username1 + " said:" + data.get(0) + "/n";
                DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("Video Chat:" + usernameID + " with " + request_sentID).child("Transcription").child(request_sentID).setValue(net_text);
                textview = findViewById(R.id.textView8);
                //Add the text
                textview.append(net_text);
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                //Record audio, send data to real-time database
                startRecording();
                handler.postDelayed(this, 2000);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            assert data != null;
            theImage = (Bitmap) Objects.requireNonNull(data.getExtras()).get("data");
            setDataToDataBase(theImage);
        }
    }

    private void setDataToDataBase(Bitmap theImage) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        //Send camera data to the other user
        mDatabase.child("Video Chat:" + usernameID + " with " + request_sentID)
                .child(request_sentID).setValue(theImage);
    }

    public void listen_for_image() {

        String node = "Video Chat:" + usernameID + " with " + request_sentID;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(node).child(usernameID);
        final Handler handler = new Handler();
        handler.post(
                new Runnable() {
                    public void run() {
                        mDatabase.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() == null) {
                                    return;
                                }
                                theImage = (Bitmap) dataSnapshot.getValue();
                                image1.setImageBitmap(theImage);

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

    public void listen_for_transcribed() {

        String node = "Video Chat:" + usernameID + " with " + request_sentID;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(node).child("Transcription").child(usernameID);
        final Handler handler = new Handler();
        handler.post(

                new Runnable() {
                    public void run() {
                        mDatabase.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() == null) {
                                    return;
                                }

                                String transcribed_speech = (String) dataSnapshot.getValue();
                                textview = findViewById(R.id.textView8);
                                //Add the text
                                textview.append(transcribed_speech);

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

    private void getPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            //Send camera data to the other user
            mDatabase.child("Video Chat:" + usernameID + " with " + request_sentID)
                    .child("Audio:" + request_sentID).setValue(player);

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public void listen_for_audio() {

        String node = "Video Chat:" + usernameID + " with " + request_sentID;
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child(node).child("Audio" + usernameID);
        Handler handler = new Handler();
        handler.post(
                new Runnable() {
                    public void run() {

                        mDatabase.addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() == null) {
                                    return;
                                }
                                player = (MediaPlayer) dataSnapshot.getValue();
                                startPlaying(player);

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

    private void startPlaying(MediaPlayer player1) {
        try {
            player1.setDataSource(fileName);
            player1.prepare();
            player1.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }


    private void startRecording() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
            getPlaying();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

}
