package se.simonekdahl.mymaps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.PersistableBundle;
import android.provider.Settings;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;

import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.lifecycle.ViewModelProviders;
import se.simonekdahl.mymaps.bottomsheet.RoundedBottomSheetDialogFragment;
import se.simonekdahl.mymaps.dao.MapObject;
import se.simonekdahl.mymaps.dao.MarkerObject;
import se.simonekdahl.mymaps.utils.PermissionUtils;

import static se.simonekdahl.mymaps.utils.MapImageUtils.loadImageFromStorage;

public class MainActivity extends ParentActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLoadedCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int GPS_ENABLE_REQUEST = 9002;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;
    private boolean dialogShown = false;
    private boolean freshStart = true;

    private static final String TAG = "MainActivity";
    private LocationManager locationManager;
    List<MapObject> mapObjects;
    GoogleMap mMap;
    LinearLayout llBottomSheet;
    //Googles api client
    private GoogleApiClient client;
    private Bundle savedInstanceState;

    private Marker latestMarker;

    private HashMap<Marker, MapObject> mapMarkerMap;

    LinearLayout loadingWrapper;

    private boolean mPermissionDenied = false;

    MarkerViewModel model;

    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private ArrayList<Marker> currentMarkers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.savedInstanceState = savedInstanceState;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (servicesOK()) {

            //initiate UI
            setContentView(R.layout.nav_drawer_main);
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            FloatingActionButton fab = findViewById(R.id.fab);

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

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            assert drawer != null;
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = findViewById(R.id.nav_view);
            assert navigationView != null;
            navigationView.setNavigationItemSelectedListener(this);

            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            model = ViewModelProviders.of(this).get(MarkerViewModel.class);

        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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


        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);


        View poweredByGoogle = autocompleteFragment.getView().findViewById(R.id.places_autocomplete_powered_by_google);
        if(poweredByGoogle != null){
            poweredByGoogle.setVisibility(View.GONE);
        }
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(place.getLatLng())
                        .zoom(10).build();
                //Zoom in and animate the camera.
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //final MenuItem searchItem = menu.findItem(R.id.search);
        //SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        //final SearchView searchView = (SearchView) searchItem.getActionView();
        //searchView.setQueryHint(getString(R.string.search_for_hint));

        //searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

