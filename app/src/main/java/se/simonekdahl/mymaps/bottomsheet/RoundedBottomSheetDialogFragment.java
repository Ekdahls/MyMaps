package se.simonekdahl.mymaps.bottomsheet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.maps.model.Marker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import se.simonekdahl.mymaps.MarkerViewModel;
import se.simonekdahl.mymaps.R;
import se.simonekdahl.mymaps.dao.MarkerObject;

public class RoundedBottomSheetDialogFragment extends BottomSheetDialogFragment {


    @Override
    public int getTheme() {
        return R.style.BottomSheetDialogTheme;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new BottomSheetDialog(requireContext(), getTheme());
    }



    private EditText title;
    private TextView textView1;
    private TextView textView2;
    private Button saveButton;
    private Button deleteButton;

    public static RoundedBottomSheetDialogFragment newInstance(MarkerObject mo) {
        RoundedBottomSheetDialogFragment fragment = new RoundedBottomSheetDialogFragment();

        Bundle args = new Bundle();

        args.putLong("ID", mo.getId());
        args.putDouble("LAT", mo.getLatitude());
        args.putDouble("LONG", mo.getLongitude());
        args.putString("TITLE", mo.getName());

        fragment.setArguments(args);

        return fragment;
    }

    public static RoundedBottomSheetDialogFragment newInstance(Marker marker) {

        RoundedBottomSheetDialogFragment fragment = new RoundedBottomSheetDialogFragment();

        Bundle args = new Bundle();

        args.putLong("ID", -1);
        args.putDouble("LAT", marker.getPosition().latitude);
        args.putDouble("LONG", marker.getPosition().longitude);
        args.putString("TITLE", "");

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

        title = view.findViewById(R.id.marker_title);
        textView1 = view.findViewById(R.id.marker_lat);
        textView2 = view.findViewById(R.id.marker_long);
        saveButton = view.findViewById(R.id.save_marker_button);
        deleteButton = view.findViewById(R.id.delete_marker_button);

        assert getArguments() != null;

        long id = getArguments().getLong("ID");
        double lat = getArguments().getDouble("LAT");
        double longitude = getArguments().getDouble("LONG");
        String titleText = getArguments().getString("TITLE");

        if(id == -1){
            deleteButton.setVisibility(View.GONE);
        }

        title.setText(titleText);
        textView1.setText(String.valueOf(lat));
        textView2.setText(String.valueOf(longitude));

        MarkerViewModel model = ViewModelProviders.of(getActivity()).get(MarkerViewModel.class);


        saveButton.setOnClickListener(v -> {

            MarkerObject m = new MarkerObject();
            m.setName(title.getText().toString());
            m.setLatitude(lat);
            m.setLongitude(longitude);

            model.addMarkerObject(m);
            this.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            model.deleteMarkerObject(id);
            this.dismiss();
        });



        return view;
    }




}
