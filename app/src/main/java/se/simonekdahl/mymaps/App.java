package se.simonekdahl.mymaps;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.libraries.places.api.Places;

import org.greenrobot.greendao.database.Database;

import java.util.Locale;

import se.simonekdahl.mymaps.dao.DaoMaster;
import se.simonekdahl.mymaps.dao.DaoSession;

public class App extends Application {

    private static App singleton;

    private DaoSession daoSession;
    private Database db;
    private SQLiteDatabase sqlDatabase;

    public Application getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;


        // regular SQLite database
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db");
        db = helper.getWritableDb();
        sqlDatabase = helper.getWritableDatabase();

        // encrypted SQLCipher database
        // note: you need to add SQLCipher to your dependencies, check the build.gradle file
        // DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "notes-db-encrypted");
        // Database db = helper.getEncryptedWritableDb("encryption-key");

        daoSession = new DaoMaster(db).newSession();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.PLACES_API_KEY));
        }

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public Database getDb(){
        return db;
    }

    public SQLiteDatabase getSqlDatabase(){
        return sqlDatabase;
    }

}