/*
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
*/
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
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlertToUser();
        }
    }

    //Function to alert user if GPS is currently turned off on the device
    private void showGPSDisabledAlertToUser() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.Alert_message_GPS_is_disabled)
                .setPositiveButton(R.string.Go_to_gps_settings_text,
                        (dialog, id) -> {
                            //Set the dialogshown to true. user has seen this alert
                            dialogShown = true;
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, GPS_ENABLE_REQUEST);
                        });

        alertDialogBuilder.setNegativeButton(R.string.cancel,
                (dialog, id) -> dialog.cancel());
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
    public void enableGPS() {

        //Needs to have this check
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        } else {
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

        if (client != null) {
            client.connect();
            //loadGroundOverlayData();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (client != null && client.isConnected()) {
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
        gotoLocation(latitude, longitude, zoom);
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
            startActivityForResult(i, 3);

        } else if (id == R.id.nav_home) {

            loadGroundOverlayData();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //PARENT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GPS_ENABLE_REQUEST && resultCode == 0) {
            String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (provider != null) {
                Log.v(TAG, " Location providers: " + provider);
                Toast.makeText(this, R.string.gps_has_been_turned_on_toast,
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.GPS_is_turned_off_toast,
                        Toast.LENGTH_LONG).show();
            }
        }

        loadGroundOverlayData();

    }

    private void loadGroundOverlayData() {

        mapObjects = ((App)getApplication()).getDaoSession().getMapObjectDao().queryBuilder().list();

        //clear the map from old overlays.
        mMap.clear();

        for (MapObject m : mapObjects) {
            if(m.getTiePointOne() == null || m.getTiePointTwo() == null){
                //Something wrong with map object, skip
                continue;
            }
            //If the mapobject has two tiepoints, place a the groundoverlay on the map.
            if (m.getTiePointOne() > 0 && m.getTiePointTwo() > 0) {
                //Add all groundoverlays to the map
                GroundOverlayOptions gO = new GroundOverlayOptions();
                LatLng latLng = new LatLng(m.getTiePointOne(), m.getTiePointTwo());

                //Just for logging
                Log.d(TAG, "Tiepoint one is: " + m.getTiePointOne());
                Log.d(TAG, "Tiepoint two is: " + m.getTiePointTwo());
                Log.d(TAG, "LATNG:  = " + latLng);
                Log.d(TAG, "Width : = " + m.getSize());
                Log.d(TAG, "Bearing: =  " + m.getRotation());
                Log.d(TAG, "FILENAME: = " + m.getBitmapName());
                Log.d(TAG, "FilePath: = " + m.getFilePath());

                gO.position(latLng, (float) m.getSize());
                gO.bearing((float) m.getRotation());
                Bitmap bM = loadImageFromStorage(m.getFilePath(), String.valueOf(m.getId()));
                gO.image(BitmapDescriptorFactory.fromBitmap(bM));

                mMap.addGroundOverlay(gO);

            }
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onMapLongClick(LatLng point) {
        // Creating an instance of MarkerOptions to set position
        MarkerOptions markerOptions = new MarkerOptions();

        // Setting position on the MarkerOptions
        markerOptions.position(point);

        // Animating to the currently touched position
        mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

        // Adding marker on the GoogleMap

        if (latestMarker != null) {
            latestMarker.remove();
            latestMarker = null;
        }

        Marker marker = mMap.addMarker(markerOptions);

        latestMarker = marker;
        // Showing InfoWindow on the GoogleMap
        marker.showInfoWindow();

        showMarker(marker, null);


    }

    private void showMarker(final Marker marker, final MarkerObject mo) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(marker.getPosition())
                .zoom(17).build();
        //Zoom in and animate the camera.
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {

                RoundedBottomSheetDialogFragment f;

                if(mo != null){
                    f = RoundedBottomSheetDialogFragment.newInstance(mo);
                } else {
                    f = RoundedBottomSheetDialogFragment.newInstance(marker);
                }

                f.show(getSupportFragmentManager(), "add_photo_dialog_fragment");
            }

            @Override
            public void onCancel() {

            }
        });


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLoadedCallback(this);


        model.getMarkerObjectList().observe(this, markerObjectList -> {

            ArrayList<Marker> a = getCurrentMarkers();

            for(Marker m : a){
                m.remove();
            }

            a.clear();

            for(MarkerObject marker : markerObjectList){
                a.add(mMap.addMarker(marker.getMarkerOptions()));
            }

            setCurrentMarkers(a);
        });

        enableMyLocation();

        //initialZoom();

        if (getIntent().getDoubleExtra("MAP_LATITUDE", 0) > 0 && getIntent().getDoubleExtra("MAP_LONGITUDE", 0) > 0) {

            gotoLocation(getIntent().getDoubleExtra("MAP_LATITUDE", 0), getIntent().getDoubleExtra("MAP_LONGITUDE", 0), 12);
            //Reset the intent
            getIntent().putExtra("MAP_LATITUDE", 0);
            getIntent().putExtra("MAP_LONGITUDE", 0);


            //If the app is opened for the first time the instacestate is null.
            //Move the camera to users position, fairly zoomed out.
        } else if (savedInstanceState == null) {
            loadGroundOverlayData();
        }

    }

    private void setCurrentMarkers(ArrayList<Marker> markers) {
        this.currentMarkers = markers;
    }

    private ArrayList<Marker> getCurrentMarkers() {
        if(currentMarkers == null)
            currentMarkers = new ArrayList<>();

        return currentMarkers;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            initialZoom();
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void initialZoom() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            // Get LocationManager object from System Service LOCATION_SERVICE
            final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            // Create a criteria object to retrieve provider
            /*Criteria criteria = new Criteria();

            // Get the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            if(provider != null){
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }*/
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    private void setInfoWindow() {
        // Setting a custom info window adapter for the google map


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Getting view from the layout file info_window_layout
                View v = getLayoutInflater().inflate(R.layout.marker_info_window, null);

                // Getting the position from the marker
                LatLng latLng = marker.getPosition();

                // Getting reference to the TextView to set latitude
                TextView tvLat = v.findViewById(R.id.tv_lat);

                // Getting reference to the TextView to set longitude
                TextView tvLng = v.findViewById(R.id.tv_lng);

                // Setting the latitude
                tvLat.setText("Latitude:" + latLng.latitude);

                // Setting the longitude
                tvLng.setText("Longitude:"+ latLng.longitude);

                // Returning the view containing InfoWindow contents
                return v;
            }
        });
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        MarkerObject mo = model.getMarkerObjectFromMarker(marker);

        if (latestMarker != null && marker != latestMarker) {
            latestMarker.remove();
            latestMarker = null;
        }

        showMarker(marker, mo);

        return true;

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(cameraUpdate);
        locationManager.removeUpdates(this);
        loadingWrapper = findViewById(R.id.loading_wrapper);
        loadingWrapper.setVisibility(View.GONE);


    }

    @Override
    public void onMapLoaded() {
        /* loadingWrapper = findViewById(R.id.loading_wrapper);
        loadingWrapper.setVisibility(View.GONE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            initialZoom();
        }*/
    }




    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
