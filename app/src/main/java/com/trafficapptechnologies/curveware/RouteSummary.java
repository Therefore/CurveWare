package com.trafficapptechnologies.curveware;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RouteSummary extends AppCompatActivity implements OnMapReadyCallback{

    //UI Elements
    Button sendButton;
    private android.support.v7.widget.ShareActionProvider mShareActionProvider;

    //UI elements
    TextView distanceTextView, averageSpeedTextView, durationTextView, curveynessFactor;

    //MapElements
    GoogleMap map;
    ArrayList<LatLng> pastPositions;

    //Speed Variables
    ArrayList<Double> speedList = new ArrayList<>();
    double speedMax;
    double speedMin;
    double speedAverage;
    double speedsum;

    //Duration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_summary);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        pastPositions = getIntent().getParcelableArrayListExtra("positionList"); //gets the LatLngs from the previous activity

        //Compute Duration
        long duration = getIntent().getLongExtra("duration",0);
        String durationString = getDisplayValue(duration);

        //Get speeds from previous activity and compute average.
        speedList = (ArrayList<Double>) getIntent().getSerializableExtra("speedList");
        Log.d("speedList", speedList.toString());
        if (!speedList.isEmpty()){ //checks to make sure there is a list
            for (int i = 0; i < speedList.size(); i++){
                speedsum += speedList.get(i); // sums up all of the speed values from the route
            }
            speedAverage = speedsum/speedList.size(); // computes average speed.
        }

/*        if (speedList != null){
            for (int i = 0; i <= speedList.length; i++){
                speedsum += speedsum += speedList[i];
            }
            speedAverage = speedsum/speedList.length;
        }*/
        //Log.d("pastPositions", pastPositions.toString());

        distanceTextView = findViewById(R.id.distanceValueTV);
        averageSpeedTextView = findViewById(R.id.averageSpeedValueTV);
        durationTextView = findViewById(R.id.durationValueTV);
        curveynessFactor = findViewById(R.id.curveynessFactorValueTV);
        distanceTextView.setText(distanceFormat(Datapoint.getDistance()) + " miles");
        durationTextView.setText(durationString);
        sendButton = findViewById(R.id.sendBTN);

        DecimalFormat df = new DecimalFormat("#0");
        averageSpeedTextView.setText(Double.parseDouble(df.format(speedAverage)) + " mph");

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This will need to be updated when updating to Target APK 26 - This code works, but will need to be updated to File Provider
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
                Log.d("path", file.toString());
                if (!file.exists() || !file.canRead()) {
                    return;
                }
                Uri uri = Uri.fromFile(file);
                emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));
            }
        });
    }

    private String distanceFormat(double d){
        DecimalFormat df = new DecimalFormat("#00.00");
        return df.format(d);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        googleMap.setMyLocationEnabled(true);
        drawPolyline();

        Polyline polyline = map.addPolyline(new PolylineOptions()
                .addAll(pastPositions)
                .color(Color.BLUE));
        setMapMarkersBounds(polyline);
        Log.d("Datapoint",Integer.toString(Datapoint.getCounter()));
    }

    //Gets the polylines extents and sets the mapcamera to that section
    //https://stackoverflow.com/a/30117800/6884269
    public void setMapMarkersBounds(Polyline polyline) {
        LatLngBounds.Builder builder;
        float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int padding = (int) (40 * scale + 0.5f);
        builder = new LatLngBounds.Builder();

        for(int i = 0; i < polyline.getPoints().size(); i++){
            builder.include(polyline.getPoints().get(i));
        }
        LatLngBounds bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu, 400, null);
    }

    private void drawPolyline() {
        Log.d("numberOfPositions", Float.toString(pastPositions.size()));
        //numberOfPointsTextView.setText("Points Recorded: " + Float.toString(pastPositionsList.size()));
    }

    public String getDisplayValue(Long ms) {
        Duration duration = Duration.ofMillis(ms);
        Long minutes = duration.toMinutes();
        Long seconds = duration.minusMinutes(minutes).getSeconds();

        return minutes + " min " + seconds + " sec";
    }

    //Share Menu is a cross of the official documentation and this answer:
    //Official Docs:
    //Stack Overflow: https://stackoverflow.com/a/20047716/6884269

   //uncomment below to add share icon to the action bar. Still need to get the user click.
    /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.share_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (android.support.v7.widget.ShareActionProvider) MenuItemCompat.getActionProvider(item);

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }*/
}

