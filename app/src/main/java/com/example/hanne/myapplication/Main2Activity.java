package com.example.hanne.myapplication;

import android.content.pm.ActivityInfo;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import static com.example.hanne.myapplication.MainActivity.DEBUG;
import static java.lang.Math.log10;

public class Main2Activity extends AppCompatActivity {

    private int frequency = 44100, channelConfiguration = AudioFormat.CHANNEL_IN_MONO, audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int blockSize = 1024;
    private boolean started = true;
    private int sampling = 0;
    protected GraphView graph;
    private Thread recordingThread;
    private LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
    private ArrayList<DataPoint> frequencyPoints = new ArrayList<>();
    private int findFrequency = 1000;
    private static final float MAX_REPORTABLE_AMP = 32767f;
    private static final float MAX_REPORTABLE_DB = 90.3087f;
    private final static double a2dScalar = 20.0 / Math.log(10.0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //Button audiobut = (Button) findViewById(R.id.audio);
        //audiobut.setOnClickListener();
        Log.e(DEBUG, "akjidjiasdas");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3)
        });

        graph.addSeries(series);

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(DEBUG, "before creating method");
                    RecordAudio record = new RecordAudio();
                    record.doInBackground();
                    Log.e(DEBUG, "created, should not be here");
                } catch(Exception e) {
                    Log.e(DEBUG, "ERROR", e);
                }
            }
        });

        recordingThread.start();
    }

    private void plot(double[] magnitudes, int N) {
        /*
        graph.removeAllSeries();

        // 500 = i * 44100 / N;
        // 500 * N = i * 44100
        // i = (500 * N) / 44100
        int startFrequency = 500;
        int stopFrequency = 1500;

        series = new LineGraphSeries<>();

        int start = (startFrequency * N) / frequency;
        int stop = Math.min(magnitudes.length / 2, (stopFrequency * N) / 44100);

        //DataPoint[] dataPoints = new DataPoint[stop - start];

        for (int i = (startFrequency * N) / frequency; i < magnitudes.length / 2 && i < (stopFrequency * N) / 44100; i++) {
            double currentFrequency = (i * frequency / N);
            double currentMagnitude = magnitudes[i];

            series.appendData(new DataPoint(currentFrequency, currentMagnitude), false, stop - start + 1);
            //dataPoints[i - start] = new DataPoint(currentFrequency, currentMagnitude);
        }

        //series.resetData(dataPoints);
        graph.addSeries(series);

        /*
        for (int i = 0; i < magnitudes.length / 2; i++) {
            boolean added = false;

            double currentFrequency = (i * frequency / N);
            double currentMagnitude = magnitudes[i];

            while (frequencyPoints.size() <= i) {
                frequencyPoints.add(new DataPoint(currentFrequency, currentMagnitude));
                added = true;
                Log.e(MainActivity.DEBUG, "added to array, size " + frequencyPoints.size() + " i is " + i);
            }

            if (!added) {
                //Log.e(MainActivity.DEBUG, "did not add to array, size " + frequencyPoints.size() + " i is " + i);

                boolean updateValue = false;
                DataPoint currentData = frequencyPoints.get(i);
                double compare = currentFrequency - currentData.getX();

                if (Math.abs(compare) > 0.001) {
                    updateValue = true;
                }

                compare = currentMagnitude - currentData.getY();

                if (Math.abs(compare) > 0.001) {
                    updateValue = true;
                }

                if (updateValue) {
                    frequencyPoints.set(i, new DataPoint(currentFrequency, currentMagnitude));
                }
            }
        }

        list[0] = new DataPoint(1, 2);
*/
        //DataPoint[] asList = frequencyPoints.toArray(new DataPoint[frequencyPoints.size()]);

        /*
        //LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
        for (int i = 0; i < magnitudes.length / 2; i++) {
            double currentFrequency = (i * frequency / N);
            double currentMagnitude = magnitudes[i];

            series.appendData(new DataPoint(currentFrequency, currentMagnitude), true, magnitudes.length / 2);
        }

        graph.addSeries(series);
        */
    }

    public class RecordAudio {


        protected void doInBackground() {

            try {
                int bufferSize = AudioRecord.getMinBufferSize(frequency,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

                Log.e(DEBUG, "I am hereeeeweee");
                //int bufferSize = AudioRecord.getMinBufferSize(frequency,
                //  channelConfiguration, audioEncoding);

                AudioRecord audioRecord = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, frequency,
                        channelConfiguration, audioEncoding, bufferSize);
                Log.e(DEBUG, "GAY");
                short[] buffer = new short[blockSize];
                //double[] toTransform = new double[blockSize];


                audioRecord.startRecording();



                // started = true; hopes this should true before calling
                // following while loop

                while (started) {
                    sampling++;

                    double[] re = new double[blockSize];
                    double[] im = new double[blockSize];

                    double[] frequencies = new double[blockSize * 2];
                    double[] magns = new double[blockSize];

                    double MaxMagn = 0;
                    double pitch = 0;
                    //Log.e("", "YOLOOOOOOOOOOOOOOOOOOOO");

                    int bufferReadResult = audioRecord.read(buffer, 0,
                            blockSize);


                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        re[i] = (double) buffer[i] / 32768.0; // signed   16bit
                        im[i] = 0;
                    }
                    double sum =0;
                    for (int i = 0; i < bufferReadResult; i++){
                        sum += Math.abs(buffer[i]);
                    }
                    sum /= bufferReadResult;
                    frequencies = FFTbase.fft(re, im, true);

                    for (int i = 0; i < frequencies.length; i += 2) {

                        re[i / 2] = frequencies[i];
                        im[i / 2] = frequencies[i + 1];
                        magns[i / 2] = Math.sqrt(re[i / 2] * re[i / 2] + im[i / 2] * im[i / 2]);
                    }

                    // I only need the first half

                    /*
                        500 = i * 44100 / blockSize;
                        500 * blockSize = i * 44100;
                        (500 * blockSize) / 44100 = i
                     */

                    //long start = Math.round((18900 * blockSize) / (double)44100);
                    //long stop  = Math.round((19100 * blockSize) / (double)44100);

                    long start = 0;
                    long stop = (magns.length - 1) / 2;

                    double totalMagnitude = 0.0;
                    double maxMagnitude = 0.0f;
                    double actualFrequency = 0;

                    for (int i = (int)start; i <= stop; i++) {
                        totalMagnitude += magns[i];

                        //Log.e(MainActivity.DEBUG, "Magnitude: " + magns[i]);
                        //Log.e(MainActivity.DEBUG, "Frequency: " + ((i * frequency) / blockSize));

                        if (magns[i] > maxMagnitude) {
                            maxMagnitude = magns[i];
                            actualFrequency = i * 44100 / blockSize;
                        }
                    }

                    //if (true)
                    //    break;

                    if (sampling > 20)
                        Log.e(MainActivity.DEBUG, "Frequency: " + actualFrequency + " with magnitude: " + maxMagnitude + " and total magnitude of: " + totalMagnitude);

                    for (int i = 0; i < re.length; i++) {
                        if (i >= start && i <= stop) {
                            continue;
                        }

                        re[i] = 0.0f;
                        im[i] = 0.0f;
                    }

                    frequencies = FFTbase.fft(re, im, false);

                    for (int i = 0; i < frequencies.length; i += 2) {
                        re[i / 2] = frequencies[i];
                        im[i / 2] = frequencies[i + 1]; // should be 0
                    }

                    double sum_im = 0.0f;

                    for (double current_im : im) {
                        sum_im += current_im;
                    }

                    //Log.e(MainActivity.DEBUG, "sum_im after ifft " + sum_im);

                    double sum_amplitude = 0.0f;

                    /*
                    for (int i = start + 1; i <= stop + 1; i++) {
                        double current_re = re[i];

                        sum_amplitude += current_re;//Math.abs(current_re);

                        //Log.e(MainActivity.DEBUG, "RE: " + current_re);
                        //Log.e(MainActivity.DEBUG, "Frequency: " + ((i * frequency) / blockSize));
                    }

*/
                    //if (true)
                    //    break;

                    sum_amplitude /= blockSize;

                    //if (sum_amplitude < 0.1)
                     //   continue;

                    //Log.e(MainActivity.DEBUG,"amp" + sum_amplitude);

                    //Log.e(MainActivity.DEBUG, "amp after ifft " + sum_amplitude);

                    double dbamplitude;
                    double decibel;

                    //Log.e(DEBUG, "Entered displayDecibel");

                    double dbFFT = MAX_REPORTABLE_DB + 20 * Math.log10(maxMagnitude);

                    if (sampling > 20)
                        Log.e(MainActivity.DEBUG, "DB: " + dbFFT);

                    dbamplitude = (float) (MAX_REPORTABLE_DB + (20 * Math.log10(sum / MAX_REPORTABLE_AMP)));
                    decibel = Math.log(dbamplitude) * a2dScalar;
                    //Log.e(MainActivity.DEBUG, "Calculated dB to" + String.valueOf(decibel));
                    //Log.e(MainActivity.DEBUG, "Calculated A to " + String.valueOf(dbamplitude));
                    /*
                    for (int i = 0; i < (magns.length) / 2; i++) {
                        if (magns[i] > MaxMagn) {
                            MaxMagn = magns[i];
                            pitch = i;
                        }
                    }
                    */


                    //Log.e(DEBUG, "Magnitude: " + totalMagnitude);
                    //Log.e(DEBUG, "Frequency: " + actualFrequency + " with magnitude: " + maxMagnitude);

                    if (sampling > 20) {

                        //Log.e("pitch and magnitude", "" + MaxMagn + "   " + pitch * 15.625f);
                        //Log.e(MainActivity.DEBUG, "freq: " + (pitch * frequency / blockSize));
                        sampling = 0;
                        MaxMagn = 0;
                        pitch = 0;

                    }
                    //Log.e(MainActivity.DEBUG, "HEPPIDEPP");

                    //plot(magns, blockSize);

                }

                audioRecord.stop();

            } catch (Throwable t) {
                t.printStackTrace();
                Log.e("AudioRecord", "Recording Failed", t);
            }

        }
    }
}
