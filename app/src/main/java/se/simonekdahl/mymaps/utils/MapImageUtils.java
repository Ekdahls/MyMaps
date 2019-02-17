package se.simonekdahl.mymaps.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import se.simonekdahl.mymaps.dao.MapObject;

public class MapImageUtils {

    static public void deleteMapFile(Context context, MapObject map){

    }

    //Function for saving image to internal storage folder imageDir
    static public String saveToInternalStorage(Bitmap bitmapImage, String fileName, Context context){

        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory, fileName + ".png");

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

    static public File getImageFile(String path, String filename){
        return new File(path, filename + ".png");
    }


    static public Bitmap loadImageFromStorage(String path, String fileName)
    {
        Bitmap b = null;

        try {
            File f=new File(path, fileName + ".png" );
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

        return b;
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


}
