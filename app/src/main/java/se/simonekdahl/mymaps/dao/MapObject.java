package se.simonekdahl.mymaps.dao;

import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity(
        // Flag to make an entity "active": Active entities have update,
        // delete, and refresh methods.
        active = true,
        // Whether an all properties constructor should be generated.
        // A no-args constructor is always required.
        generateConstructors = true,

        // Whether getters and setters for properties should be generated if missing.
        generateGettersSetters = true
)
public class MapObject implements Parcelable {


    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String name;

    private String bitmapName;
    private String filePath;
    private double tiePointOne;
    private double tiePointTwo;
    private double rotation;
    private double size;


    private String description;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 48110761)
    private transient MapObjectDao myDao;


    @Generated(hash = 755981821)
    public MapObject(Long id, @NotNull String name, String bitmapName, String filePath,
                     double tiePointOne, double tiePointTwo, double rotation, double size, String description) {
        this.id = id;
        this.name = name;
        this.bitmapName = bitmapName;
        this.filePath = filePath;
        this.tiePointOne = tiePointOne;
        this.tiePointTwo = tiePointTwo;
        this.rotation = rotation;
        this.size = size;
        this.description = description;
    }


    @Generated(hash = 705302497)
    public MapObject() {
    }


    public Long getId() {
        return this.id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return this.name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public String getBitmapName() {
        return this.bitmapName;
    }


    public void setBitmapName(String bitmapName) {
        this.bitmapName = bitmapName;
    }


    public String getFilePath() {
        return this.filePath;
    }


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public double getTiePointOne() {
        return this.tiePointOne;
    }


    public void setTiePointOne(double tiePointOne) {
        this.tiePointOne = tiePointOne;
    }


    public double getTiePointTwo() {
        return this.tiePointTwo;
    }


    public void setTiePointTwo(double tiePointTwo) {
        this.tiePointTwo = tiePointTwo;
    }


    public double getRotation() {
        return this.rotation;
    }


    public void setRotation(double rotation) {
        this.rotation = rotation;
    }


    public double getSize() {
        return this.size;
    }


    public void setSize(double size) {
        this.size = size;
    }


    public String getDescription() {
        return this.description;
    }


    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }


    protected MapObject(Parcel in) {
        id = in.readLong();
        name = in.readString();
        bitmapName = in.readString();
        filePath = in.readString();
        tiePointOne = in.readDouble();
        tiePointTwo = in.readDouble();
        rotation = in.readDouble();
        size = in.readDouble();
        description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(bitmapName);
        dest.writeString(filePath);
        dest.writeDouble(tiePointOne);
        dest.writeDouble(tiePointTwo);
        dest.writeDouble(rotation);
        dest.writeDouble(size);
        dest.writeString(description);
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 788755102)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getMapObjectDao() : null;
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MapObject> CREATOR = new Parcelable.Creator<MapObject>() {
        @Override
        public MapObject createFromParcel(Parcel in) {
            return new MapObject(in);
        }

        @Override
        public MapObject[] newArray(int size) {
            return new MapObject[size];
        }
    };
}