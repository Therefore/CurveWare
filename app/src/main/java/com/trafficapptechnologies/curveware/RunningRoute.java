package com.trafficapptechnologies.curveware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.math.MathUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.DragAndDropPermissions;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.dynamic.SupportFragmentWrapper;
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
import com.otaliastudios.cameraview.CameraView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class RunningRoute extends AppCompatActivity implements OnMapReadyCallback, LocationListener, SensorEventListener {
    private GoogleMap mMap;
    private SensorManager sensorManager;

    //Rich Polyline

    //intents
    String streetNamne = "";
    String routeNumber = "";

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
    ArrayList<String> recordedData = new ArrayList<String>();

    //accelerometer variables
    ImageView carTurningLeftImageView;
    ImageView carTurningRightImageView;
    //Float[] accellList = new Float[10];
    ArrayList<Float> accellList = new ArrayList<Float>(10);
    Float[] list = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
    private Float[] accelArray;
    float[] mGravity;
    float[] mGeomagnetic;
    static final float ALPHA = 0.15f;
    Float previousInlination = 0f;
    Float movingAverage = 0f;
    //ArrayList<Float> movingList = new ArrayList<>(10);
    Float[] movingList;
    float[] movingValues = new float[10];
    //float[] previousValues =  new float[10];
    ArrayList<Float> previousValues = new ArrayList<>();
    ArrayList<Float> currentValues = new ArrayList<>();
    //float[] currentValues = new float[10];
    float[] returnedFilteredValues = new float[3];
    double smoothed = 0;
    double smoothing = 300;
    long lastUpdate = System.currentTimeMillis();
    long lastUpdateX = System.currentTimeMillis();
    int inclination = 0;
    ArrayList<Double> inclinationArray = new ArrayList<>();

    //Route Duration
    Long startTime = System.currentTimeMillis();
    Long endTime;

    //putExtras
    ArrayList<Double> speedList = new ArrayList<>();


    //debug
    Button testbutton;
    TextView numberOfPointsTextView;

    //UI Elements
    Button stopButton;
    Boolean stopBoolean = false;
    Button pauseButton;
    Boolean pauseBoolean = false;
    ImageButton resetDistanceButton;
    ImageButton addSubtractDistanceButton;
    ImageButton imageButton;
    Boolean addDistance = true;
    TextToSpeech ttobj;
    Chronometer chronometer;
    Button testB;
    long timeWhenStopped = 0;
    SeekBar testSeek;
    ImageView redball;
    int seekValue;
    int ballbankLayoutX;
    int ballbankLayoutY;
    Float initialBallX;
    Float initialBallY;
    RelativeLayout ballbankLayout;
    TextView degreesTV;

    Float sum = 0f;
    Float avg = 0f;

    private float xPos, xAccel, xVel = 0.0f;
    private float yPos, yAccel, yVel = 0.0f;
    private float xMax, yMax;
    private Bitmap ball;

    SensorInclinometer sensorInclinometer = new SensorInclinometer();

    //Data
    public static ArrayList<Datapoint> arrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_running_route);
        //CameraView camera = findViewById(R.id.camera);
        //camera.setLifecycleOwner(this);

        streetNamne = getIntent().getStringExtra("streetName");
        routeNumber = getIntent().getStringExtra("routeNumber");
        //shortToast(streetNamne);
        //shortToast(routeNumber);

        //ball tutorial
        BallView ballView = new BallView(this);
        //setContentView(ballView);

        Point size = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(size);
        xMax = (float) size.x - 100;
        yMax = (float) size.y - 100;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        recordedData.add("Curve Direction" + "," + "Inclination" + "," + "Speed" + "," + "Latitude" + "," + "Longitude" + "," + "\n\r");

        distanceTextView = findViewById(R.id.distanceTV);
        speedTextView = findViewById(R.id.speedTV);
        timeTextView = findViewById(R.id.timeTV);
        //carTurningLeftImageView = findViewById(R.id.carLeftTurnIV);
        //carTurningRightImageView = findViewById(R.id.carRightTurnIV);
        testbutton = findViewById(R.id.test);
        stopButton = findViewById(R.id.stopB);
        pauseButton = findViewById(R.id.pauseB);
        resetDistanceButton = findViewById(R.id.resetDistanceB);
        addSubtractDistanceButton = findViewById(R.id.addSubtractDistanceB);
        testB = findViewById(R.id.testB);
        testSeek = findViewById(R.id.seekBar);
        redball = findViewById(R.id.redBall);
        chronometer = findViewById(R.id.chronometerC);
        numberOfPointsTextView = findViewById(R.id.numberOfPointsTV);
        ballbankLayout = findViewById(R.id.ballbanklayout);
        degreesTV = findViewById(R.id.degreesTV);
        //cameraKitView = findViewById(R.id.camera);

        initialBallX = redball.getX();
        initialBallY = redball.getY();


        final MediaPlayer startSound = MediaPlayer.create(this, R.raw.start);
        startSound.start();


        //TEST BUTTON
        testbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        //SEEK BAR
        testSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                seekValue = i / 5;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                shortToast(Integer.toString(seekValue));
                Log.d("SeekValue", Integer.toString(seekValue));
            }
        });


        //PAUSE BUTTON
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shortToast("Pause Button Pressed");
                playBeepSound();
                if (!pauseBoolean) {
                    pauseBoolean = !pauseBoolean;
                    pauseButton.setText("Resume");
                    pauseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    pauseButton.setBackgroundColor(ContextCompat.getColor(RunningRoute.this, R.color.buttonActive));
                    timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();
                    chronometer.stop();
                    speakWords("Paused");
                } else {
                    pauseBoolean = !pauseBoolean;
                    pauseButton.setText("Pause");
                    pauseButton.setBackgroundResource(android.R.drawable.btn_default);
                    pauseButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chronometer.start();
                    speakWords("Resumed");
                }

            }
        });

        //STOP BUTTON
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playBeepSound();
                endTime = System.currentTimeMillis();
                Long duration = endTime - startTime;
                //shortToast("Stop Button Pressed");
                Intent i = new Intent(RunningRoute.this, RouteSummary.class);
                i.putExtra("positionList", (Serializable) pastPositionsList);
                i.putExtra("duration", duration);
                i.putExtra("speedList", speedList);
                startActivity(i);
            }
        });

        //RESET DISTANCE BUTTON
        resetDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                distance = 0;
                distanceTextView.setText(Double.toString(distance));
                speakWords("Distance Reset");
            }
        });


        //ADD OR SUBTRACT DISTANCE
        addSubtractDistanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addDistance) {
                    addSubtractDistanceButton.setImageDrawable(getDrawable(R.drawable.arrow_down_thick_white));
                    addDistance = !addDistance;
                    //playBeepSound();
                    speakWords("Counting Down.");

                } else {
                    addSubtractDistanceButton.setImageDrawable((getDrawable(R.drawable.arrow_up_thick_white)));
                    addDistance = !addDistance;
                    //playBeepSound();
                    speakWords("Counting Up");
                }

            }
        });

        Environment.getExternalStorageState();
        testB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date currentTime = Calendar.getInstance().getTime();
                //File name should eventually be the route name and number
                generateNoteOnSD(RunningRoute.this,  streetNamne + " Route " + routeNumber + " Date: " + currentTime.toString() + ".txt","");
            }
        });


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the location provider -> use default
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

        //Sets Header for text file

        //init Google TTS
        ttobj = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            getLinearAcceleration(event);
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED){
         getAccelerometerUncalibrated(event);
            //getAccelerometer(event);
        }

    }

    private void  getAccelerometerUncalibrated(SensorEvent event){
        Log.d("uncalibrated: ", event.toString());
        float[] values = event.values;

        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

/*        Double testAngle =  Math.atan(y/(Math.sqrt(Math.pow(x,2) + Math.pow(z,2))));
        Log.d("testAngle: ", Double.toString(testAngle));*/
    }

    private void getLinearAcceleration(SensorEvent event) {
        float[] linaccValues = event.values;
        //Movement
        float x = linaccValues[0];
        float y = linaccValues[1];
        float z = linaccValues[2];
        Log.d("linacc", Float.toString(y));
    }

    @SuppressLint("SetTextI18n")
    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;

        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //Log.d("x", Float.toString(x));

        // If the device is sideways it will use y for x.
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            x = y;
        }

        //double smoothedX = smoothedX(x);

        //SIMPLE ACCELEROMETER TO DEGREES CONVERTER:
        //https://stackoverflow.com/a/15149421/6884269
        float[] g = new float[3];
        g = event.values.clone();
        double norm_Of_g = Math.sqrt(g[0] * g[0] + g[1] * g[1]); // had to take out + g[2] * g[2] so it wouldn't calculate  higher tile when changing speed.
        // Normalize the accelerometer vector
        g[0] = (float) (g[0] / norm_Of_g);
        g[1] = (float) (g[1] / norm_Of_g);
        g[2] = (float) (g[2] / norm_Of_g);
        inclination = (int) Math.round(Math.toDegrees(Math.acos(g[1])));
        //Log.d("inclinationTest: ", Integer.toString(inclination));
        if (inclination>=90){ //Never shows over 90 degrees.
            inclination = 90;
        }
        double smoothedincline = smoothedValue(inclination); // smoothed the accelerometer data http://phrogz.net/js/framerate-independent-low-pass-filter.html

        //Log.d("smoothedIncline", Double.toString(smoothedincline));
        NumberFormat formattedSmoothedIncline = new DecimalFormat("#0"); //formats the value to no sigfigs
        degreesTV.setText(Math.abs(Integer.parseInt(formattedSmoothedIncline.format(smoothedincline))) + "°"); //formats the value for the Textview

        //MOVING BALL WITHIN BALLBANK CASE:
        //ball on 20 deg left when at x=160 and y=115
        //ball on 20 deg right when at x=790 and y=115
        //ball on 0  deg center when at x=475 and y=171
        //Equation to set the location of thr ball along a parabollic path
        if (x <= 0){
            redball.setX((float) (475+smoothedincline*16)); // makes the ball go right when the x value is less thanm 0.
        } else {
            redball.setX((float) (475-smoothedincline*16.)); // makes the ball go left when the x value is less than 0.
        }
        redball.setY((float) (-0.00056437389770723*Math.pow(redball.getX(),2)+0.53615520282187*redball.getX()+43.66)); // Parabolic formula for movement of ball
        //Log.d("Ball Position: ", "redball x: " + redball.getX() + " redball y: " + redball.getY());
        //rotate the ball to keep the illusion of a glass tube
            if (redball.getX() <= 475){//if the ball is on the left side of the case rotate the ball clockwise to maintain look of a glass tube
                redball.setRotation((float) ((475 - redball.getX())*0.063));// rotate ball
                //Log.d("redballRotation", Float.toString(redball.getRotation()));
            } else if (redball.getX() > 475){//if the ball is on the right side of the case rotate the ball counter-clockwise to maintain look of a glass tube
                redball.setRotation((float) ((475 - redball.getX())*0.063));//rotate ball
                //Log.d("redballRotation", Float.toString(redball.getRotation()));
            } else {
                redball.setRotation(0);
            }
            //Log.d("x: ", Float.toString(x));

        if (redball.getX() >= 80 && redball.getX() <=870){
                redball.setVisibility(View.VISIBLE);
        } else {
                redball.setVisibility(View.INVISIBLE);
        }


            //dead code below from a previous smoothing test
        if (previousValues.size() < 30) {
            previousValues.add(x);
        } else {
            for (int i = 0; i < previousValues.size(); i++) {
                sum += previousValues.get(i);
            }
            avg = sum / previousValues.size();
            //Log.d("array", previousValues.toString());
            //Log.d("avg: ", avg.toString());
            //Log.d("rawX :", Float.toString(x));
            sum = 0f;

            if (Math.abs(x - avg) > seekValue) {
                //Log.d("GlitchDetected", "New: " + x + " Previous Average: " + avg);
            }
            previousValues.remove(0);
            previousValues.add(previousValues.size(), x);
        }



        sensorInclinometer.onDataReceived(event.timestamp, event.values);
        sensorInclinometer.updateOffsets();
        //Log.d("Pitch", Double.toString(sensorInclinometer.getPitch()));
        //Log.d("Roll", Double.toString(sensorInclinometer.getRoll()));

        /*        returnedFilteredValues = lowPass(x,y,z);*/


