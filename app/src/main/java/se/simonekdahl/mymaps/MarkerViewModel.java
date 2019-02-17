package se.simonekdahl.mymaps;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import se.simonekdahl.mymaps.dao.DaoSession;
import se.simonekdahl.mymaps.dao.MarkerObject;

public class MarkerViewModel extends AndroidViewModel {

    private String TAG = MarkerViewModel.class.getSimpleName();

    private MutableLiveData<List<MarkerObject>> markerObjectList;

    public MarkerViewModel(@NonNull Application application) {
        super(application);
    }


    LiveData<List<MarkerObject>> getMarkerObjectList() {
        if(markerObjectList == null){
            markerObjectList = new MutableLiveData<>();
            loadMarkerObjects();
        }

        return markerObjectList;
    }

    private void loadMarkerObjects() {

        Handler myHandler = new Handler();

        myHandler.post(() -> {

            DaoSession daoSession = ((App)getApplication()).getDaoSession();
            List<MarkerObject> markerObjectList1 = daoSession.getMarkerObjectDao().queryBuilder().list();

            Collections.sort(markerObjectList1);

            markerObjectList.setValue(markerObjectList1);

        });

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "on cleared called");
    }


    public void getLatest() {
        loadMarkerObjects();
    }

    public MarkerObject getMarkerObjectFromMarker(Marker marker) {
        for(MarkerObject mo : Objects.requireNonNull(markerObjectList.getValue())){

            if(mo.getLatitude() == marker.getPosition().latitude && mo.getLongitude() == marker.getPosition().longitude){
                return mo;
            }

        }

        return null;
    }
}
