package se.simonekdahl.mymaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;



public class MapList extends ParentActivity {

    private static final String TAG = "MapList";
    private DBHandler handler;
    List<MapObject> mapObjects;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_map_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Intent i = new Intent(MainActivity.this, GroundOverlayActivity.class);
                Intent i = new Intent(MapList.this, ChoseFileActivity.class);
                startActivityForResult(i, 1);

                    /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();*/
            }
        });

        handler = new DBHandler(this);

        lv = (ListView) findViewById(R.id.lv_maps_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MapList.this, MapDetails.class);
                intent.putExtra("MAP_ID", mapObjects.get(position).get_id());
                intent.putExtra("MAP_NAME", mapObjects.get(position).get_mapName());
                intent.putExtra("MAP_DESC", mapObjects.get(position).get_mapDescription());
                intent.putExtra("MAP_IMAGE_NAME", mapObjects.get(position).get_bitmapName());
                intent.putExtra("MAP_IMAGE_FILEPATH", mapObjects.get(position).get_filePath());
                intent.putExtra("MAP_TIEPOINT_ONE", mapObjects.get(position).get_tiePointOne());
                intent.putExtra("MAP_TIEPOINT_TWO", mapObjects.get(position).get_tiePointTwo());
                intent.putExtra("MAP_ROTATION", mapObjects.get(position).get_Rotation());
                intent.putExtra("MAP_SIZE", mapObjects.get(position).get_Size());
                startActivity(intent);
            }

        });


                loadContactData();



    }

    @Override
    protected void onResume() {
        super.onResume();
        loadContactData();
    }

    private void loadContactData(){
        // Code for loading contact list in ListView
        // Reading all contacts

        mapObjects = handler.readAllMaps();

        // Initialize Custom Adapter
        CustomAdapter adapter = new CustomAdapter(this, mapObjects);

        // Set Adapter to ListView
        lv.setAdapter(adapter);


        for(MapObject m : mapObjects){
            String record = "ID=" + m.get_id() + " | Name=" + m.get_mapName() + " | " + m.get_mapDescription() +
                    " Filepath = " + m.get_filePath() + " Filnamet är " + m.get_bitmapName();

            Log.d(TAG, "RECORDDATA =__________ : " + record);
        }

    }



}
