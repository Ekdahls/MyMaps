package se.simonekdahl.mymaps;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatTextView;
import se.simonekdahl.mymaps.dao.MapObject;

import static se.simonekdahl.mymaps.utils.MapImageUtils.getImageFile;

public class MapObjectListItemView extends RelativeLayout implements Checkable {

    private View view;

    private ImageView listImage;
    private AppCompatTextView mapName;
    private AppCompatTextView mapDescription;

    private CheckBox checkBox;
    private OnCheckedChangedListener onCheckedChangedListener;
    private MapObject mapObject;

    public void setOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.onCheckedChangedListener = onCheckedChangedListener;
    }

    public MapObject getMapObject() {
        return this.mapObject;
    }


    public abstract interface OnCheckedChangedListener {
        public void onCheckedChanged(ArrayList<MapObject> count);
    }


    public MapObjectListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MapObjectListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MapObjectListItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public MapObjectListItemView(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        view = inflater.inflate(R.layout.map_item, this, true);

        listImage = view.findViewById(R.id.list_image);
        mapName = view.findViewById(R.id.tv_map_name);
        mapDescription = view.findViewById(R.id.tv_map_desc);

        checkBox = view.findViewById(R.id.map_list_checkbox);

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));

    }

    public void setMapObjectListItemView(MapObject mapObject){

        this.mapObject = mapObject;

        Picasso.get()
                .load(getImageFile(mapObject.getFilePath(), mapObject.getBitmapName()))
                .fit()
                .into(listImage);

        //listImage.setImageBitmap(mapImageThumbnail);
        mapName.setText(mapObject.getName());
        mapDescription.setText(mapObject.getDescription());
    }


    @Override
    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
        this.onCheckedChangedListener.onCheckedChanged(null);
    }

    @Override
    public boolean isChecked() {
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        checkBox.toggle();
        this.onCheckedChangedListener.onCheckedChanged(null);
    }
}
