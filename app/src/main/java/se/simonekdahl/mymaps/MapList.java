package se.simonekdahl.mymaps;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;
import se.simonekdahl.mymaps.dao.MapObject;
import se.simonekdahl.mymaps.dao.MapObjectDao;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.List;



public class MapList extends ParentActivity {

    private static final String TAG = "MapList";
    List<MapObject> mapObjects;
    ListView lv;

    private MapObjectDao mapObjectDao;

    private MapObjectDao getMapObjectDao(){
        if(mapObjectDao == null){
            mapObjectDao = getDaoSession().getMapObjectDao();
        }

        return mapObjectDao;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        Toolbar toolbar = findViewById(R.id.toolbar_map_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FloatingActionButton fab = findViewById(R.id.fab);

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

        lv = findViewById(R.id.lv_maps_list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MapList.this, MapDetails.class);
                intent.putExtra("MAP", mapObjects.get(position));
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

        mapObjects = getMapObjectDao().loadAll();

        // Initialize Custom Adapter
        CustomAdapter adapter = new CustomAdapter(this, mapObjects);

        // Set Adapter to ListView
        lv.setAdapter(adapter);


        for(MapObject m : mapObjects){
            String record = "ID=" + m.getId() + " | Name=" + m.getName() + " | " + m.getDescription() +
                    " Filepath = " + m.getFilePath() + " Filnamet är " + m.getBitmapName();

            Log.d(TAG, "RECORDDATA =__________ : " + record);
        }

    }



}
