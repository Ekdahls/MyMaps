package se.simonekdahl.mymaps;

/**
 * Created by Simon on 16-03-22.
 * Database handler class
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DBHandler extends SQLiteOpenHelper {

    //If you update your database object you need to update the database version!
    private static final int DATABASE_VERSION = 4;
    //Name of the database file
    private static final String DATABASE_NAME = "MyMaps.db";
    //Name of the table
    public static final String TABLE_MAPS = "Maps_table";
    //For every single column
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MAPNAME = "mapname";
    public static final String COLUMN_MAPDESCRIPTION = "mapdescription";
    public static final String COLUMN_BITMAP_NAME = "bitmapname";
    public static final String COLUMN_FILE_PATH = "filepath";
    public static final String COLUMN_TIEPOINT_ONE ="tiepointone";
    public static final String COLUMN_TIEPOINT_TWO ="tiepointtwo";
    public static final String COLUMN_ROTATION = "rotation";
    public static final String COLUMN_SIZE = "size";

    private String[] columns= {COLUMN_ID, COLUMN_MAPNAME, COLUMN_MAPDESCRIPTION, COLUMN_BITMAP_NAME,
            COLUMN_FILE_PATH,COLUMN_TIEPOINT_ONE,COLUMN_TIEPOINT_TWO,COLUMN_ROTATION,COLUMN_SIZE};

    private static final String TAG = "DBHandler";

    public DBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    public DBHandler(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String query = "CREATE TABLE " + TABLE_MAPS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_MAPNAME + " TEXT," +
                COLUMN_MAPDESCRIPTION + " TEXT," +
                COLUMN_BITMAP_NAME + " TEXT," +
                COLUMN_FILE_PATH + " TEXT," +
                COLUMN_TIEPOINT_ONE + " REAL,"+
                COLUMN_TIEPOINT_TWO + " REAL,"+
                COLUMN_ROTATION + " REAL," +
                COLUMN_SIZE + " REAL"
                + ")";
        db.execSQL(query);

    }


    //If database is updated, drop (delete) the previous db
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MAPS);
        onCreate(db);
    }

    //Add a new row to the database
    //imagefilename is the name of the bitmap refering to the _id of the sqlite object with mapname
    public long addMap(MapObject1 mapObject) {

        ContentValues values = new ContentValues();
        values.put(COLUMN_MAPNAME, mapObject.get_mapName());
        values.put(COLUMN_MAPDESCRIPTION, mapObject.get_mapDescription());
        values.put(COLUMN_BITMAP_NAME, mapObject.get_bitmapName());
        values.put(COLUMN_FILE_PATH, mapObject.get_filePath());
        values.put(COLUMN_TIEPOINT_ONE, mapObject.get_tiePointOne());
        values.put(COLUMN_TIEPOINT_TWO, mapObject.get_tiePointTwo());
        values.put(COLUMN_ROTATION, mapObject.get_Rotation());
        values.put(COLUMN_SIZE, mapObject.get_Size());

        SQLiteDatabase db = getWritableDatabase();
        Long id = db.insert(TABLE_MAPS,null, values);
        db.close();
        return id;
    }

    //funtion to add a mapName to the database
    public void addMapNameToDB(long id){

        String fileName = String.valueOf(id);
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_BITMAP_NAME + "="
                + fileName + " WHERE " + COLUMN_ID + " = " + id);
        //Close db??
    }

    //Function to add groundoverlay with and rotation to database
    public void addWidthAndBearingToMap(long id, double width, double bearing){

        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_SIZE + "="
                + width + " WHERE " + COLUMN_ID + " = " + id);
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_ROTATION + "="
                + bearing + " WHERE " + COLUMN_ID + " = " + id);
        db.close();

    }

    //function to read all mapobjects from db
    public List<MapObject1> readAllMaps(){

        //get db writable
        SQLiteDatabase db = getWritableDatabase();
        List<MapObject1> mapObjects = new ArrayList<MapObject1>();
        Cursor c = db.query(TABLE_MAPS, columns, null, null, null,null,null);

        c.moveToFirst();

        while ((!c.isAfterLast())){
            MapObject1 mapObject = new MapObject1();
            mapObject.set_id(Integer.parseInt(c.getString(0)));
            mapObject.set_mapName(c.getString(1));
            mapObject.set_mapDescription(c.getString(2));
            mapObject.set_bitmapName(c.getString(3));
            mapObject.set_filePath(c.getString(4));
            mapObject.set_tiePointOne(c.getDouble(5));
            mapObject.set_tiePointTwo(c.getDouble(6));
            mapObject.set_Rotation(c.getDouble(7));
            mapObject.set_Size(c.getDouble(8));

            mapObjects.add(mapObject);
            c.moveToNext();
        }

        //close the cursor
        c.close();
        return mapObjects;
    }


    //Deleting mapobject
    public boolean deleteMap(MapObject1 mapObject){

        String filePath = getFilePathToImage(mapObject.get_id());
        SQLiteDatabase db = getWritableDatabase();
        int i = db.delete(TABLE_MAPS, COLUMN_ID + " = ?", new String[]{String.valueOf(mapObject.get_id())});
        db.close();

        //Remember to delete image object in phones internal memory as well.
        if(i !=0){

            Log.d(TAG, "Map reference was deleted from Database: ");
           boolean deleted = deleteImageFile(filePath,mapObject.get_id());
            if(deleted){
                Log.d(TAG, "internal mapfile was deleted!");
            }

            return true;
        }else{
            return false;
        }
    }


    //add anchorpoints latitude and longitude to mapobject in database
    public void addAnchorsToMap(long id, double anchorU, double anchorV){

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_TIEPOINT_ONE + "="
                + anchorU + " WHERE " + COLUMN_ID + " = " + id);
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_TIEPOINT_TWO + "="
                + anchorV + " WHERE " + COLUMN_ID + " = " + id);

        db.close();
    }

    //funtion for deleting imagefile from internal memory , is called when mapobject is deleted from db
    public boolean deleteImageFile(String filePath, long id){

        String fileName = String.valueOf(id);

        File file = new File(filePath, fileName + ".png");

        return file.delete();
    }


    //funtion to add filepath to corresponding image in internal storage to database
    public void addFilePathToDB(long id, String filePath){
        SQLiteDatabase db = getReadableDatabase();
        db.execSQL("UPDATE " + TABLE_MAPS + " SET " + COLUMN_FILE_PATH + " = \""
                + filePath + "\" WHERE " + COLUMN_ID + " = " + id);
    }

    //Function to get filepath to saved corresponding image in database.
    public String getFilePathToImage(long id){

        SQLiteDatabase db = getWritableDatabase();
        String filePath = null;
        Cursor c = db.rawQuery("SELECT " + COLUMN_FILE_PATH + " FROM "
                + TABLE_MAPS + " WHERE " + COLUMN_ID + " = "
                + id , null);

        if (c.getCount() > 0) {
            c.moveToFirst();

            //Read first and only column (1 row 1 column)
            filePath = c.getString(0);
        }
        c.close();
        db.close();

        SQLiteDatabase.releaseMemory();

        return filePath;
    }


    public void updateMap(MapObject1 mapObject) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(COLUMN_MAPNAME, mapObject.get_mapName());
        values.put(COLUMN_MAPDESCRIPTION, mapObject.get_mapDescription());
        values.put(COLUMN_BITMAP_NAME, mapObject.get_bitmapName());
        values.put(COLUMN_FILE_PATH, mapObject.get_filePath());
        values.put(COLUMN_TIEPOINT_ONE, mapObject.get_tiePointOne());
        values.put(COLUMN_TIEPOINT_TWO, mapObject.get_tiePointTwo());
        values.put(COLUMN_ROTATION, mapObject.get_Rotation());
        values.put(COLUMN_SIZE, mapObject.get_Size());


        db.update(TABLE_MAPS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(mapObject.get_id()) });

        db.close();

    }
}