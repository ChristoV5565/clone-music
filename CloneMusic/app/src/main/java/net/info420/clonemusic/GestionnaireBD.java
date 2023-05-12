package net.info420.clonemusic;

/*
*
* Classe GestionnaireBD
*
* Utilisé pour gérer l'accès à la base de données pour les autres classes.
* Permet d'insérer des chansons, de les compter et de les récupérer
*
* */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class GestionnaireBD {
    //Tag pour le debugging
    private static final String TAG = "gestionnaire";
    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public static final String DB_NAME = "clonemusic.db";
    public static final int DB_VERSION = 1;

    //Table chansons
    public static final String TABLE_CHANSONS = "chansons";
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

    //Retourne un curseur qui pointe sur toutes les chansons de la base de données
    public Cursor queryChansons(){
        Cursor cursor;
        db = dbHelper.getReadableDatabase();

        cursor = db.query(TABLE_CHANSONS, null, null, null, null, null, ID_CHANSONS);
        return cursor;
    }

    //Retourne un curseur qui pointe sur toutes les chansons dont les identifiants se retrouvent dans la liste en paramètre
    public Cursor queryChansons(String[] id_chansons)
    {
        Cursor cursor;
        String sqlWhere = "";
        db = dbHelper.getReadableDatabase();


        for(int compteur = 0 ; compteur < id_chansons.length; compteur ++)
        {
            if(compteur == 0)
            {
                sqlWhere = ID_CHANSONS + " = " + id_chansons[compteur];
            }
            else
            {
                sqlWhere = sqlWhere + " or " + ID_CHANSONS + " = " + id_chansons[compteur];
            }
        }

        String sql = String.format("select * from %s where %s", TABLE_CHANSONS, sqlWhere);

        Log.d("CreationPt1", sql);

        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor;
    }

    //Retourne l'identifiant machine d'une chanson à la position donnée dans la liste
    public int queryChansons(int position){
        Cursor cursor;
        db = dbHelper.getReadableDatabase();

        Log.d("LecteurMusique", "Tente de jouer chanson position : " + position);

        //Compense parce que les index dans SQLite commencent à 1 au lieu de 0
        position ++;

        String sql = String.format("Select %s from %s where %s = %s", LIEN_CHANSONS, TABLE_CHANSONS, ID_CHANSONS, position);
        cursor = db.rawQuery(sql, null);

        cursor.moveToFirst();
        int indentifiantChanson = cursor.getInt(cursor.getColumnIndexOrThrow(LIEN_CHANSONS));
        return indentifiantChanson;
    }

    //Permet de supprimer la table chansons. Prépare la base de données à un nouveau scan
    public void resetChansons()
    {
        db = dbHelper.getReadableDatabase();
        db.execSQL("drop table if exists " + TABLE_CHANSONS);

        String sql;

        //Création de la table chansons
        sql = String.format("create table %s (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s text, %s int)", TABLE_CHANSONS, ID_CHANSONS, TITRE_CHANSONS, LIEN_CHANSONS);
        db.execSQL(sql);
    }

    //Retourne le nombre total de chansons dans la base de données
    public int queryNombreChansons()
    {
        Cursor cursor;
        db = dbHelper.getReadableDatabase();
        String sql = String.format("Select * from %s", TABLE_CHANSONS);
        cursor = db.rawQuery(sql, null);
        cursor.moveToFirst();
        return cursor.getCount();
    }

    //Permet d'insérer une chanson dans la table chansons
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

    //Helper pour l'accès à la base de données
    private class DBHelper extends SQLiteOpenHelper
    {
        private static final String TAG = "DBHelper";

        public DBHelper() {
            super(context, DB_NAME, null, DB_VERSION);
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
