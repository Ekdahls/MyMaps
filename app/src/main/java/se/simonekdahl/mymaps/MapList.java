package se.simonekdahl.mymaps;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import se.simonekdahl.mymaps.dao.MapObject;
import se.simonekdahl.mymaps.dao.MapObjectDao;



public class MapList extends ParentActivity {

    private static final String TAG = "MapList";
    List<MapObject> mapObjects;
    ListView lv;

    private MapObjectDao mapObjectDao;

    private RecyclerView mRecyclerView;

    public final int offset = 30;
    private int page = 0;
    private boolean loadingMore = false;

    MapListViewModel model;

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

        model = ViewModelProviders.of(this).get(MapListViewModel.class);
        model.getMapObjectList().observe(this, mapObjectList -> {

            CustomAdapter adapter = new CustomAdapter(this, mapObjectList);
            lv.setAdapter(adapter);

        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        model.getLatest();
        //loadContactData();
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