/*        previousValues[0] = returnedFilteredValues[0];
        previousValues[1] = returnedFilteredValues[1];
        previousValues[2] = returnedFilteredValues[2];*/

        xAccel = returnedFilteredValues[0];
        yAccel = returnedFilteredValues[1];


 /*       if (movingList.size()<10){
            movingList.add(x);
        } */

        movingValues[0] = x;
        //Log.d("unfiltered", Float.toString(x));
        //Log.d("movingValues", Arrays.toString(returnedFilteredValues));

/*        currentValues[0] = x;
        previousValues[0] = x;

        float[] test = lowPassTwo(currentValues,previousValues);*/

        //Log.d("accellList", accellList.toString());

        int orientation = getResources().getConfiguration().orientation;

        if (accellList.size() >= 30) {
            accellList.remove(0);
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                accellList.add(9, y);
            } else {
                accellList.add(9, x);
            }
            for (int i = 0; i < accellList.size(); i++) {
                sum += accellList.get(i);
            }

            /*for (int i =  0; i <accellList.size(); i++){
                Log.d("accelList", accellList.get(i) + " " + i);
            }*/

/*            double xAngle = Math.atan(avg / (Math.sqrt(Math.pow(y, 2) * Math.pow(z, 2))));
            //Log.d("angle?",Double.toString(xAngle));

            Float average = sum / accellList.size();
            //Log.d("average", Float.toString(average));
            TextView textView = findViewById(R.id.degreesTV);
            Double inclinationInDegrees = (xAngle) * 180 / Math.PI;*/
