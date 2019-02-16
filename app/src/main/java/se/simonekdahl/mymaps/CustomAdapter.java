package se.simonekdahl.mymaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import se.simonekdahl.mymaps.dao.MapObject;


/**
 * Created by Simon on 16-03-23.
 * class to create listobjects from maps in librairy
 */
public class CustomAdapter extends BaseAdapter {

    private List<MapObject> items;
    private Context context;
    private LayoutInflater inflater;

    public CustomAdapter(Context _context, List<MapObject> _items){
        inflater = LayoutInflater.from(_context);
        this.items = _items;
        this.context = _context;

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
        View view = convertView;

        if(view == null)
            view = inflater.inflate(R.layout.map_item, null);

        TextView name = view.findViewById(R.id.tv_map_name);
        TextView description = view.findViewById(R.id.tv_map_desc);
        ImageView photo = view.findViewById(R.id.list_image);
        Bitmap mapImage = loadImageFromStorage(map.getFilePath(), map.getId());

        if(mapImage!=null) {
            mapImage = cropCenter(mapImage);
        }

        name.setText(map.getName());
        description.setText(map.getDescription());
        photo.setImageBitmap(mapImage);

        return view;
    }

    //Function to make all mapimages square
    public static Bitmap cropCenter(Bitmap bmp) {

            int dimension = Math.min(bmp.getWidth(), bmp.getHeight());
            return ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
    }


    //Function to load all mapimages from internal storage
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
