package net.info420.clonemusic;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import java.nio.ByteOrder;

public class GestionnaireBD {

    private static final String TAG = "gestionnaire";

    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public static final String DB_NAME = "clonemusic.db";
    public static final int DB_VERSION = 1;

    //Table chansons
    public static final String TABLE_CHANSONS = "chansons";
    /*public static final String ID_CHANSONS = "c_id"; //Clé primaire*/
    public static final String ID_CHANSONS = BaseColumns._ID; //Clé primaire
    public static final String TITRE_CHANSONS = "c_titre";
    public static final String LIEN_CHANSONS = "c_lien";

    //Table playlists
    public static final String TABLE_PLAYLISTS = "playlists";
    public static final String ID_PLAYLISTS = "p_id"; //Clé primaire
    public static final String TITRE_PLAYSLIST = "p_titre";

    //Table lien
    public static final String TABLE_LIEN = "lien";
    public static final String ID_LIEN_CHANSON = "l_c_id";
    public static final String ID_LIEN_PLAYLIST = "l_p_id";
    public static final String ORDRE_CHANSON = "l_o";

    public GestionnaireBD(Context context)
    {
        this.context = context;
        dbHelper = new DBHelper();
    }

    public Cursor queryChansons(){
        Cursor cursor;
        db = dbHelper.getReadableDatabase();

        cursor = db.query(TABLE_CHANSONS, null, null, null, null, null, ID_CHANSONS);
        return cursor;
    }

    public void resetChansons()
    {
        db = dbHelper.getReadableDatabase();
        db.execSQL("drop table if exists " + TABLE_CHANSONS);

        String sql;

        //Création de la table chansons
        sql = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s text, %s int)", TABLE_CHANSONS, ID_CHANSONS, TITRE_CHANSONS, LIEN_CHANSONS);
        db.execSQL(sql);
    }

    public int queryChansons(int position){
        Cursor cursor;
        db = dbHelper.getReadableDatabase();

        position ++;

        String sql = String.format("Select %s from %s where %s = %s", LIEN_CHANSONS, TABLE_CHANSONS, ID_CHANSONS, position);


        cursor = db.rawQuery(sql, null);
        //int allo = cursor.getInt(0);
        cursor.moveToFirst();
        int allo = cursor.getInt(cursor.getColumnIndexOrThrow(LIEN_CHANSONS));
        return allo;
    }

    public long insertChanson(Chanson chanson)
    {
        long insertResult;

        ContentValues fieldsValues = new ContentValues();
        db = dbHelper.getWritableDatabase();

        fieldsValues.put(TITRE_CHANSONS, chanson.getTitre());
        fieldsValues.put(LIEN_CHANSONS, chanson.getID());

        insertResult = db.insertWithOnConflict(TABLE_CHANSONS, null, fieldsValues, SQLiteDatabase.CONFLICT_IGNORE);

        if(insertResult != -1)
        {
            Log.d(TAG, String.format("Chanson \"%s\" ID :  %s insérée dans la BD", chanson.getTitre(),chanson.getID()));
        }
        return insertResult;
    }

    private class DBHelper extends SQLiteOpenHelper
    {

        private static final String TAG = "DBHelper";

        public DBHelper() {
            super(context, DB_NAME, null, DB_VERSION);
            //null on utilise le curseur standard. L'équivalent d'un select ben straight
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Exécutée une seule fois. Crée la base de donneés et toutes les tables
            String sql;

            //Création de la table chansons
            sql = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s text, %s int)", TABLE_CHANSONS, ID_CHANSONS, TITRE_CHANSONS, LIEN_CHANSONS);
            db.execSQL(sql);

            //Création de la table playlists
            sql = String.format("create table %s (%s int primary key, %s text)", TABLE_PLAYLISTS, ID_PLAYLISTS, TITRE_PLAYSLIST);
            db.execSQL(sql);


/*            sql = String.format("create table %s (%s int, %s int, %s int, primary key (%s, %s, %s), foreign key (%s) references %s(%s), foreign key (%s) references %s(%s))",
                    TABLE_LIEN,  //Table
                    ID_LIEN_CHANSON, //Champ 1
                    ID_LIEN_PLAYLIST, //Champ 2
                    ORDRE_CHANSON, //Champ 3

                    //Primary key
                    ID_LIEN_CHANSON,
                    ID_LIEN_PLAYLIST,
                    ORDRE_CHANSON,

                    //Foreign key 1
                    ID_CHANSONS,
                    TABLE_CHANSONS,
                    ID_CHANSONS,

                    //Foreign key 2
                    ID_PLAYLISTS,
                    TABLE_PLAYLISTS,
                    ID_PLAYLISTS
                    );
            db.execSQL(sql);*/
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            //Drop toutes les tables
            db.execSQL("drop table if exists " + TABLE_CHANSONS);
            db.execSQL("drop table if exists " + TABLE_LIEN);
            db.execSQL("drop table if exists " + TABLE_PLAYLISTS);

            onCreate(db);
            Log.d(TAG, "onUpgrade() : Mise à jour de la version de la BD. (V" + oldVersion + " --> V" + newVersion + ")");

        }
    }


}
