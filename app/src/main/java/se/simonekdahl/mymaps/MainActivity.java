package se.simonekdahl.mymaps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class MainActivity extends ParentActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int GPS_ENABLE_REQUEST = 9002;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;
    private boolean dialogShown = false;
    private boolean freshStart = true;

    private static final String TAG = "MainActivity";
    private LocationManager locationManager;
    private DBHandler handler;
    List<MapObject> mapObjects;
    GoogleMap mMap;

    //Googles api client
    private GoogleApiClient client;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //control if user has gps enabled on his/her device
        checkIfGpsEnabled();

        try {

            //control mapservices
            if (servicesOK()) {

                //initiate UI
                setContentView(R.layout.nav_drawer_main);
                Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                setSupportActionBar(toolbar);

                FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

                assert fab != null;
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Intent i = new Intent(MainActivity.this, GroundOverlayActivity.class);
                        Intent i = new Intent(MainActivity.this, ChoseFileActivity.class);

                        i.putExtra("CURRENT_LATITUDE", mMap.getCameraPosition().target.latitude);
                        i.putExtra("CURRENT_LONGITUDE", mMap.getCameraPosition().target.longitude);
                        i.putExtra("CURRENT_ZOOM", mMap.getCameraPosition().zoom);

                        startActivityForResult(i, 1);

                    }
                });

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                assert drawer != null;
                drawer.setDrawerListener(toggle);
                toggle.syncState();

                NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
                assert navigationView != null;
                navigationView.setNavigationItemSelectedListener(this);


                if (mMap == null) {
                    SupportMapFragment mapFragment =
                            (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                }

            } else {
                setContentView(R.layout.activity_nomapsfound);
            }
            client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_for_hint));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                //Collapse the searchfield
                searchItem.collapseActionView();
                // Send the entered string to geolocate
                try {
                    geoLocateAppBar(searchView, s);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        return true;
    }

    //Options to select map-type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling , change the map from various layouts.
        switch (id) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

