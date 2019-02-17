package se.simonekdahl.mymaps.bottomsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import se.simonekdahl.mymaps.App;
import se.simonekdahl.mymaps.MarkerViewModel;
import se.simonekdahl.mymaps.R;
import se.simonekdahl.mymaps.dao.MarkerObject;

public class MapObjectBottomSheetDialogFragment extends BottomSheetDialogFragment {


    EditText title;
    TextView textView1;
    TextView textView2;
    Button saveButton;

    MarkerViewModel model;

    Marker marker;
    private OnMarkerSaveListener listener;

    public static MapObjectBottomSheetDialogFragment newInstance(MarkerObject mo) {
        MapObjectBottomSheetDialogFragment fragment = new MapObjectBottomSheetDialogFragment();

        Bundle args = new Bundle();

        args.putDouble("LAT", mo.getLatitude());
        args.putDouble("LONG", mo.getLongitude());
        args.putString("TITLE", mo.getName());

        fragment.setArguments(args);

        return fragment;
    }

    public abstract interface OnMarkerSaveListener {
        public void onMarkerSave();
    }

    public static MapObjectBottomSheetDialogFragment newInstance(Marker marker) {

        MapObjectBottomSheetDialogFragment fragment = new MapObjectBottomSheetDialogFragment();

        Bundle args = new Bundle();

        args.putDouble("LAT", marker.getPosition().latitude);
        args.putDouble("LONG", marker.getPosition().longitude);
        args.putString("TITLE", "");

        fragment.setArguments(args);

        return fragment;
    }

    public void setOnSaveListener(OnMarkerSaveListener listener){
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       // setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);

    }



    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.marker_bottom_sheet, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent())
                .getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = container;

        if(view == null)
            view = inflater.inflate(R.layout.marker_bottom_sheet, container, false);

        title = view.findViewById(R.id.marker_title);
        textView1 = view.findViewById(R.id.marker_lat);
        textView2 = view.findViewById(R.id.marker_long);
        saveButton = view.findViewById(R.id.save_marker_button);

        double lat = getArguments().getDouble("LAT");
        double longitude = getArguments().getDouble("LONG");
        String titleText = getArguments().getString("TITLE");

        title.setText(titleText);
        textView1.setText(String.valueOf(lat));
        textView2.setText(String.valueOf(longitude));


        saveButton.setOnClickListener(v -> {

            MarkerObject m = new MarkerObject();
            m.setName(title.getText().toString());
            m.setLatitude(lat);
            m.setLongitude(longitude);

            ((App)getActivity().getApplication()).getDaoSession().getMarkerObjectDao().save(m);
            listener.onMarkerSave();
        });



        return view;
    }

}
