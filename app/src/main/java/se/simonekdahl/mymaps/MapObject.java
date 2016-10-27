package se.simonekdahl.mymaps;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.BitmapDescriptor;


/**
 * Created by Simon on 16-03-22.
 */
public class MapObject {
    // private Bitmap _bitmap;
    // private BitmapDescriptor _imageBitmap;
    private int _id;
    private String _mapName;
    private String _mapDescription;
    private String _bitmapName;
    private String _filePath;
    private double _tiePointOne;
    private double _tiePointTwo;
    private double _Rotation;
    private double _Size;



    public MapObject(String mapName, String mapDescription){
        _mapName = mapName;
        _mapDescription = mapDescription;

    }

    public MapObject(){

    }

    public String get_bitmapName() {
        return _bitmapName;
    }

    public void set_bitmapName(String _bitmapName) {
        this._bitmapName = _bitmapName;
    }

    public String get_filePath() {
        return _filePath;
    }

    public void set_filePath(String _filePath) {
        this._filePath = _filePath;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
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

    public double get_Rotation() {
        return _Rotation;
    }

    public void set_Rotation(double _Rotation) {
        this._Rotation = _Rotation;
    }

    public double get_Size() {
        return _Size;
    }

    public void set_Size(double _Size) {
        this._Size = _Size;
    }

    public double get_tiePointOne() {
        return _tiePointOne;
    }

    public void set_tiePointOne(double _tiePointOne) {
        this._tiePointOne = _tiePointOne;
    }

    public double get_tiePointTwo() {
        return _tiePointTwo;
    }

    public void set_tiePointTwo(double _tiePointTwo) {
        this._tiePointTwo = _tiePointTwo;
    }
}
