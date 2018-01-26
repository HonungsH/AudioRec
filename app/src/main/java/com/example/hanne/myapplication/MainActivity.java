package com.example.hanne.myapplication;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import static java.lang.Math.log10;



public class MainActivity extends AppCompatActivity {
    private boolean recording_ = false;
    private MediaRecorder recorder_ = null;
    public static final String DEBUG = "DEBUG";
    Thread displayThread;
    TextView display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        MainActivity.this.startActivity(intent);

        Log.e(DEBUG, "Creating watchingButton");
        ImageButton stopbut = findViewById(R.id.stop);
        stopbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //recording_ = true;
                audioActivation();
                display.setText("");

            }
        });
        ImageButton rec = findViewById(R.id.record);
        rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording_)
                    return;

                audioActivation();
            }
        });

       displayThread = new Thread() {
            @Override
            public void run() {
                try {
                    while(!isInterrupted()) {
                        Thread.sleep(500);

                        if (!recording_)
                            continue;



                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplayText();

                            }
                            public void stop() {

                                displayThread.interrupt();



                            }
                        });
                    }
                } catch(InterruptedException e) {
                    Log.e(DEBUG, "displayThread was interrupted", e);
                }
            }
        };

        displayThread.start();

    }

    private void audioActivation() {
        Log.e(DEBUG, "Clicked watchingButton");

        if (recording_) {
            recording_ = false;
            stopRecording();
        }
        else {
            recording_ = true;
            startRecording();
        }
    }

    private void stopRecording() {
        recorder_.stop();
        recorder_.reset();
        recorder_.release();
       recorder_ = null;

        Log.e(DEBUG, "Stopped recording.");
    }

    private String createNewAudioFile() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.3gp";
    }

    private void updateDisplayText() {
        display = findViewById(R.id.display_db);
        int amplitude;
        double db;

        Log.e(DEBUG, "Entered displayDecibel");

        amplitude = recorder_.getMaxAmplitude();
        db = 20 * log10(amplitude / 32767.0);

        Log.e(DEBUG, "Calculated dB to" + String.valueOf(db));
        Log.e(DEBUG, "Calculated A to " + String.valueOf(amplitude));

        display.setText(String.valueOf((int)Math.round(db) + " dB"));
    }

    private void startRecording() {
        if (recorder_ == null) {
            String fileString = createNewAudioFile();
            File file = new File(fileString);

            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(DEBUG, "Could not create file " + fileString, e);
            }

            Log.e(DEBUG, "Starting MediaRecorder in " + fileString);

            recorder_ = new MediaRecorder();
            recorder_.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder_.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder_.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder_.setOutputFile(fileString);

            try {
                recorder_.prepare();
            } catch (IOException e) {
                Log.e(DEBUG, "recorder_.prepare() failed", e);
            }
        }

        recorder_.start();

        Log.e(DEBUG, "Started MediaRecorder. Currently recording audio.");
    }
}