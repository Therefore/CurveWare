package com.trafficapptechnologies.curveware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RunningRoute extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SensorEventListener {


    private GoogleMap mMap;

    private SensorManager sensorManager;

    //Rich Polyline

    //location variables
    LatLng myPosition;
    Float mySpeed;
    private TextView distanceTextView;
    private TextView speedTextView;
    private TextView timeTextView;
    private List<LatLng> latLngList;
    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;
    ArrayList<LatLng> pastPositions = new ArrayList<LatLng>();
    List<LatLng> pastPositionsList = new ArrayList<>();
    private Polyline polyline_path;
    private PolylineOptions polylineOptions_path;
    Boolean isMapReady = false;
    public double earthRadius = 6372.8; // In kilometers
    double distance = 0;
    double traveledDistance = 0;

    //accelerometer variables
    ImageView carTurningLeftImageView;
    ImageView carTurningRightImageView;
    //Float[] accellList = new Float[10];
    ArrayList<Float> accellList = new ArrayList<Float>(10);
    Float[] list = {0f,0f,0f,0f,0f,0f,0f,0f,0f,0f};
    private Float[] accelArray;
    float[] mGravity;
    float[] mGeomagnetic;
    static final float ALPHA = 0.15f;
    Float movingAverage = 0f;
    double inclination;

    //debug
    Button testbutton;
    TextView numberOfPointsTextView;

    //UI Elements
    Button stopButton;
    Boolean stopBoolean = false;
    Button pauseButton;
    Boolean pauseBoolean = false;
    Button resetDistanceButton;
    ImageButton addSubtractDistanceButton;
    Boolean addDistance = true;
    TextToSpeech ttobj;
    Chronometer chronometer;
    long timeWhenStopped = 0;

    //Data
    public static ArrayList<Datapoint> arrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_route);


        distanceTextView = findViewById(R.id.distanceTV);
        speedTextView = findViewById(R.id.speedTV);
        timeTextView = findViewById(R.id.timeTV);
        carTurningLeftImageView = findViewById(R.id.carLeftTurnIV);
        carTurningRightImageView = findViewById(R.id.carRightTurnIV);
        testbutton = findViewById(R.id.test);
        stopButton = findViewById(R.id.stopB);
        pauseButton = findViewById(R.id.pauseB);
        resetDistanceButton = findViewById(R.id.resetDistanceB);
        addSubtractDistanceButton = findViewById(R.id.addSubtractDistanceB);

        chronometer = findViewById(R.id.chronometerC);

        numberOfPointsTextView = findViewById(R.id.numberOfPointsTV);


        final MediaPlayer startSound = MediaPlayer.create(this,R.raw.start);
        startSound.start();


        //TEST BUTTON
        testbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(RunningRoute.this,"Test Button Pressed", Toast.LENGTH_SHORT).show();
            }
        });

        //PAUSE BUTTON
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shortToast("Pause Button Pressed");
                playBeepSound();
                if (!pauseBoolean){
                    pauseBoolean = !pauseBoolean;
                    pauseButton.setText("Resume");
                    pauseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
                    pauseButton.setBackgroundColor(ContextCompat.getColor(RunningRoute.this,R.color.buttonActive));
                    timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                    chronometer.stop();
                    //speakWords("Paused");
                } else {
                    pauseBoolean = !pauseBoolean;
                    pauseButton.setText("Pause");
                    pauseButton.setBackgroundResource(android.R.drawable.btn_default);
                    pauseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP,40);
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometer.start();
                    //speakWords("Resumed");
                }

            }
        });

        //STOP BUTTON
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBeepSound();
                shortToast("Stop Button Pressed");
                Intent i = new Intent(RunningRoute.this,RouteSummary.class);
                startActivity(i);
            }
        });

        //RESET DISTANCE BUTTON
        resetDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distance = 0;
                distanceTextView.setText(Double.toString(distance));
            }
        });

        //ADD OR SUBTRACT DISTANCE
        addSubtractDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addDistance){
                    addSubtractDistanceButton.setImageDrawable(getDrawable(R.drawable.arrow_down_thick));
                    addDistance = !addDistance;
                    playBeepSound();

                } else {
                    addSubtractDistanceButton.setImageDrawable((getDrawable(R.drawable.arrow_up_thick)));
                    addDistance = !addDistance;
                    playBeepSound();
                }

            }
        });


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latituteField.setText("Location not available");
            longitudeField.setText("Location not available");
        }
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Starts the clock
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();

        //init Google TTS
        ttobj=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
            getLinearAcceleration(event);
        }

    }

    private void getLinearAcceleration(SensorEvent event) {
        float[] linaccValues = event.values;
        //Movement
        float x = linaccValues[0];
        float y = linaccValues[1];
        float z = linaccValues[2];
        Log.d("linacc", Float.toString(y));
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        Float sum = 0f;

        //Log.d("accellList", accellList.toString());

        int orientation = getResources().getConfiguration().orientation;

        if (accellList.size()>=30){
            accellList.remove(0);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                accellList.add(9,y);
            }else {
                accellList.add(9,x);
            }
            for (int i = 0; i < accellList.size(); i++){
                sum += accellList.get(i);
            }

            /*for (int i =  0; i <accellList.size(); i++){
                Log.d("accelList", accellList.get(i) + " " + i);
            }*/

            Float average = sum/accellList.size();
            //Log.d("average", Float.toString(average));
            TextView textView = findViewById(R.id.degreesTV);
            Double inclinationInDegrees = (average/9.81)*180/Math.PI;
            if (inclinationInDegrees>0){
                carTurningLeftImageView.setVisibility(View.VISIBLE);
                carTurningRightImageView.setVisibility(View.INVISIBLE);
            } else if (inclinationInDegrees<0){
                carTurningRightImageView.setVisibility(View.VISIBLE);
                carTurningLeftImageView.setVisibility(View.INVISIBLE);
            } else {
                carTurningLeftImageView.setVisibility(View.INVISIBLE);
                carTurningRightImageView.setVisibility(View.INVISIBLE);
            }

            String degreesSigFig = String.format("%.0f",Math.abs(inclinationInDegrees));
            textView.setText(degreesSigFig + "°");
            inclination = inclinationInDegrees;
            inclinationInDegrees = Math.abs(inclinationInDegrees);
            if (inclinationInDegrees <= 14){
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this,R.color.greenThreshold));
            } else if (inclinationInDegrees>14 && inclinationInDegrees<=16){
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this,R.color.yellowThreshhold));
            } else if (inclinationInDegrees >16){
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this,R.color.redThreshold));
            }

        }else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE){
                accellList.add(y);
            }else {
                accellList.add(x);
            }
        }



        //degreesTextView.setText(Float.toString(x));
        //Log.d("Sensor Value", Float.toString(y));

