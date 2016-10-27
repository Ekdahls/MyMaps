package se.simonekdahl.mymaps;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Simon on 16-03-22.
 */
public class OverlayObject {

    //not used in this version.
    private LatLng southWest ;
    private LatLng northEast ;

    private Bitmap image;
    private GroundOverlay groundOverlay;
    private GroundOverlayOptions groundOverlayOptions;
    private BitmapDescriptor imageBitmap;
    private static final String TAG = "OverlayObject";

    public OverlayObject(){

    }

    public OverlayObject(Bitmap image){
        this.image = image;
        updateBitmaoDescriptor();
}

    public double getLatitude(){
        return  groundOverlay.getPosition().latitude;
    }

    public double getLongitude(){

        return groundOverlay.getPosition().longitude;
    }

    public double getBearing(){
        return groundOverlay.getBearing();
    }

    public double getWidth(){
        return groundOverlay.getWidth();
    }

    public double setGroundOverlay(GoogleMap map){

        LatLng latLng=map.getCameraPosition().target;

        double width = getWidthOfOverlay(map);

        groundOverlayOptions = new GroundOverlayOptions();
        groundOverlayOptions.position(latLng, (float) width);
        groundOverlayOptions.image(imageBitmap);

        return width;
    }


    //Function to calculate the with of the overlay depending on current zoomlevel on Google maps.
    public double getWidthOfOverlay(GoogleMap mMap){

        //Get bounds of visible region on screen.
        LatLngBounds myBounds = mMap.getProjection().getVisibleRegion().latLngBounds;
        LatLng myCenter=  mMap.getCameraPosition().target;

        if (myCenter.latitude==0 || myCenter.longitude==0) {
            myCenter=new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);
        }

        LatLng ne = myBounds.northeast;

        // r = radius of the earth in statute miles
        double r = 3963.0;

        // Convert lat or lng from decimal degrees into radians by dividing by 57.2958
        double lat1 = myCenter.latitude / 57.2958;
        double lon1 = myCenter.longitude / 57.2958;
        final double lat2 = ne.latitude / 57.2958;
        final double lon2 = ne.longitude / 57.2958;

        // distance = circle radius from center to Northeast corner of bounds
        double dis = r * Math.acos(Math.sin(lat1) * Math.sin(lat2) +
                Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1));

        //1 Meter = 0.000621371192237334 Miles
        double meters_calc=dis/0.000621371192237334;
        return meters_calc;
    }


    public void updateBitmaoDescriptor(){
        imageBitmap = BitmapDescriptorFactory.fromBitmap(image);
    }

    public GroundOverlay getGroundOverlay(){
        return groundOverlay;

    }

    public void fixGroundOverlay( GoogleMap map){
        groundOverlay = map.addGroundOverlay(groundOverlayOptions);
    }

}
