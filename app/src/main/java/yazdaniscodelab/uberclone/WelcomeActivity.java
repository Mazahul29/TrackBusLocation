package yazdaniscodelab.uberclone;

import android.*;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.ConnectionResult;




import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import yazdaniscodelab.uberclone.Common.common;
import yazdaniscodelab.uberclone.Remote.IGoogleAPI;

public class WelcomeActivity extends FragmentActivity implements OnMapReadyCallback,

        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener

{

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mlocationRequest;
    private GoogleApiClient mgoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private FirebaseAuth mAuth;


    DatabaseReference drive;
    GeoFire geoFire;
    Marker mCurrent;
    MaterialAnimatedSwitch mlocation_switcher;

    SupportMapFragment mapFragment;


    // CAR aNIMATION.


    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private Double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPosition,currentPosition;
    private int index,next;
    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions,blackPolyLineOptions;
    private Polyline blackPoleyline,greypolyline;


    private IGoogleAPI mService;


    Runnable drawPathRunable =new Runnable() {
        @Override
        public void run() {

            if (index<polyLineList.size()-1){
                index++;
                next=index+1;
            }

            if (index<polyLineList.size()-1){

                startPosition=polyLineList.get(index);
                endPosition=polyLineList.get(next);

            }

            ValueAnimator valueAnimator=ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {

                    v=valueAnimator.getAnimatedFraction();
                    lng=v*endPosition.longitude+(1-v)*startPosition.longitude;
                    lat=v*endPosition.latitude+(1-v)*startPosition.latitude;

                    LatLng newpos = new LatLng(lng,lat);
                    carMarker.setPosition(newpos);
                    carMarker.setAnchor(0.5f,0.5f);
                    carMarker.setRotation(getBearing(startPosition,newpos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()

                            .target(newpos)
                            .zoom(15.5f)
                            .build()

                    ));

                }
            });

            valueAnimator.start();
            handler.postDelayed(this,3000);

        }
    };

    private float getBearing(LatLng startPosition, LatLng endPosition) {

        double lat=Math.abs(startPosition.latitude-endPosition.latitude);
        double lng=Math.abs(startPosition.longitude-endPosition.longitude);

        if (startPosition.latitude<endPosition.longitude && startPosition.longitude<endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));

        else if (startPosition.latitude >= endPosition.longitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+90);

        else if (startPosition.latitude>=endPosition.longitude && startPosition.longitude>=endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);



        else if (startPosition.latitude<endPosition.longitude && startPosition.longitude>=endPosition.longitude)
            return (float) ((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Init View...

        mlocation_switcher = findViewById(R.id.location_switch);

        mAuth = FirebaseAuth.getInstance();

        mlocation_switcher.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {

                if (isOnline) {
                    startLocationUpdates();
                    displayLocation();

                    Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_LONG).show();
                } else {

                    stopLocationsUpdate();
                    mCurrent.remove();
                    mMap.clear();

                    handler.removeCallbacks(drawPathRunable);

                    Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_LONG).show();

                }
            }
        });

//        For Animation part..

        polyLineList = new ArrayList<>();
        btnGo=findViewById(R.id.btnGo);
        edtPlace=findViewById(R.id.edtPlace);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                destination=edtPlace.getText().toString();
                destination = destination.replace("","+");
                Log.d("AnimationCheck",destination);
                
                getDirection(); //Banano Method nt builting;
                
            }
        });





        drive = FirebaseDatabase.getInstance().getReference("ServiceProviders");
        geoFire = new GeoFire(drive);

        setUplocations();

        mService= common.getGoogleAPI();


    }


