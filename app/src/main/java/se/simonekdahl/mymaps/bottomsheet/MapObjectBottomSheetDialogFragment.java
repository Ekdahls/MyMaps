package se.simonekdahl.mymaps.bottomsheet;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.simonekdahl.mymaps.R;

public class MapObjectBottomSheetDialogFragment extends BottomSheetDialogFragment {


    public static MapObjectBottomSheetDialogFragment newInstance() {
        return new MapObjectBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {



        View view = container;

        if(view == null)
            view = inflater.inflate(R.layout.marker_bottom_sheet, container, false);



        return view;
    }



}