/*            if (inclinationInDegrees > 0) {
                carTurningLeftImageView.setVisibility(View.VISIBLE);
                carTurningRightImageView.setVisibility(View.INVISIBLE);
            } else if (inclinationInDegrees < 0) {
                carTurningRightImageView.setVisibility(View.VISIBLE);
                carTurningLeftImageView.setVisibility(View.INVISIBLE);
            } else {
                carTurningLeftImageView.setVisibility(View.INVISIBLE);
                carTurningRightImageView.setVisibility(View.INVISIBLE);
            }*/

/*            //String degreesSigFig = String.format("%.0f",Math.abs(inclinationInDegrees));
            String degreesSigFig = String.format("%.0f", Math.abs(inclinationInDegrees));
            textView.setText(degreesSigFig + "°");
            inclination = inclinationInDegrees;
            inclinationInDegrees = Math.abs(inclinationInDegrees);*/
            if (inclination <= 14) {
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this, R.color.greenThreshold));
            } else if (inclination > 14 && inclination <= 16) {
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this, R.color.yellowThreshhold));
            } else if (inclination > 16) {
                getWindow().getDecorView().setBackgroundColor(ContextCompat.getColor(RunningRoute.this, R.color.redThreshold));
            }

        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                accellList.add(y);
            } else {
                accellList.add(x);
            }
        }
    }

    public double smoothedX(double newValue){
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastUpdateX;
        smoothed += elapsedTime * (newValue - smoothed)/smoothing;
        lastUpdateX = now;
        return smoothed;
    }

    public double smoothedValue(double newValue){
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastUpdate;
        smoothed += elapsedTime * (newValue - smoothed)/smoothing;
        lastUpdate = now;
        return smoothed;
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

    @Override
    protected void onStart() {
        super.onStart();
        //cameraKitView.onStart();
    }

    /* Request updates at startup */
    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, 1, 1, this);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        //cameraKitView.onResume();

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
        LatLng latestLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (myPosition != null) {

            if (addDistance) {
                distance += haversine(myPosition.latitude, myPosition.longitude, location.getLatitude(), location.getLongitude()); // returns distance in km.
            } else {
                distance -= haversine(myPosition.latitude, myPosition.longitude, location.getLatitude(), location.getLongitude()); // returns distance in km.
            }

            updateDistanceTextView(distance); // converts km to mi and updates textview
        }

        myPosition = latestLatLng;
        //Log.d("myposition", myPosition.toString()); //prints lat and lon
        pastPositionsList.add(myPosition); //updates the list of locations for the polyline

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myPosition.latitude, myPosition.longitude), 14f));

        //updates speed textview
        double d = location.getSpeed() * 2.2369;
        speedList.add(d);
        DecimalFormat df = new DecimalFormat("00");
        speedTextView.setText(df.format(d) + " mph");

        updatePolyline(myPosition);

        String curveDirection;
        if (inclination > 0) {
            curveDirection = "Left Curve";
        } else if (inclination < 0) {
            curveDirection = "Right Curve";
        } else {
            curveDirection = "Level";
        }

        timeWhenStopped = chronometer.getBase() - SystemClock.elapsedRealtime();

        Datapoint datapoint1 = new Datapoint(1, location.getLatitude(), location.getLongitude(), distance * 0.621371, location.getSpeed(), timeWhenStopped, inclination, curveDirection);
        arrayList.add(datapoint1);

        //Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        //recordedData.add(curveDirection + "," +  "Speed" + "," + df + "," + Integer.toString(lat) + "," + Integer.toString(lng));
        //recordedData.add(curveDirection + "," + Integer.toString(lat) + "," + Integer.toString(lng) + "," + "1");
        recordedData.add(datapoint1.getCurveDirection() + "," + datapoint1.getInclination() + "," + datapoint1.getSpeed() + "," + datapoint1.getLatitude() + "," + datapoint1.getLongitude());



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

