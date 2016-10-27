package se.simonekdahl.mymaps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Simon on 16-03-17.
 */
public class ImageObject {

    private int _id;
    private Bitmap _bitmap;
    private String _mapName, _mapDescription;

    private LatLng _southWest = new LatLng(63.980953, 20.024248);
    private LatLng _northEast = new LatLng(64.040895, 20.109384);

    private LatLngBounds _latLngBounds;
    private GroundOverlay _GroundOverlay;
    private GroundOverlayOptions _groundOverlayOptions;
    private BitmapDescriptor _imageBitmap;

    public ImageObject(Bitmap image, String nameOfMap, String description){
        _mapName = nameOfMap;
        _mapDescription = description;
        setImage(image);
    }

    public ImageObject(){

    }

    public Bitmap get_bitmap() {
        return _bitmap;
    }

    public void set_bitmap(Bitmap _bitmap) {
        this._bitmap = _bitmap;
    }

    public GroundOverlay get_GroundOverlay() {
        return _GroundOverlay;
    }

    public void set_GroundOverlay(GroundOverlay _GroundOverlay) {
        this._GroundOverlay = _GroundOverlay;
    }

    public GroundOverlayOptions get_groundOverlayOptions() {
        return _groundOverlayOptions;
    }

    public void set_groundOverlayOptions(GroundOverlayOptions _groundOverlayOptions) {
        this._groundOverlayOptions = _groundOverlayOptions;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public BitmapDescriptor get_imageBitmap() {
        return _imageBitmap;
    }

    public void set_imageBitmap(BitmapDescriptor _imageBitmap) {
        this._imageBitmap = _imageBitmap;
    }

    public LatLngBounds get_latLngBounds() {
        return _latLngBounds;
    }

    public void set_latLngBounds(LatLngBounds _latLngBounds) {
        this._latLngBounds = _latLngBounds;
    }

    public String get_mapDescription() {
        return _mapDescription;
    }

    public void set_mapDescription(String _mapDescription) {
        this._mapDescription = _mapDescription;
    }

    public String get_mapName() {
        return _mapName;
    }

    public void set_mapName(String _mapName) {
        this._mapName = _mapName;
    }










    public void setImage(Bitmap image){

        _bitmap = image;
        updateBitmaoDescriptor();
    }

    public void updateBitmaoDescriptor(){
        _imageBitmap = BitmapDescriptorFactory.fromBitmap(_bitmap);
    }

    public void setGroundOverlay(GoogleMap map, int width){

        LatLng latLng=map.getCameraPosition().target;
        float zoomlevel = map.getCameraPosition().zoom;

        _groundOverlayOptions = new GroundOverlayOptions();
        _groundOverlayOptions.position(latLng, (float) width * 100 / zoomlevel);
        _groundOverlayOptions.image(_imageBitmap);

        /*
        _latLngBounds = new LatLngBounds(_southWest,_northEast);
        _groundOverlayOptions = new GroundOverlayOptions();
        _groundOverlayOptions.positionFromBounds(_latLngBounds);
        _groundOverlayOptions.image(_imageBitmap);
        */

    }
    public GroundOverlay getGroundOverlay(){
        return _GroundOverlay;

    }

    public void removeGroundOverlay(){
        _GroundOverlay.remove();
    }

    public Bitmap getBitmap(){

        return _bitmap;
    }

    public void fixGroundOverlay( GoogleMap map){
        _GroundOverlay = map.addGroundOverlay(_groundOverlayOptions);
    }




}
