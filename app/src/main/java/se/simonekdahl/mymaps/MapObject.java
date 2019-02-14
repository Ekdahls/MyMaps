package se.simonekdahl.mymaps;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.BitmapDescriptor;


/**
 * Created by Simon on 16-03-22.
 */
public class MapObject implements Parcelable {
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

    protected MapObject(Parcel in) {
        _id = in.readInt();
        _mapName = in.readString();
        _mapDescription = in.readString();
        _bitmapName = in.readString();
        _filePath = in.readString();
        _tiePointOne = in.readDouble();
        _tiePointTwo = in.readDouble();
        _Rotation = in.readDouble();
        _Size = in.readDouble();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(_id);
        dest.writeString(_mapName);
        dest.writeString(_mapDescription);
        dest.writeString(_bitmapName);
        dest.writeString(_filePath);
        dest.writeDouble(_tiePointOne);
        dest.writeDouble(_tiePointTwo);
        dest.writeDouble(_Rotation);
        dest.writeDouble(_Size);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MapObject> CREATOR = new Parcelable.Creator<MapObject>() {
        @Override
        public MapObject createFromParcel(Parcel in) {
            return new MapObject(in);
        }

        @Override
        public MapObject[] newArray(int size) {
            return new MapObject[size];
        }
    };

}
