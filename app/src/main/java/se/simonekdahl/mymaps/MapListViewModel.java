package se.simonekdahl.mymaps;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import se.simonekdahl.mymaps.dao.DaoMaster;
import se.simonekdahl.mymaps.dao.DaoSession;
import se.simonekdahl.mymaps.dao.MapObject;

public class MapListViewModel extends AndroidViewModel {


    private String TAG = MapListViewModel.class.getSimpleName();

    private MutableLiveData<List<MapObject>> mapObjectList;

    public MapListViewModel(@NonNull Application application) {
        super(application);
    }

    LiveData<List<MapObject>> getMapObjectList() {
        if(mapObjectList == null){
            mapObjectList = new MutableLiveData<>();
            loadMapObjects();
        }

        return mapObjectList;
    }

    private void loadMapObjects() {

        Handler myHandler = new Handler();

        myHandler.post(() -> {

            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(getApplication(), "notes-db");
            Database db = helper.getWritableDb();
            DaoSession daoSession = new DaoMaster(db).newSession();
            List<MapObject> mapObjectList1 = daoSession.getMapObjectDao().queryBuilder().list();

            Collections.sort(mapObjectList1);

            mapObjectList.setValue(mapObjectList1);

        });

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "on cleared called");
    }


    public void getLatest() {
        loadMapObjects();
    }
}
