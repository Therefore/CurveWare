package com.trafficapptechnologies.curveware;

import android.icu.text.LocaleDisplayNames;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RouteSummary extends AppCompatActivity {

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
