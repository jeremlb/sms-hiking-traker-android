package com.example.jeremi.smshikingtraker;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 2000; // 2 secondes
    private static final String PHONE_NUMBER = "YOUR_PHONE_NUMBER"; //

    /**
     * Called when the activity is first created.
     */
    EditText eTextMsg, eTextMblNumber;
    TextView eLocationText;
    Spinner weatherSpinner, breakSpinner;
    Button btnSendSMS;

    // flag for GPS status
    boolean isGPSEnabled = false;

    int locationPermissionGranted = PackageManager.PERMISSION_DENIED;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    float accuracy; // accuracy

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eTextMblNumber = (EditText) findViewById(R.id.etextMblNumber);
        eTextMblNumber.setText(PHONE_NUMBER); // set the field width a default value
        weatherSpinner = (Spinner) findViewById(R.id.weather);
        breakSpinner = (Spinner) findViewById(R.id.break_type);

        eTextMsg = (EditText) findViewById(R.id.etextMsg);
        eLocationText = (TextView) findViewById(R.id.textViewLocation);
        btnSendSMS = (Button) findViewById(R.id.btnSendSMS);

        setWeatherSpinner();
        setBreakSpinner();

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, 1);


        btnSendSMS.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                sendSMS();
            }
        });

        getLocation();
    }

    public Location getLocation() {
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(
                    Context.LOCATION_SERVICE);


            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);


            locationPermissionGranted = ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION);

            if(locationPermissionGranted == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                if (isGPSEnabled) {
                    if (this.location == null) {
                        this.location = locationManager.getLastKnownLocation(
                                LocationManager.GPS_PROVIDER);

                        if(this.location != null) {
                            updateLocation();
                        }
                    }
                } else {
                    Intent gpsOptionsIntent = new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    public void updateLocation() {
        if(this.location != null) {
            this.latitude = this.location.getLatitude();
            this.longitude = this.location.getLongitude();
            this.accuracy = this.location.getAccuracy();
            updateLocationText();
        }
    }

    public void sendSMS() {
        if(this.location != null) {
            String breakValue = breakSpinner.getSelectedItem().toString();
            String weatherValue = weatherSpinner.getSelectedItem().toString();

            if(!breakValue.isEmpty() && !weatherValue.isEmpty()) {
                SmsManager sm = SmsManager.getDefault();

                String number = eTextMblNumber.getText().toString();
                String msg = eTextMsg.getText().toString();

                String text = this.latitude + "," + this.longitude +
                        " - " + weatherValue +
                        " - " + breakValue +
                        " - " + msg;

                sm.sendTextMessage(number, null, text, null, null);
                CharSequence toastmsg = "Send";
                Toast.makeText(getApplicationContext(), toastmsg, Toast.LENGTH_SHORT).show();
            } else {
                CharSequence text = "No selected weather or selected break";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }

        } else {
            CharSequence text = "No location";
            Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
        }
    }

    public void updateLocationText() {
        CharSequence text = "lat : " + this.latitude + ", lng : " + this.longitude +
                ", accuracy : " + this.accuracy;

        eLocationText.setText(text);
    }

    public void setWeatherSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.weather, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        weatherSpinner.setAdapter(adapter);
    }

    public void setBreakSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.break_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        breakSpinner.setAdapter(adapter);
    }

    @Override
    public void onLocationChanged(Location currentLocation) {
        this.location = currentLocation;
        updateLocation();
    }

    @Override
    public void onProviderDisabled(String provider) {
        CharSequence text = "Provider disable";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        CharSequence text = "Provider enable";
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
}