//        File folder = new File("/sdcard/Android/data/com.trafficapptechnologies.curveware" + "/Folder.png");
//        Log.d("directory", folder.toString());
//        if (!folder.exists()){
//            final File newFile = new File(Environment.getExternalStorageDirectory().getPath(),"newFile.png");
//            try {
//                newFile.createNewFile();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Log.d("directory", Environment.getExternalStorageDirectory().getPath());
//            newFile.mkdir();
//        }

//        String fnam = "safga";
//        String mdata = "sdlkjh";
//
//        try {
//            String namFile = Environment.getExternalStorageDirectory() + "/AMJ/" + fnam;
//            File datfile = new File(namFile);
//            datfile.mkdirs();
//            Toast.makeText(getApplicationContext(), "File is:  " + datfile, Toast.LENGTH_LONG).show(); //##4
//            FileOutputStream fOut = new FileOutputStream(datfile);
//            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
//            myOutWriter.append(mdata);
//            myOutWriter.close();
//            fOut.close();
//            Toast.makeText(getApplicationContext(), "Finished writing to SD", Toast.LENGTH_LONG).show(); //##5
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(), "Write failure", Toast.LENGTH_SHORT).show(); //##6
//        }

//        // Find the root of the external storage.
//        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal
//
//        File root = android.os.Environment.getExternalStorageDirectory();
//
//        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder
//
//        File dir = new File (root.getAbsolutePath() + "/download");
//        dir.mkdirs();
//        File file = new File(dir, "myData.txt");
//
//        try {
//            FileOutputStream f = new FileOutputStream(file);
//            PrintWriter pw = new PrintWriter(f);
//            pw.println("Hi , How are you");
//            pw.println("Hello");
//            pw.flush();
//            pw.close();
//            f.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//            Log.i("file", "******* File not found. Did you" +
//                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        String csv = "C:\\work\\output.csv";
//        CSVWriter writer = new CSVWriter(new FileWriter(csv));
//
//        List<string[]> data = new ArrayList<string[]>();
//        data.add(new String[] {"India", "New Delhi"});
//        data.add(new String[] {"United States", "Washington D.C"});
//        data.add(new String[] {"Germany", "Berlin"});
//
//        writer.writeAll(data);
//
//        writer.close();


