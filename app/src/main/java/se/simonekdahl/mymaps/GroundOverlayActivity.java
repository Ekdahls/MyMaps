
package se.simonekdahl.mymaps;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import androidx.annotation.StringRes;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.SearchView;
import se.simonekdahl.mymaps.dao.MapObject;
import se.simonekdahl.mymaps.dao.MapObjectDao;

import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static se.simonekdahl.mymaps.utils.MapImageUtils.loadImageFromStorage;

/**
 * This shows how to add a ground overlay to a map.
 */
public class GroundOverlayActivity extends ParentActivity
        implements SeekBar.OnSeekBarChangeListener, OnMapReadyCallback,
         NavigationView.OnNavigationItemSelectedListener {

    private static final int TRANSPARENCY_MAX = 250;
    private static final int ROTAION_MAX = 360;
    private static final int SIZE_MAX = 1000;
    private static final int OVERLAY_TRANSPARENCY_MAX = 100;
    private static final int OVERLAY_ROTATION_MAX = 360;
    private static final int OVERLAY_SIZE_MAX = 40000;
    public static final int GROUND_OVERLAY_REQUEST = 3;

    private static final LatLng NEWARK = new LatLng(64.0074463, 20.036676);
    private static final LatLng NEAR_NEWARK =
            new LatLng(NEWARK.latitude - 0.001, NEWARK.longitude - 0.025);
    public static final int IMAGE_GALLERY_REQUEST = 2;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 2;

    private final List<BitmapDescriptor> mImages = new ArrayList<BitmapDescriptor>();

    private BitmapDescriptor mImage;
    private Bitmap mapFile;
    private OverlayObject mapOverlay;
    //private ImageObject mapImage;
    private ImageView imgPicture;
    private GroundOverlay mGroundOverlay;
    private SeekBar mTransparencyBar, mSizeBar, mRotationBar;
    private Button mPlaceMapBtn;

    private float currentRotation;
    private float currentOpacity;

    private static final String TAG = "GroundOverlayActivity";

    GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Load the mapimage to the imageview.
        long mapId = getIntent().getLongExtra("MAP_IMAGE_ID", 0);
        String filePath = getIntent().getStringExtra("MAP_IMAGE_FILE_PATH");

        mapFile = loadImageFromStorage(filePath, String.valueOf(mapId));
        mapOverlay = new OverlayObject(mapFile);

        setContentView(R.layout.activity_add_overlay);
        mapFile = drawDotOnMapCenter(mapFile);
        imgPicture = new ImageView(this);
        imgPicture = findViewById(R.id.imageMapView);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        displayImageView();

        mTransparencyBar = findViewById(R.id.transparencySeekBar);
        assert mTransparencyBar != null;
        mTransparencyBar.setMax(TRANSPARENCY_MAX);
        mTransparencyBar.setProgress(TRANSPARENCY_MAX / 2);
        currentOpacity = TRANSPARENCY_MAX / 2.0f;
        imgPicture.setAlpha(0.5f);

        mSizeBar = findViewById(R.id.sizeSeekBar);
        assert mSizeBar != null;
        mSizeBar.setMax(SIZE_MAX);
        mSizeBar.setProgress(SIZE_MAX);

        mRotationBar = findViewById(R.id.rotaionSeekBar);
        assert mRotationBar != null;
        mRotationBar.setMax(ROTAION_MAX);
        mRotationBar.setProgress(0);
        currentRotation = 0;

        mRotationBar.setOnSeekBarChangeListener(this);
        mSizeBar.setOnSeekBarChangeListener(this);
        mTransparencyBar.setOnSeekBarChangeListener(this);

        mPlaceMapBtn = findViewById(R.id.saveImage_btn);
        mPlaceMapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addGroundoverlay(v);
            }
        });

        //if map is loaded correct , enable GPS location manager
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        Toast.makeText(this, R.string.Toast_groundoverlayactivity_place_image_1of2, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        enableGPS();

        // Register a listener to respond to clicks on GroundOverlays.
        Log.d(TAG, "Current lat = : " + getIntent().getDoubleExtra("CURRENT_LATITUDE", 0));

        //addImageAsOverlay(map);
        mTransparencyBar.setOnSeekBarChangeListener(this);
        mRotationBar.setOnSeekBarChangeListener(this);
        mSizeBar.setOnSeekBarChangeListener(this);

        //If user has come from other acticity, set camera to that location
        if(getIntent().getDoubleExtra("CURRENT_LATITUDE", 0) > 0 && getIntent().getDoubleExtra("CURRENT_LONGITUDE", 0) > 0) {
            gotoLocation(getIntent().getDoubleExtra("CURRENT_LATITUDE", 0),
                    getIntent().getDoubleExtra("CURRENT_LONGITUDE", 0),
                    getIntent().getFloatExtra("CURRENT_ZOOM", 0));
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    //Function for scaling the imageview of the map
    private void scale(int progress){

        imgPicture.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgPicture.setScaleX((float) progress/(1000/3));
        imgPicture.setScaleY((float) progress/(1000/3));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if(seekBar == mTransparencyBar) {
                currentOpacity = progress;
                if (mapOverlay.getGroundOverlay() != null) {
                    mapOverlay.getGroundOverlay().setTransparency((100 - (float) progress) / OVERLAY_TRANSPARENCY_MAX);
                } else {
                    imgPicture.setAlpha((float) progress / (float) TRANSPARENCY_MAX);
                }
            }
            if (seekBar == mRotationBar) {
                currentRotation = progress;
                if (mapOverlay.getGroundOverlay() != null) {
                    mapOverlay.getGroundOverlay().setBearing((float) progress);
                }else{
                    imgPicture.setRotation(progress);
                }
            }
            if (seekBar == mSizeBar) {

                if (mapOverlay.getGroundOverlay() != null) {
                    mapOverlay.getGroundOverlay().setDimensions((float) progress );
                }else{

                    scale(progress);
                }
            }

    }


    //Function to draw a small red dot in center of mapimage in order to know where the anchorpoint
    //for the rotation is.
    public Bitmap drawDotOnMapCenter(Bitmap bm){

        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        Bitmap workingBitmap = Bitmap.createBitmap(bm);
        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(mutableBitmap);

        canvas.drawCircle(workingBitmap.getWidth()/2, workingBitmap.getHeight()/2,
                workingBitmap.getWidth()/50, paint);

        return mutableBitmap;
    }

    //Function to add the ground overlay to the image
    public void addGroundoverlay(View view){

        if(mapOverlay.getGroundOverlay()==null) {
            imgPicture.setImageBitmap(null);
            assert (findViewById(R.id.imageMapView)) != null;
            mRotationBar.setMax(OVERLAY_ROTATION_MAX);
          //  mSizeBar.setMax(OVERLAY_SIZE_MAX);
            mTransparencyBar.setMax(OVERLAY_TRANSPARENCY_MAX);
            int newOpacityProgress = (int) ((currentOpacity / 250) * 100);
            mTransparencyBar.setProgress(newOpacityProgress);

            double sizeMax = mapOverlay.setGroundOverlay(mMap);
            mapOverlay.fixGroundOverlay(mMap);
            mSizeBar.setMax((int) (sizeMax * 1.2));
            mSizeBar.setProgress((int) sizeMax);

            mPlaceMapBtn.setText(R.string.save_image);
            Toast.makeText(this, R.string.toast_groundoverlayactivity_place_map2of2, Toast.LENGTH_SHORT).show();

            mapOverlay.getGroundOverlay().setBearing(currentRotation);
            mapOverlay.getGroundOverlay().setTransparency((float)(100 - newOpacityProgress)/OVERLAY_TRANSPARENCY_MAX);

        }else{

            MapObjectDao mapObjectDao = ((App)getApplication()).getDaoSession().getMapObjectDao();

            //User saved overlay, update database accordingly
            long mapId = getIntent().getLongExtra("MAP_IMAGE_ID", 0);

            MapObject map = mapObjectDao.load(mapId);

            if(map != null){
                map.setTiePointOne(mapOverlay.getLatitude());
                map.setTiePointTwo(mapOverlay.getLongitude());

                map.setSize(mapOverlay.getWidth());
                map.setRotation(mapOverlay.getBearing());

                mapObjectDao.update(map);
            }else{

            }


            //Finish activity.
            setResult(Activity.RESULT_OK);



            finish();
        }
    }




    public void displayImageView(){
        imgPicture.setImageBitmap(mapFile);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        deleteCurrentMap();
        super.onBackPressed();
    }

    private void deleteCurrentMap(){
        MapObjectDao mapObjectDao = ((App)getApplication()).getDaoSession().getMapObjectDao();

        //User saved overlay, update database accordingly
        long mapId = getIntent().getLongExtra("MAP_IMAGE_ID", 0);

        mapObjectDao.deleteByKey(mapId);
    }

    //Options to select map-type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
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
            case R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    //Function to go to a certain location
    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    //Function to hide the keyboard
    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //Function to find the destination input from user.
    public void geoLocateAppBar(View v, String s) throws IOException {

        //hide the keyboard.
        hideSoftKeyboard(v);
        String searchString = s;
        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1);

        if (list.size() > 0) {
            Address address = list.get(0);
            String locality = address.getLocality();

            if (locality != null) {
                Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();
            }

            double lat = address.getLatitude();
            double lng = address.getLongitude();
            //Go to the location, zoomlevel is 12
            gotoLocation(lat, lng, 12);
        }
    }

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


}