/*        TextView textView = findViewById(R.id.degreesTV);
        Double inclinationInDegrees =  Math.abs(Math.asin((y)/9.81)*180/Math.PI);
        String degreesSigFig = String.format("%.1f", inclinationInDegrees);
        textView.setText(degreesSigFig);*/
/*        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= 2) //
        {
            Toast.makeText(this, "Device was shuffed", Toast.LENGTH_SHORT)
                    .show();
        }*/


    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.latitude, myPosition.longitude), 14f));
        Log.d("OnMapReady", "On Map Ready");


      /*  pastPositionsList.add(new LatLng(37.283934, -80.042777));
        pastPositionsList.add(new LatLng(37.284617, -80.042745));
        pastPositionsList.add(new LatLng(37.284617, -80.042745));

        polyline_path = googleMap.addPolyline(new PolylineOptions()
                //.add(new LatLng(37.283934, -80.042777), new LatLng(37.284617, -80.042745), new LatLng(37.284617, -80.042745), new LatLng(37.283644, -80.040868))
                .addAll(pastPositionsList)
                .width(5)
                .color(Color.RED));*/

/*        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(37, -79))
                .title("Marker"));*/

/*        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
        .addAll(pastPositions));
        Log.d("pastPositions", "Size: " + pastPositions.size());
        for (int i = 0; i<pastPositions.size(); i++){
            Log.d("pastPositions", "Past Position index: " + i + " position: " + pastPositions.get(i));
        }*/

        isMapReady = true;
    }

    /* Request updates at startup */
    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 1, 1, this);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH);
    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        int lat = (int) (location.getLatitude());
        int lng = (int) (location.getLongitude());
        //Log.d("location", location.toString());
        LatLng latestLatLng = new LatLng(location.getLatitude(),location.getLongitude());

        if (myPosition!=null){

            if (addDistance){
                distance += haversine(myPosition.latitude,myPosition.longitude,location.getLatitude(),location.getLongitude()); // returns distance in km.
            } else {
                distance -= haversine(myPosition.latitude,myPosition.longitude,location.getLatitude(),location.getLongitude()); // returns distance in km.
            }

            updateDistanceTextView(distance); // converts km to mi and updates textview
        }

        myPosition = latestLatLng;
        //Log.d("myposition", myPosition.toString()); //prints lat and lon
        pastPositionsList.add(myPosition); //updates the list of locations for the polyline

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.latitude, myPosition.longitude), 14f));

        //updates speed textview
        double d = location.getSpeed()*2.23694;
        DecimalFormat df = new DecimalFormat("00");
        speedTextView.setText(df.format(d) + " mph");

        updatePolyline(myPosition);

        String curveDirection;
        if (inclination>0){
            curveDirection = "Left Curve";
        } else if (inclination<0){
            curveDirection = "Right Curve";
        } else {
            curveDirection = "Level";
        }

        timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();

        Datapoint datapoint1 = new Datapoint(1,location.getLatitude(),location.getLongitude(),distance*0.621371,location.getSpeed(), timeWhenStopped ,inclination, curveDirection);
        arrayList.add(datapoint1);

