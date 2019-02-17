package se.simonekdahl.mymaps;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import se.simonekdahl.mymaps.dao.MapObject;

import static androidx.core.content.ContextCompat.startActivity;


/**
 * Created by Simon on 16-03-23.
 * class to create listobjects from maps in librairy
 */
public class CustomAdapter extends BaseAdapter {

    private final MapObjectListItemView.OnCheckedChangedListener onCheckedChangedListener;
    private List<MapObject> items;
    private Context context;
    private LayoutInflater inflater;

    public CustomAdapter(Context _context, List<MapObject> _items, MapObjectListItemView.OnCheckedChangedListener onCheckedChangedListener){
        inflater = LayoutInflater.from(_context);
        this.items = _items;
        this.context = _context;
        this.onCheckedChangedListener = onCheckedChangedListener;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        MapObject map = items.get(position);

        if(convertView == null)
            convertView = new MapObjectListItemView(context);


        MapObjectListItemView itemView = (MapObjectListItemView) convertView;
        itemView.setMapObjectListItemView(map);


        itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MapDetails.class);
            intent.putExtra("MAP", map);
            startActivity(context, intent, null);
        });


        itemView.setOnCheckedChangedListener(count -> {
            final ListView listView = (ListView)parent;
            int sum = 0;

            ArrayList<MapObject> checkedItems = new ArrayList<>();

            for(int i = 0; i < listView.getChildCount(); i++){
                MapObjectListItemView v = (MapObjectListItemView) listView.getChildAt(i);
                if(v.isChecked()) checkedItems.add(v.getMapObject());
            }

            onCheckedChangedListener.onCheckedChanged(checkedItems);
        });





        return convertView;
    }

    //Function to make all mapimages square
    public static Bitmap cropCenter(Bitmap bmp) {

            int dimension = Math.min(bmp.getWidth(), bmp.getHeight());
            return ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
    }

}
