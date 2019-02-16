package se.simonekdahl.mymaps;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import se.simonekdahl.mymaps.dao.DaoMaster;
import se.simonekdahl.mymaps.dao.DaoSession;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import org.greenrobot.greendao.database.Database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class ParentActivity extends AppCompatActivity{

    private static final String TAG = "ParentActivity";
    private String FILE_NAME = "";
    private DaoSession daoSession;
    private  Database db;
    private SQLiteDatabase sqlDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // regular SQLite database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db");
        db = helper.getWritableDb();
        sqlDatabase = helper.getWritableDatabase();

        // encrypted SQLCipher database
        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
        // Database db = helper.getEncryptedWritableDb("encryption-key");

        daoSession = new DaoMaster(db).newSession();

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Database getDb(){
        return db;
    }

    public SQLiteDatabase getSqlDatabase(){
        return sqlDatabase;
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     * This activity is used in order to hide the soft keyboard when user clicks outside editTex
     * Can be used as an extenstion by any activity who needs this function.
     */




    //If backarrow is pressed. terminate the activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    //Funtion for hiding keyboard when user clicks outside edittexview
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && view instanceof EditText && !view.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            view.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + view.getLeft() - scrcoords[0];
            float y = ev.getRawY() + view.getTop() - scrcoords[1];
            if (x < view.getLeft() || x > view.getRight() || y < view.getTop() || y > view.getBottom())
                hideKeyboard(this);
        }
        return super.dispatchTouchEvent(ev);
    }

    public static void hideKeyboard(Activity act) {
        if(act!=null)
            ((InputMethodManager)act.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow((act.getWindow().getDecorView().getApplicationWindowToken()), 0);
    }

    public boolean saveImageToInternalStorage(Bitmap image, String fileName) {

        try {

            FILE_NAME = fileName;
        // Use the compress method on the Bitmap object to write image to the OutputStream
            FileOutputStream fos = this.openFileOutput(FILE_NAME + ".png", Context.MODE_PRIVATE);

            // Writing the bitmap to the output stream, 100 stands for full resolution
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return true;
        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
            return false;
        }
    }


    //Function for saving image to internal storage folder imageDir
    public String saveToInternalStorage(Bitmap bitmapImage, String fileName){

        FILE_NAME = fileName;

        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, FILE_NAME + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }


    public Bitmap loadImageFromStorage(String path, long fileName)
    {
        Bitmap b = null;

        try {
            File f=new File(path, String.valueOf(fileName) + ".png" );
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return b;
    }

}

