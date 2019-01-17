package com.trafficapptechnologies.curveware;

import android.content.Intent;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RouteSummary extends AppCompatActivity {

    //UI Elements
    Button sendButton;

    //UI elements
    TextView distanceTextView, averageSpeedTextView, durationTextView, curveynessFactor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_summary);

        distanceTextView = findViewById(R.id.distanceTV);
        averageSpeedTextView = findViewById(R.id.averageSpeedTV);
        durationTextView = findViewById(R.id.durationTV);
        curveynessFactor = findViewById(R.id.curveynessFactorTV);
        distanceTextView.setText(distanceFormat(Datapoint.getDistance()));
        durationTextView.setText(Long.toString(Datapoint.getDuration()));
        sendButton = findViewById(R.id.sendBTN);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //This actually attaches the file that was created. It can be shared to any application using the switcher.
                Toast.makeText(RouteSummary.this, "Test Button Pressed", Toast.LENGTH_SHORT).show();
                //generateNoteOnSD(RunningRoute.this,"emailfile","this is the body");
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                //emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "subject here");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "body text");
                File root = new File(Environment.getExternalStorageDirectory(), "Curveware Files");
                String pathToMyAttachedFile = "emailfile";
                File file = new File(root, pathToMyAttachedFile);
                Log.d("path",file.toString());
                if (!file.exists() || !file.canRead()) {
                    return;
                }
                Uri uri = Uri.fromFile(file);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
            }
        });




        /*        ArrayList<Datapoint> arrayList = new ArrayList<Float>();
        for (int i = 0; i< Datapoint.getCounter(); i++){
            arrayList.add(Datapoint)
        }

        List<Float> speeds = new ArrayList<>();
        for (int i = 0; i>= Datapoint.getCounter(); i++){
            speeds.add(Datapoint.getSpeed());
        }*/
    }

    private String distanceFormat(double d){
        DecimalFormat df = new DecimalFormat("#00.00");
        return df.format(d);
    }
}