//For location animation...

    private void getDirection() {

        currentPosition=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        String requestApi = null;

        try {

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("Yazdani",requestApi);

            mService.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {

                            try {

                                JSONObject jsonObject=new JSONObject(response.body().toString());

                                JSONArray jsonArray=jsonObject.getJSONArray("routes");

                                for (int i=0;i<jsonArray.length();i++){

                                    JSONObject route=jsonArray.getJSONObject(i);
                                    JSONObject poly=route.getJSONObject("overview_polylline");
                                    String polyline=poly.getString("points");
                                    polyLineList=decodePoly(polyline);

                                }

                                //Adjusting Bound..
                                LatLngBounds.Builder builder=new LatLngBounds.Builder();

                                for (LatLng latLng:polyLineList)
                                    builder.include(latLng);
                                LatLngBounds bounds = builder.build();
                                CameraUpdate mCameraUpdate=CameraUpdateFactory.newLatLngBounds(bounds,2);
                                mMap.animateCamera(mCameraUpdate);

                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(5);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.endCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polyLineList);

                                greypolyline=mMap.addPolyline(polylineOptions);


                                blackPolyLineOptions = new PolylineOptions();
                                blackPolyLineOptions.color(Color.BLACK);
                                blackPolyLineOptions.width(5);
                                blackPolyLineOptions.startCap(new SquareCap());
                                blackPolyLineOptions.endCap(new SquareCap());
                                blackPolyLineOptions.jointType(JointType.ROUND);

                                blackPoleyline=mMap.addPolyline(blackPolyLineOptions);

                                mMap.addMarker(new MarkerOptions()
                                    .position(polyLineList.get(polyLineList.size()-1))
                                        .title("Pickup Location"));


                                //Animator..

                                ValueAnimator polyLineAnimator=ValueAnimator.ofInt(0,100);
                                polyLineAnimator.setDuration(2000);
                                polyLineAnimator.setInterpolator(new LinearInterpolator());

                                polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(ValueAnimator valueAnimator) {

                                        List<LatLng> points=greypolyline.getPoints();
                                        int parcentValue=(int)valueAnimator.getAnimatedValue();
                                        int size=points.size();
                                        int newPoints=(int) (size*(parcentValue/100.0f));

                                        List<LatLng>p=points.subList(0,newPoints);
                                        blackPoleyline.setPoints(p);
                                    }
                                });

                                polyLineAnimator.start();

                                carMarker=mMap.addMarker(new MarkerOptions().position(currentPosition)

                                        .flat(true)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                                handler = new Handler();

                                index=-1;
                                next=1;
                                handler.postDelayed(drawPathRunable,3000);



                            }catch (JSONException e){
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {

                            Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });


        }catch (Exception e){

            e.printStackTrace();

        }


    }

    //decode poly boy copy from github

        private List decodePoly(String encoded) {

            List poly = new ArrayList();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }





    //Request permission


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices()) {
                        builtGoogleApiclient();
                        createLocationRequest();
                        if (mlocation_switcher.isChecked()) ;
                        displayLocation();
                    }
                }
        }

    }

    private void setUplocations() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION

            }, MY_PERMISSION_REQUEST_CODE);

        } else {
            if (checkPlayServices()) {
                builtGoogleApiclient();
                createLocationRequest();
                if (mlocation_switcher.isChecked()) ;
                displayLocation();

            }
        }


    }

    private void createLocationRequest() {

        mlocationRequest = new LocationRequest();
        mlocationRequest.setInterval(UPDATE_INTERVAL);
        mlocationRequest.setFastestInterval(FATEST_INTERVAL);
        mlocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mlocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }

    private void builtGoogleApiclient() {
        mgoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mgoogleApiClient.connect();
    }

    private boolean checkPlayServices() {

        int resultcode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (resultcode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode))
                GooglePlayServicesUtil.getErrorDialog(resultcode, this, PLAY_SERVICE_RES_REQUEST).show();

            else {
                Toast.makeText(getApplicationContext(), "This Driver is not Supported", Toast.LENGTH_SHORT).show();
                finish();
            }

            return false;
        }

        return true;
    }


    //Stops Locations Methods..

    private void stopLocationsUpdate() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            return;
        }

        LocationServices.FusedLocationApi.removeLocationUpdates(mgoogleApiClient, this);


    }

    //Display LOcation Methods..

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            return;
        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mgoogleApiClient);

        if (mLastLocation != null) {

            if (mlocation_switcher.isChecked()) {

                final double lattitude = mLastLocation.getLatitude();
                final double longituted = mLastLocation.getLongitude();


                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(lattitude, longituted), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {

                        //Add Marker..

                        if (mCurrent != null)
                            mCurrent.remove();

                        mCurrent = mMap.addMarker(new MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                                .position(new LatLng(lattitude, longituted))
                                .title("Your Location"));

                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lattitude, longituted), 15.0f));

//                        //Drive animation rotated marker..
//
//                        rotateMarker(mCurrent, -360, mMap);

                    }
                });
            }
        } else {
            Log.d("ERROR", "Cannot get your locations");
        }


    }

    private void rotateMarker(final Marker mCurrent, final float i, GoogleMap mMap) {

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final float startlocations = mCurrent.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elaped = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elaped / duration);
                float rot = t * i + (1 - t) * startlocations;
                mCurrent.setRotation(-rot > 160 ? rot / 2 : rot);
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }

            }
        });


    }


    //Start location methods...

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mgoogleApiClient, mlocationRequest, this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();

    }

//    @Override
//    public void onStatusChanged(String s, int i, Bundle bundle) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String s) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String s) {
//
//    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mgoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    protected void onPause() {
        super.onPause();
        mAuth.signOut();
        finish();

    }

}
