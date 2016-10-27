package se.simonekdahl.mymaps;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Simon on 16-03-23.
 */
public class MapDetails extends ParentActivity {

    private static final String TAG = "MapDetails";

        private DBHandler handler;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.map_details);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_details);
            setSupportActionBar(toolbar);
            handler = new DBHandler(this);

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            final Bundle extras = getIntent().getExtras();

            Log.d(TAG, "onCreate: extras == " );

            ImageView mMapimage = (ImageView) findViewById(R.id.map_image_view_in_details);

            if(extras.getString("MAP_IMAGE_FILEPATH") !=null &&
             extras.getString("MAP_IMAGE_NAME")!=null) {
                mMapimage.setImageBitmap(loadImageFromStorage(extras.getString("MAP_IMAGE_FILEPATH")
                        , extras.getString("MAP_IMAGE_NAME")));
            }
            TextView mMapname = (TextView) findViewById(R.id.textView_insert_mapname);
            if(extras.getString("MAP_NAME") != null) {
                mMapname.setText(extras.getString("MAP_NAME"));
            }
            TextView mMapDesc = (TextView) findViewById(R.id.textView_insert_desc);

            if(extras.getString("MAP_DESC")!=null) {
                mMapDesc.setText(extras.getString("MAP_DESC"));
            }
            TextView mOther = (TextView) findViewById(R.id.textView_otherinfo_insert);

            int id = extras.getInt("MAP_ID", 0);
            mOther.setText(String.valueOf(id));


                    Button btn_back = (Button) findViewById(R.id.btn_back_to_maps);
            assert btn_back != null;
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            Button btn_goto = (Button) findViewById(R.id.button_go_to_map);
            assert btn_goto != null;
            btn_goto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(extras.getDouble("MAP_TIEPOINT_ONE",0)>0 && extras.getDouble("MAP_TIEPOINT_TWO")>0 ){
                        Intent i = new Intent(MapDetails.this, MainActivity.class);

                        i.putExtra("MAP_LATITUDE", extras.getDouble("MAP_TIEPOINT_ONE", 0));
                        i.putExtra("MAP_LONGITUDE", extras.getDouble("MAP_TIEPOINT_TWO", 0));
                        //No results needed, just start activity
                        startActivity(i);
                    }

                }
            });

            if (extras.getDouble("MAP_TIEPOINT_ONE",0)==0 || extras.getDouble("MAP_TIEPOINT_TWO")==0){

                //If the map has no coordinates, user can't go the that map
                btn_goto.setText(R.string.map_button_text_no_coord);
                btn_goto.setEnabled(false);
                btn_goto.setAlpha(0.7f);

            }
        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_delete, menu);

        return true;
    }

    public Bitmap loadImageFromStorage(String path, String fileName)
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

    //Options to select map-type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.delete:
                //Deleteobject from database
                Toast.makeText(this, R.string.Map_deleted, Toast.LENGTH_SHORT).show();
                handler.deleteMap(getIntent().getIntExtra("MAP_ID",0));
                finish();
             break;
        }

        return super.onOptionsItemSelected(item);
    }


}