//        try{
//            String filePath = this.getFilesDir().getPath().toString() + "/csvfile.csv";
//            //This PC\Galaxy Tab A\Tablet\Android\data\com.trafficapptechnologies.curveware\cache
//            Log.d("filepath", filePath);
//            File exportDir = new File(Environment.getExternalStorageDirectory(), "testfile.csv");
//            CSVWriter writer = new CSVWriter(new FileWriter(exportDir));
//            List<String[]> data = new ArrayList<String[]>();
//            data.add(new String[]{"Country", "Capital"});
//            data.add(new String[]{"India", "New Delhi"});
//            data.add(new String[]{"United States", "Washington D.C"});
//            data.add(new String[]{"Germany", "Berlin"});
//
//            writer.writeAll(data); // data is adding to csv
//
//            writer.close();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }

    }

    //generates the text files
    //From this tutorial:
    //https://stackoverflow.com/a/8152217/6884269
    //Good Writeup on internal and external storage
    //https://imnotyourson.com/which-storage-directory-should-i-use-for-storing-on-android-6/
    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Curveware Files");
            Log.d("pathWrite", root.toString());
            //Log.d("noteRoot", root.toString());
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            for (int i = 0; i < recordedData.size(); i++) {
                writer.append(recordedData.get(i)); //add \n\r if you want.
            }
            //writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDistanceTextView(double distance) {
        double distanceMi = distance * 0.621371;
        DecimalFormat df = new DecimalFormat("##0.00");
