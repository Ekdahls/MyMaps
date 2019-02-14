package se.simonekdahl.mymaps;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
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

    MapObject map;
    EditText mMapname;
    EditText mMapDesc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_details);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        handler = new DBHandler(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Bundle extras = getIntent().getExtras();


        map = extras.getParcelable("MAP");

        Log.d(TAG, "onCreate: extras == ");

        ImageView mMapimage = (ImageView) findViewById(R.id.map_image_view_in_details);

        if (extras.getString("MAP_IMAGE_FILEPATH") != null &&
                extras.getString("MAP_IMAGE_NAME") != null) {
            mMapimage.setImageBitmap(loadImageFromStorage(extras.getString("MAP_IMAGE_FILEPATH")
                    , extras.getString("MAP_IMAGE_NAME")));
        }
        mMapname = (EditText) findViewById(R.id.textView_insert_mapname);

        if (extras.getString("MAP_NAME") != null) {
            mMapname.setText(extras.getString("MAP_NAME"));
        }

        mMapDesc = (EditText) findViewById(R.id.textView_insert_desc);

        if (extras.getString("MAP_DESC") != null) {
            mMapDesc.setText(extras.getString("MAP_DESC"));
        }
        TextView mOther = (TextView) findViewById(R.id.textView_otherinfo_insert);

        int id = extras.getInt("MAP_ID", 0);
        mOther.setText(String.valueOf(id));

        Button btn_goto = (Button) findViewById(R.id.button_go_to_map);
        assert btn_goto != null;
        btn_goto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (extras.getDouble("MAP_TIEPOINT_ONE", 0) > 0 && extras.getDouble("MAP_TIEPOINT_TWO") > 0) {
                    Intent i = new Intent(MapDetails.this, MainActivity.class);

                    i.putExtra("MAP_LATITUDE", extras.getDouble("MAP_TIEPOINT_ONE", 0));
                    i.putExtra("MAP_LONGITUDE", extras.getDouble("MAP_TIEPOINT_TWO", 0));
                    //No results needed, just start activity
                    startActivity(i);
                }

            }
        });

        Button deleteMapButton = (Button) findViewById(R.id.btn_delete_map_object);
        assert deleteMapButton != null;
        deleteMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(MapDetails.this)
                        .setTitle("Title")
                        .setMessage("Do you really want to delete the map?")
                        .setIcon(android.R.drawable.ic_delete)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(getBaseContext(), R.string.Map_deleted, Toast.LENGTH_SHORT).show();
                                handler.deleteMap(map);
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

            }
        });


        if (extras.getDouble("MAP_TIEPOINT_ONE", 0) == 0 || extras.getDouble("MAP_TIEPOINT_TWO") == 0) {

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
        inflater.inflate(R.menu.menu_save, menu);

        return true;
    }

    public Bitmap loadImageFromStorage(String path, String fileName) {
        Bitmap b = null;

        try {
            File f = new File(path, fileName + ".png");
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return b;
    }

    /*
        //Deleteobject from database

     */

    //Options to select map-type
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.save:
                map.set_mapName(mMapname.getText().toString());
                map.set_mapDescription((mMapDesc.getText().toString()));
                handler.updateMap(map);
                Toast.makeText(getBaseContext(), R.string.saved_map, Toast.LENGTH_SHORT).show();
                break;
            case R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


}