//        Log.d("datapoint: ", datapoint1.toString() + " Number of Dps: " + Datapoint.getCounter());
////        String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyCsvFile.csv"); // Here csv file name is MyCsvFile.csv
//
//        String csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyCsvFile.csv"); // Here csv file name is MyCsvFile.csv
//        CSVWriter writer = new CSVWriter(new FileWriter(csv));
//
//        String [] country = "India#China#United States".split("#");
//
//        writer.writeNext(country);
//
//        writer.close();


        try{
            String filePath = this.getFilesDir().getPath().toString() + "/csvfile.csv";
            //This PC\Galaxy Tab A\Tablet\Android\data\com.trafficapptechnologies.curveware\cache
            Log.d("filepath", filePath);
            File file = new File(getExternalFilesDir(null), "pessoas.csv");
            CSVWriter writer = new CSVWriter(new FileWriter(file));
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[]{"Country", "Capital"});
            data.add(new String[]{"India", "New Delhi"});
            data.add(new String[]{"United States", "Washington D.C"});
            data.add(new String[]{"Germany", "Berlin"});

            writer.writeAll(data); // data is adding to csv

            writer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateDistanceTextView(double distance) {
        double distanceMi = distance*0.621371;
        DecimalFormat df = new DecimalFormat("##0.00");
/*        BigDecimal bd = new BigDecimal(distanceMi);
        bd = bd.round(new MathContext(3));
        bd = bd.movePointRight(1);*/
        if (!pauseBoolean){
            distanceTextView.setText(String.valueOf(df.format(distanceMi)) + " Miles");
        }
    }

    private void updatePolyline(LatLng myPosition) {

        if (isMapReady){
            Log.d("numberOfPositions", Float.toString(pastPositionsList.size()));
            numberOfPointsTextView.setText("Points Recorded: " + Float.toString(pastPositionsList.size()));
            Polyline polyline = mMap.addPolyline(new PolylineOptions()
                    .addAll(pastPositionsList)
                    .color(Color.BLUE));
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    //Short Toast Method
    public void shortToast(String string){
        Toast.makeText(this, string,Toast.LENGTH_SHORT).show();
    }

    //Distance Formula
    //https://rosettacode.org/wiki/Haversine_formula#Java
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }

    //TTS Method
    private void speakWords(String string) {
        ttobj.speak(string,TextToSpeech.QUEUE_FLUSH,null);
    }

    //beep sound
    public void playBeepSound(){
        final MediaPlayer buttonBeep = MediaPlayer.create(this,R.raw.buttonpushbeep);
        buttonBeep.start();
    }
}
