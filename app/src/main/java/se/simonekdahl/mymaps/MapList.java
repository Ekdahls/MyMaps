package se.simonekdahl.mymaps;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import se.simonekdahl.mymaps.dao.MapObject;

public class MapList extends ParentActivity {

    ListView lv;
    MapListViewModel model;
    Menu menu;
    private ArrayList<MapObject> checkedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_list);

        Toolbar toolbar = findViewById(R.id.toolbar_map_list);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        lv = findViewById(R.id.lv_maps_list);

        lv.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

        model = ViewModelProviders.of(this).get(MapListViewModel.class);
        model.getMapObjectList().observe(this, mapObjectList -> {

            CustomAdapter adapter = new CustomAdapter(this, mapObjectList, (mapObjects) -> {
                MenuItem deleteItem = menu.findItem(R.id.delete);
                deleteItem.setVisible(mapObjects.size() > 0);
                this.checkedItems = mapObjects;
            });
            lv.setAdapter(adapter);

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        model.getLatest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map_list, menu);

        this.menu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.delete){
            ((App)getApplication()).getDaoSession().getMapObjectDao().deleteInTx(checkedItems);
            model.getLatest();
        }

        return super.onOptionsItemSelected(item);
    }
}