//Function to control if gps is enabled , and if not, alert user to turn it on
    public void checkIfGpsEnabled() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showGPSDisabledAlertToUser();
        }
    }

    //Function to alert user if GPS is currently turned off on the device
    private void showGPSDisabledAlertToUser(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.Alert_message_GPS_is_disabled)
                .setPositiveButton(R.string.Go_to_gps_settings_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Set the dialogshown to true. user has seen this alert
                                dialogShown = true;
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent, GPS_ENABLE_REQUEST);
                            }
                        });

        alertDialogBuilder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    //Funtion to control if googleplayservices is available
    public boolean servicesOK() {

        int isAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvailable)) {
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvailable, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, R.string.unable_to_connect_to_mappingservice_toast, Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    //Function to go (move camera) to a certain location on the map.
    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }


    //Enable Gps-tracking on the map. checks the manifest for permissions. makes persmission request to get location
    public void enableGPS(){

        //Needs to have this check
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
        else{
            mMap.setMyLocationEnabled(true);

            }
    }


    //Function to find the destination on googlemaps from user input.
    public void geoLocateAppBar(View v, String s) throws IOException {

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(s, 1);

        if (list.size() > 0) {
            Address address = list.get(0);
            String locality = address.getLocality();

            if (locality != null) {
                Toast.makeText(this, getString(R.string.found_toast_text) + locality, Toast.LENGTH_SHORT).show();
            }

            double lat = address.getLatitude();
            double lng = address.getLongitude();

            //Go to the location, zoomlevel is 12
            gotoLocation(lat, lng, 12);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume: load groundoverlaydata ");
        //loadGroundOverlayData();
    }

    @Override
    public void onStart() {
        super.onStart();

        if(client!=null) {
            client.connect();
            //loadGroundOverlayData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if(client!=null && client.isConnected()) {
            client.disconnect();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        outState.putInt("CURRENT_MAP_TYPE", mMap.getMapType());
        outState.putDouble("CURRENT_LONGITUDE", mMap.getCameraPosition().target.latitude);
        outState.putDouble("CURRENT_LATITUDE", mMap.getCameraPosition().target.longitude);
        outState.putFloat("CURRENT_ZOOM", mMap.getCameraPosition().zoom);

    }


    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onRestoreInstanceState(savedInstanceState, persistentState);

        int mapType = savedInstanceState.getInt("CURRENT_MAP_TYPE");
        double longitude = savedInstanceState.getDouble("CURRENT_LONGITUDE");
        double latitude = savedInstanceState.getDouble("CURRENT_LATITUDE");
        float zoom = savedInstanceState.getFloat("CURRENT_ZOOM");

        mMap.setMapType(mapType);
        gotoLocation(latitude,longitude,zoom);
        loadGroundOverlayData();

    }

    //function to register clicks in navigationdrawer
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_map) {
            Intent i = new Intent(MainActivity.this, ChoseFileActivity.class);
            startActivityForResult(i, 2);

        } else if (id == R.id.nav_my_maps) {
            Intent i = new Intent(MainActivity.this, MapList.class);
            startActivityForResult(i,3);

        } else if (id == R.id.nav_home) {

            loadGroundOverlayData();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //checks the permission requests.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                    enableGPS();

                } else {
                    Toast.makeText(this, R.string.app_need_location_to_work_toast,
                            Toast.LENGTH_LONG).show();
                    // User did not grant permission
                }
                break;
        }
    }

    //PARENT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GPS_ENABLE_REQUEST && resultCode == 0){
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if(provider != null){
                Log.v(TAG, " Location providers: " + provider);
                Toast.makeText(this, R.string.gps_has_been_turned_on_toast,
                        Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, R.string.GPS_is_turned_off_toast,
                        Toast.LENGTH_LONG).show();
            }
        }

        loadGroundOverlayData();

    }

    private void loadGroundOverlayData(){

        if(handler == null){
            handler = new DBHandler(this);
        }

        mapObjects = handler.readAllMaps();

        //clear the map from old overlays.
        mMap.clear();

        for(MapObject m : mapObjects){

            //If the mapobject has two tiepoints, place a the groundoverlay on the map.
            if(m.get_tiePointOne() >0 && m.get_tiePointTwo()>0){
                //Add all groundoverlays to the map
                GroundOverlayOptions gO = new GroundOverlayOptions();
                LatLng latLng = new LatLng(m.get_tiePointOne(),m.get_tiePointTwo());

                //Just for logging
                Log.d(TAG, "Tiepoint one is: " + m.get_tiePointOne());
                Log.d(TAG, "Tiepoint two is: " + m.get_tiePointTwo());
                Log.d(TAG, "LATNG:  = " + latLng);
                Log.d(TAG, "Width : = " + m.get_Size());
                Log.d(TAG, "Bearing: =  " + m.get_Rotation());
                Log.d(TAG, "FILENAME: = "+ m.get_bitmapName());
                Log.d(TAG, "FilePath: = " + m.get_filePath());

                gO.position(latLng,(float) m.get_Size());
                gO.bearing((float)m.get_Rotation());
                Bitmap bM = loadImageFromStorage(m.get_filePath(), m.get_id());
                gO.image(BitmapDescriptorFactory.fromBitmap(bM));

                mMap.addGroundOverlay(gO);

            }
        }
        handler.close();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        enableGPS();

        Toast.makeText(this, R.string.ready_to_map, Toast.LENGTH_SHORT).show();

        if (getIntent().getDoubleExtra("MAP_LATITUDE", 0) > 0 && getIntent().getDoubleExtra("MAP_LONGITUDE", 0) > 0) {

            gotoLocation(getIntent().getDoubleExtra("MAP_LATITUDE", 0), getIntent().getDoubleExtra("MAP_LONGITUDE", 0), 12);
            //Reset the intent
            getIntent().putExtra("MAP_LATITUDE", 0);
            getIntent().putExtra("MAP_LONGITUDE", 0);


            //If the app is opened for the first time the instacestate is null.
            //Move the camera to users position, fairly zoomed out.
        } else if (savedInstanceState == null) {

            // Get LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Create a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Get Current Location
            //Needs to have this check
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {


                ActivityCompat.requestPermissions(this, new String[]
                                {Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_ACCESS_FINE_LOCATION);
            }

            if (locationManager.getLastKnownLocation(provider) != null) {
                Location myLocation = locationManager.getLastKnownLocation(provider);

                // Get latitude of the current location
                double latitude = myLocation.getLatitude();

                // Get longitude of the current location
                double longitude = myLocation.getLongitude();

                gotoLocation(latitude, longitude, 7);
            }
        }

        handler = new DBHandler(this);
        //Load all groundoverlays to map.
        loadGroundOverlayData();


    }
}
