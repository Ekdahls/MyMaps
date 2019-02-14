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
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import se.simonekdahl.mymaps.R;

public class MapObjectBottomSheetDialogFragment extends BottomSheetDialogFragment {



    TextView textView1;
    TextView textView2;
    Button saveButton;

    Marker marker;

    public static MapObjectBottomSheetDialogFragment newInstance(Marker marker) {

        MapObjectBottomSheetDialogFragment fragment = new MapObjectBottomSheetDialogFragment();

        Bundle args = new Bundle();

        args.putDouble("LAT", marker.getPosition().latitude);
        args.putDouble("LONG", marker.getPosition().longitude);

        fragment.setArguments(args);

        return fragment;
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


        textView1 = view.findViewById(R.id.marker_lat);
        textView2 = view.findViewById(R.id.marker_long);
        saveButton = view.findViewById(R.id.save_marker_button);

        double lat = getArguments().getDouble("LAT");
        double longitude = getArguments().getDouble("LONG");

        textView1.setText(String.valueOf(lat));
        textView2.setText(String.valueOf(longitude));



        return view;
    }

}
