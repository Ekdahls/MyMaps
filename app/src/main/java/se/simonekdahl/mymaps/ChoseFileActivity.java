package se.simonekdahl.mymaps;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.appcompat.widget.Toolbar;
import se.simonekdahl.mymaps.dao.DaoSession;
import se.simonekdahl.mymaps.dao.MapObject;
import se.simonekdahl.mymaps.dao.MapObjectDao;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.Toast;

import org.greenrobot.greendao.query.WhereCondition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ChoseFileActivity extends ParentActivity {

    private EditText mMapNameTextView, mMapDescriptionTextView;
    private Button mAddImageButton, mPlaceImageButton;
    private ImageView imgPicture;
    public static final int IMAGE_GALLERY_REQUEST = 2;
    public static final int NO_OF_LINES_IN_DESCRIPTION = 3;
    private static final String TAG = "ChoseFileActivity";
    public static final int GROUND_OVERLAY_REQUEST = 3;


    //Temp placeholders for information.
    private MapObject mapObject;
    private Bitmap bitmapImage = null;
    private String mapName = null;
    private String mapDescription = null;

    private MapObjectDao mapObjectDao;

    //Databasehandler
    DBHandler dbHandler;

    private MapObjectDao getMapObjectDao(){
        if(mapObjectDao == null){
            mapObjectDao = getDaoSession().getMapObjectDao();
        }

        return mapObjectDao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initiate UI
        setContentView(R.layout.activity_chose_file);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        imgPicture = (ImageView) findViewById(R.id.imageView_mapImage);
        mMapNameTextView = (EditText) findViewById(R.id.editText_mapName);
        mMapDescriptionTextView = (EditText) findViewById(R.id.editText_mapDescription);
        mMapDescriptionTextView.setScroller(new Scroller(this));
        mMapDescriptionTextView.setMaxLines(NO_OF_LINES_IN_DESCRIPTION);
        mMapDescriptionTextView.setVerticalScrollBarEnabled(true);
        mMapDescriptionTextView.setMovementMethod(new ScrollingMovementMethod());

        mAddImageButton = (Button) findViewById(R.id.button_choose_image);
        mPlaceImageButton = (Button) findViewById(R.id.button_place_map);

        assert mPlaceImageButton != null;
        mPlaceImageButton.setAlpha(.2f);
        disablePlaceButton();
        dbHandler = new DBHandler(this,null,null,1);
    }


    //Function to add image from device storage via external photopickerintent.
    public void addImage(View view) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        //Pick data from
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();
        //Get a URI representation
        Uri data = Uri.parse(pictureDirectoryPath);
        //set the datatype (get all types of image files)
        photoPickerIntent.setDataAndType(data, "image/*");
        //the number (NAME)  in last is the fingerprint of the intent.
        startActivityForResult(photoPickerIntent, IMAGE_GALLERY_REQUEST);

    }

    //Function to place image onto map, opens groundoverlayactivity for this.
    public void placeImage(View view){

        //saves name and description of texviews
        mapName = mMapNameTextView.getText().toString();
        mapDescription = mMapDescriptionTextView.getText().toString();

        mapObject = new MapObject();
        mapObject.setDescription(mapDescription);
        mapObject.setName(mapName);
        //Set id as filename in database
        long id = saveToDB(mapObject,bitmapImage);

        MapObject mapObject = getMapObjectDao().queryBuilder().where(MapObjectDao.Properties.Id.eq(id)).unique();


        Log.d(TAG, "mapName: " + mapName);
        Log.d(TAG, "Mapdesc: " + mapDescription);

        //Start new intent.
        Intent i = new Intent(ChoseFileActivity.this, GroundOverlayActivity.class);

        //Add extras to intent
        i.putExtra("MAP_IMAGE_ID", id);
        i.putExtra("MAP_IMAGE_FILE_PATH", mapObject.getFilePath());
        //For the camera position when add new map button was pressed.
        i.putExtra("CURRENT_LATITUDE",getIntent().getDoubleExtra("CURRENT_LATITUDE", 0));
        i.putExtra("CURRENT_LONGITUDE", getIntent().getDoubleExtra("CURRENT_LONGITUDE", 0));
        i.putExtra("CURRENT_ZOOM", getIntent().getFloatExtra("CURRENT_ZOOM", 0));

        startActivityForResult(i, GROUND_OVERLAY_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save, menu);
        return true;

    }

    //Function for diableing the place button
    public void disablePlaceButton(){

        mPlaceImageButton.setClickable(false);
        mPlaceImageButton.setAlpha(.1f);

    }

    //Function to enable place button
    public void enablePlaceButton (){
            mPlaceImageButton.setClickable(true);
            mPlaceImageButton.setAlpha(1f);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //checks if user picked an image.
        if(resultCode==RESULT_OK){
            //Function processed successfully
            if(requestCode == IMAGE_GALLERY_REQUEST){
                //We got something back from gallery
                Uri imageUri = data.getData();
                //declare a stream to read image data from gallery
                InputStream inputStream;
                //we are trying to get an inputstream, based on the URI of the image
                try {
                    if(imgPicture!=null) {
                        imgPicture.setImageBitmap(null);
                    }
                    inputStream = getContentResolver().openInputStream(imageUri);
                    //get a bitmap from the stream
                    Bitmap image = BitmapFactory.decodeStream(inputStream);
                    bitmapImage = image;
                    imgPicture.setImageBitmap(bitmapImage);
                    enablePlaceButton();

                    //In future version the bitmap will save to internal storage right away when chosen
                    //Now user has to press save or place button in order to do this.
                    //Rotating the phone will make image go away.

                    assert inputStream != null;
                    inputStream.close();
                    //if user has not given the map a Name. Set the name of the map as file-name
                    if(mMapNameTextView.getText().length() == 0) {
                        String fileName = getFileNameByUri(this, imageUri);
                        Log.i(TAG, getString(R.string.filename_is) + fileName);
                        if (fileName != null) {
                            mMapNameTextView.setText(fileName);
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //if image is unavailabe, show this message to user.
                    Toast.makeText(this, R.string.Unable_to_open_image_toast, Toast.LENGTH_LONG);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (requestCode == GROUND_OVERLAY_REQUEST){

                String toastText = getString(R.string.saved_map);
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
                finish();
            }
        }


    }

    //function for saving Mapinfo to database and mapimage to internal storage
    public long saveToDB(MapObject mapObject, Bitmap bitmap){

        long id = getMapObjectDao().insert(mapObject);
        mapObject.update();

        mapObject.setBitmapName(String.valueOf(id));
        getMapObjectDao().save(mapObject);

        String fileName = String.valueOf(id);
        String filePath = saveToInternalStorage(bitmap, fileName);

        mapObject.setFilePath(filePath);

        getMapObjectDao().save(mapObject);

        return id;

    }


    //function to get filename from imagefile uRI
    public static String getFileNameByUri(Context context, Uri uri)
    {
        String fileName="unknown";//default fileName
        Uri filePathUri = uri;

        if (uri!=null && uri.getScheme().compareTo("content")==0)
        {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            assert cursor != null;
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                //Instead of "MediaStore.Images.Media.DATA" can be used "_data"
                filePathUri = Uri.parse(cursor.getString(column_index));
                fileName = filePathUri.getLastPathSegment();
                cursor.close();
            }
        }
        else {
            assert uri != null;
            if (uri.getScheme().compareTo("file")==0)
            {
                fileName = filePathUri.getLastPathSegment();
            }
            else
            {
                fileName = fileName+"_"+filePathUri.getLastPathSegment();
            }
        }
        return fileName;
    }

    //Options to select map-type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.save:

                mapName = mMapNameTextView.getText().toString();
                mapDescription = mMapDescriptionTextView.getText().toString();
                mapObject = new MapObject();
                mapObject.setName(mapName);
                mapObject.setDescription(mapDescription);
                mapObject.update();

                String toastText = getString(R.string.saved_map);
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
