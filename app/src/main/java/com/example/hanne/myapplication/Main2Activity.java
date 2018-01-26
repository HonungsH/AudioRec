package com.example.hanne.myapplication;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {

    private int frequency = 44100, channelConfiguration = AudioFormat.CHANNEL_IN_MONO, audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int blockSize = 1024;
    private boolean started = true;
    private int sampling = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //Button audiobut = (Button) findViewById(R.id.audio);
        //audiobut.setOnClickListener();
        Log.e(MainActivity.DEBUG, "akjidjiasdas");
        RecordAudio record = new RecordAudio();
        record.doInBackground();
    }

    public class RecordAudio {


        protected void doInBackground() {

            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

                Log.e(MainActivity.DEBUG, "I am hereeeeweee");
                //int bufferSize = AudioRecord.getMinBufferSize(frequency,
                //  channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);
                Log.e(MainActivity.DEBUG, "GAY");
                short[] buffer = new short[blockSize];
                //double[] toTransform = new double[blockSize];


                audioRecord.startRecording();


                // started = true; hopes this should true before calling
                // following while loop

                while (started) {
                    sampling++;

                    double[] re = new double[blockSize];
                    double[] im = new double[blockSize];

                    double[] newArray = new double[blockSize * 2];
                    double[] magns = new double[blockSize];

                    double MaxMagn = 0;
                    double pitch = 0;
                    Log.e("", "YOLOOOOOOOOOOOOOOOOOOOO");

                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);


                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        re[i] = (double) buffer[i] / 32768.0; // signed   16bit
                        im[i] = 0;
                    }

                    newArray = FFTbase.fft(re, im, true);

                    for (int i = 0; i < newArray.length; i += 2) {

                        re[i / 2] = newArray[i];
                        im[i / 2] = newArray[i + 1];
                        magns[i / 2] = Math.sqrt(re[i / 2] * re[i / 2] + im[i / 2] * im[i / 2]);
                    }

                    // I only need the first half

                    for (int i = 0; i < (magns.length) / 2; i++) {
                        if (magns[i] > MaxMagn) {
                            MaxMagn = magns[i];
                            pitch = i;
                        }
                    }
                    if (sampling > 50) {
                        Log.i("pitch and magnitude", "" + MaxMagn + "   " + pitch * 15.625f);
                        sampling = 0;
                        MaxMagn = 0;
                        pitch = 0;
                    }
                    Log.e(MainActivity.DEBUG, "HEPPIDEPP");

                }

                audioRecord.stop();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed", t);
            }

        }
    }
}
