package com.example.restdeliverycollaboratorapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import im.delight.android.ddp.Meteor;
import im.delight.android.ddp.MeteorCallback;
import im.delight.android.ddp.MeteorSingleton;
import im.delight.android.ddp.ResultListener;
import im.delight.android.ddp.db.Database;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,MeteorCallback,LocationListener,
        GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener {

    Meteor mMeteor;


    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String HELPERS="helpers";
    String orderId = "";
    boolean traveling;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    GoogleMap googleMap;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if(isGooglePlayServicesAvailable(this)) {
            MapFragment map = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            map.getMapAsync(this);
        }

        prefs = getSharedPreferences(HELPERS, Context.MODE_PRIVATE);
        editor = prefs.edit();

        mMeteor = MeteorSingleton.getInstance();
        mMeteor.addCallback(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();

    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mGoogleApiClient.isConnected()) {
            createLocationRequest();
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(MapActivity.this, location.toString(), Toast.LENGTH_SHORT).show();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        if(traveling) {

            mMeteor.call("updateLocationOrder", new Object[]{
                    orderId,
                    location.getLatitude(),
                    location.getLongitude()
            }, new ResultListener() {
                @Override
                public void onSuccess(String result) {
//                    Log.i("RESULT_CALL",result);
                }

                @Override
                public void onError(String error, String reason, String details) {
//                    Log.i("ERROR_CALL",error+" " +reason+" "+ details);
                }
            });

        }
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onDataChanged(String collectionName, String documentID, String updatedValuesJson, String removedValuesJson) {
        if(collectionName.equals("orders")){
            try {
                JSONObject jsonObject = new JSONObject(updatedValuesJson);
                String mandatedId = jsonObject.getString("mandatedId");
                if(mandatedId.equals(prefs.getString("userId",""))){
                    traveling = true;
                    orderId = documentID;
                }

            }catch (JSONException e){

            }
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onDataRemoved(String collectionName, String documentID) {
        Log.i("NAME_COLLECTION",collectionName);
        Log.i("DOCUMENT_ID",documentID);
        if(documentID==orderId) traveling=false;
    }

    @Override
    public void onDisconnect() {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onDataAdded(String collectionName, String documentID, String newValuesJson) {

    }

    @Override
    public void onConnect(boolean signedInAutomatically) {

    }

    @Override
    public void onMapReady(GoogleMap _googleMap) {
        googleMap = _googleMap;

        LatLng sydney = new LatLng(-34, 151);
        googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