/*        BigDecimal bd = new BigDecimal(distanceMi);
        bd = bd.round(new MathContext(3));
        bd = bd.movePointRight(1);*/
        if (!pauseBoolean) {
            distanceTextView.setText(String.valueOf(df.format(distanceMi)) + " mi");
        }
    }

    private void updatePolyline(LatLng myPosition) {

        if (isMapReady) {
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
    public void shortToast(String string) {
        Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
    }

    //Distance Formula
    //https://rosettacode.org/wiki/Haversine_formula#Java
    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return earthRadius * c;
    }

    //TTS Method
    private void speakWords(String string) {
        ttobj.speak(string, TextToSpeech.QUEUE_FLUSH, null);
    }

    //beep sound
    public void playBeepSound() {
        final MediaPlayer buttonBeep = MediaPlayer.create(this, R.raw.buttonpushbeep);
        buttonBeep.start();
    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public float[] lowPassTwo(float[] output, float[] input) {
        output[0] = output[0] + .90f * (input[0] - output[0]);
        output[1] = output[1] + .90f * (input[1] - output[1]);
        output[2] = output[2] + .90f * (input[2] - output[2]);
        return output;
    }

 /*   float[] lowPass(float x, float y, float z) {

        float a = .01f;

        float[] filteredValues = new float[3];

        filteredValues[0] = x * a + previousValues[0] * (1.0f - a);
        filteredValues[1] = y * a + previousValues[1] * (1.0f - a);
        filteredValues[2] = z * a + previousValues[2] * (1.0f - a);

        return filteredValues;

    }*/

/*
    //Low Pass filter for smoothing sensor data
    private float[] lowPass(float x, float y, float z) {
        float[] filteredValues = new float[3];
        filteredValues[0] = x * a + filteredValues[0] * (1.0f – a);
        filteredValues[1] = y * a + filteredValues[1] * (1.0f – a);
        filteredValues[2] = z * a + filteredValues[2] * (1.0f – a);
        return filteredValues;
    }
*/

/*    private final int SMOOTH_FACTOR_MAA = 2;//increase for better results   but hits cpu bad
    //Moving average filter for smoothing sensor data
    public ArrayList<Float> processWithMovingAverageGravity(ArrayList<Float> list, ArrayList<Float> gList) {
        int listSize = list.size();//input list
        int iterations = listSize / SMOOTH_FACTOR_MAA;
        if (!AppUtility.isNullOrEmpty(gList)) {
            gList.clear();
        }
        for (int i = 0, node = 0; i < iterations; i++) {
            float num = 0;
            for (int k = node; k < node + SMOOTH_FACTOR_MAA; k++) {
                num = num + list.get(k);
            }
            node = node + SMOOTH_FACTOR_MAA;
            num = num / SMOOTH_FACTOR_MAA;
            gList.add(num);//out put list
        }
        return gList;
    }*/


    private class BallView extends View {

        public BallView(Context context) {
            super(context);
            Bitmap ballSrc = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
            final int dstWidth = 100;
            final int dstHeight = 100;
            ball = Bitmap.createScaledBitmap(ballSrc, dstWidth, dstHeight, true);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawBitmap(ball, xPos, yPos, null);
            invalidate();
        }
    }

    public abstract class AbstractSensor {
        private final String TAG = this.getClass().getSimpleName();
        /** the watchdog is currently unimplemented */

        /**
         * Registers a listener (visitor) that wants to listen to this sensor.  The listener
         * provides a instantiation of the SensorApi; the sensor will use this SensorApi instantiation
         * to callback the listener when a sensor event occurs.
         *
         * If this is the first listener, this method will enable the sensor.  (No point in running
         * a sensor to which no one is listening.)
         *
         * @param sensorManager An instance of the Android SensorManager
         * @param callback      The listener's callback class
         */

        /**
         * Unregisters (Removes) a listener.
         *
         * If removing this listener results in no one is listening to the sensor, this method will
         * disable the sensor.  (Let's save some power, eh?)
         *
         * @param sensorManager An instance of the Android SensorManager
         * @param callback      The listener's callback class
         */


        /**
         * Method that must be overridden by the concrete sensor class to enable the sensor.  For
         * built-in Android sensors (e.g. accelerometer, gyroscope, etc.), this method will likely
         * invoke the registerListener() method in the SensorManager.  Custom sensors (e.g.
         * inclinometer) may do something different entirely.
         *
         * @param sensorManager An instance of the Android SensorManager
         */
        protected abstract void enableSensor(SensorManager sensorManager);

        /**
         * Method that must be overridden by the concrete sensor class to disable the sensor.  For
         * built-in Android sensors (e.g. accelerometer, gyroscope, etc.), this method will likely
         * invoke the unregisterListener() method in the SensorManager.  Custom sensors (e.g.
         * inclinometer) may do something different entirely.
         *
         * @param sensorManager An instance of the Android SensorManager
         */
        protected abstract void disableSensor(SensorManager sensorManager);
    }

    public class SensorInclinometer {
        private final String TAG = this.getClass().getSimpleName();

        float[] pitchAndRoll = new float[2];

        public static final int PITCH_INDEX = 0;
        public static final int ROLL_INDEX = 1;

        private static final double RAD_TO_DEG = 180.0f / Math.PI;
        private static final double ROLL_X_ZERO_THRESH = 0.25f;
        private static final double ROLL_Y_ZERO_THRESH = 0.25f;

        /* notify listeners no faster than at a 3 Hz rate (approximately 333 ms) */
        private static final long NOTIFY_LISTENERS_TIME_MS = 333;
        private long lastTimeListenersNotified = System.currentTimeMillis();

        private SensorInclinometer instance = null;
        private FilterMovingAverage filterMovingAverage =
                new FilterMovingAverage(FilterMovingAverage.DEFAULT_SAMPLE_EXPIRATION_NS);

        private double[] pitchAndRollOffsets = {0, 0};

        /**
         * Constructor - note this will force the class to be a singleton
         */
        protected SensorInclinometer() {
        }

        /**
         * Public constructor.  Returns the instance of this singleton class.  This method will
         * create the instance if it doesn't exist.
         *
         * @return the instance of this class
         */
        public SensorInclinometer getInstance() {
            if (null == instance) {
                instance = new SensorInclinometer();
            }

            return instance;
        }


        /**
         * This class's listener for new sensor data from SensorOrientedAccelerometer.  Required
         * via the SensorApi implementation.
         *
         * @param timestamp   time at which this measurement occurred
         * @param accelValues array of accelerometer measurements (x == 0, y == 1, z == 2)
         */

        public void onDataReceived(long timestamp, float[] accelValues) {
            ////HromatkaLog.getInstance().enter(TAG);

            filterMovingAverage.add(timestamp, accelValues);
            filterMovingAverage.removeExpired();

            float[] averagedAccelValues = filterMovingAverage.getMovingAverage();


            pitchAndRoll[PITCH_INDEX] =
                    (float) computePitch((double) averagedAccelValues[1], (double) averagedAccelValues[2]) +
                            (float) pitchAndRollOffsets[PITCH_INDEX];
            pitchAndRoll[ROLL_INDEX] =
                    (float) computeRoll((double) averagedAccelValues[0], (double) averagedAccelValues[1]) +
                            (float) pitchAndRollOffsets[ROLL_INDEX];

            /**
             * SensorInclinometer generates the values[] array for onDataReceived() as follows:
             * 0 == pitch (degrees)
             * 1 == roll (degrees)
             *
             * Note to save power, the inclinometer data is only sent to the activity at approximately
             * three hertz.
             */

            long currentTime = System.currentTimeMillis();
            if ((currentTime - lastTimeListenersNotified) > NOTIFY_LISTENERS_TIME_MS) {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Notifying listeners of new inclinometer data");
                lastTimeListenersNotified = currentTime;
                //notifyListenersDataReceived(timestamp, pitchAndRoll);
            } else {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Rate limiting new inclinometer data.  Do not notify listeners.");
            }

            ////HromatkaLog.getInstance().exit(TAG);
        }

        public double getRoll() {
            return pitchAndRoll[ROLL_INDEX];
        }

        public double getPitch() {
            return pitchAndRoll[PITCH_INDEX];
        }


        /**
         * Update the pitch and roll offsets.  This allows the phone to be mounted at any arbitrary
         * angle.
         */
        public void updateOffsets() {
            float[] averagedAccelValues = filterMovingAverage.getMovingAverage();

            pitchAndRollOffsets[PITCH_INDEX] =
                    -computePitch((double) averagedAccelValues[1], (double) averagedAccelValues[2]);
            pitchAndRollOffsets[ROLL_INDEX] =
                    -computeRoll((double) averagedAccelValues[0], (double) averagedAccelValues[1]);
        }

        /**
         * Method to compute the pitch of the device
         *
         * @param y accelerometer value in the y axis (m/s^2)
         * @param z accelerometer value in the z axis (m/s^2)
         * @return the pitch of the phone in degrees
         */
        public double computePitch(double y, double z) {
            ////HromatkaLog.getInstance().enter(TAG);
            double pitch = 90.0f - (Math.atan2(y, z) * RAD_TO_DEG);

            if (pitch < -180.0f) {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Pitch underflow: " + pitch);
                pitch += 360.0f;
            } else if (pitch > 180.0f) {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Pitch overflow: " + pitch);
                pitch -= 360.0f;
            }

            ////HromatkaLog.getInstance().logVerbose(TAG, String.format("y = %4.1f, z = %4.1f, pitch = %4.1f", y, z, pitch));

            ////HromatkaLog.getInstance().exit(TAG);
            return pitch;
        }

        /**
         * Method to compute the roll of the device
         *
         * @param x accelerometer value in the x axis (m/s^2)
         * @param y accelerometer value in the y axis (m/s^2)
         * @return the roll of the phone in degrees
         */
        public double computeRoll(double x, double y) {
            ////HromatkaLog.getInstance().enter(TAG);
            double roll = (Math.atan2(y, x) * RAD_TO_DEG) - 90.0f;

            if (roll < -180.0f) {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Roll underflow: " + roll);
                roll += 360.0f;
            } else if (roll > 180.0f) {
                ////HromatkaLog.getInstance().logVerbose(TAG, "Roll overflow: " + roll);
                roll -= 360.0f;
            }

            if (x < ROLL_X_ZERO_THRESH && y < ROLL_Y_ZERO_THRESH) {
                /* zero out roll when both x and y are near-zero. */
                ////HromatkaLog.getInstance().logVerbose(TAG, "Roll autozero.  x = " + x + ", y = " + y);
                roll = 0.0f;
            }

            ////HromatkaLog.getInstance().exit(TAG);
            return roll;
        }
    }

    public class FilterMovingAverage {
        private final String TAG = this.getClass().getSimpleName();
        public static final double SEC_TO_NANOSEC = 1e9;
        /* by default, expire samples after 1/2 of a second */
        public static final double DEFAULT_SAMPLE_EXPIRATION_NS = 0.5 * SEC_TO_NANOSEC;

        /**
         * class that combines the timestamp and sensor data into one (convenient!) location
         */
        private class TimestampAndData {
            private long timestamp;
            private float[] data;

            public TimestampAndData(long timestamp, float[] data) {
                this.timestamp = timestamp;
                this.data = data;
            }

            public long getTimestamp() {
                return this.timestamp;
            }

            public float[] getData() {
                return this.data;
            }
        }

        private List<TimestampAndData> timestampAndDataList = new ArrayList<>();
        private double samplesExpireAfterNanoseconds;

        public FilterMovingAverage(double samplesExpireAfterNanoseconds) {
            //////HromatkaLog.getInstance().enter(TAG);
            this.samplesExpireAfterNanoseconds = samplesExpireAfterNanoseconds;
            //////HromatkaLog.getInstance().exit(TAG);
        }

        /**
         * Returns the current moving average of the data stored by this filter
         *
         * @return moving average
         */
        public synchronized float[] getMovingAverage() {
            //////HromatkaLog.getInstance().enter(TAG);
            /* get one sample to determine the length of the float array */
            TimestampAndData oneSample = timestampAndDataList.get(0);
            float[] averages = new float[oneSample.getData().length];

            /*
             * 1) zero out the averages
             */
            for (int index = 0; index < averages.length; index++) {
                averages[index] = 0.0f;
            }

            /*
             * 2) sum up all of the samples
             */
            for (Iterator<TimestampAndData> iterator = timestampAndDataList.iterator(); iterator.hasNext(); ) {
                TimestampAndData thisSample = iterator.next();

                for (int index = 0; index < averages.length; index++) {
                    averages[index] += thisSample.getData()[index];
                }
            }

            /*
             * 3) divide by the number of samples to get the average
             */
            for (int index = 0; index < averages.length; index++) {
                averages[index] = averages[index] / timestampAndDataList.size();
            }

            //////HromatkaLog.getInstance().exit(TAG);
            return averages;
        }

        /**
         * insert a sample and its timestamp into the moving average filter
         *
         * @param timestamp timestamp of the data
         * @param data      float[] containing the sample data
         */
        public synchronized void add(long timestamp, float[] data) {
            //////HromatkaLog.getInstance().enter(TAG);
            TimestampAndData newSample = new TimestampAndData(timestamp, data);
            timestampAndDataList.add(newSample);

            //////HromatkaLog.getInstance().logVerbose(TAG, "timestampAndDataList size = " + timestampAndDataList.size());
            //////HromatkaLog.getInstance().exit(TAG);
        }

        /**
         * clear the entire moving average filter
         */
        public synchronized void clear() {
            //////HromatkaLog.getInstance().enter(TAG);
            timestampAndDataList.clear();
            //////HromatkaLog.getInstance().exit(TAG);
        }

        /**
         * remove expired samples from the moving average filter.  The expiration duration was set
         * during construction of this class.
         */
        public synchronized void removeExpired() {
            //////HromatkaLog.getInstance().enter(TAG);

            /*
             * get the most recent sample's timestamp.  we will use this as the "current" time.  Not
             * perfect, but it's the only safe comparison for a sensor.  There's no guarantee that
             * the sensor's timestamp is comparable to System.nanoTime().
             */
            TimestampAndData mostCurrentSample = timestampAndDataList.get(timestampAndDataList.size() - 1);
            long currentTime = mostCurrentSample.getTimestamp();
            //////HromatkaLog.getInstance().logVerbose(TAG, "Current time: " + currentTime);

            for (Iterator<TimestampAndData> iterator = timestampAndDataList.iterator(); iterator.hasNext(); ) {
                TimestampAndData thisSample = iterator.next();

                if (thisSample.getTimestamp() < (currentTime - (long) samplesExpireAfterNanoseconds)) {
                    /* this sample has expired.  remove it */
                    //      ////HromatkaLog.getInstance().logVerbose(TAG, "Removing: " + thisSample.getTimestamp());
                    iterator.remove();
                }
            }

            //////HromatkaLog.getInstance().logVerbose(TAG, "timestampAndDataList size = " + timestampAndDataList.size());
            //////HromatkaLog.getInstance().exit(TAG);
        }
    }


}





