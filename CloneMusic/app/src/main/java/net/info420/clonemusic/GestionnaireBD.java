package net.info420.clonemusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class GestionnaireBD {
/*
    //Tag pour débogage
    public static final String TAG = "gestionnaire";

    public static final String C_ID = BaseColumns._ID;

    public static final String C_CREATED_AT = "c_createdAt";
    public static final String C_USER = "c_user";
    public static final String C_TEXT = "c_text";

    Context context;
    DBHelper dbHelper;
    SQLiteDatabase db;

    public static final String DB_NAME = "timeline.db";
    public static final String TABLE_NAME = "statuses";
    public static final int DB_VERSION = 2;

    public StatusData(Context context)
    {
        this.context = context;
        dbHelper = new DBHelper();
    }

    public long insert(Status status)
    {
        long insertResult;

        ContentValues fieldsValues = new ContentValues();
        db = dbHelper.getWritableDatabase();

        fieldsValues.put(C_ID, status.id.longValue());
        fieldsValues.put(C_CREATED_AT, status.createdAt.getTime());
        fieldsValues.put(C_USER, status.user.name);
        fieldsValues.put(C_TEXT, status.text);

        //db.insert(TABLE_NAME, null, fieldsValues);
        insertResult = db.insertWithOnConflict(TABLE_NAME, null, fieldsValues, SQLiteDatabase.CONFLICT_IGNORE);
        //Évite l'injection SQL. La commande se bâtit automatiquement et ne prend pas en compte le texte dans le status

        if(insertResult != -1)
        {
            Log.d(TAG, String.format("inser(): Insertion du tweet \"%s: %s\" dans la BD", status.user.name, status.text));
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
            //Exécutée une seule fois

            String sql;

            sql = String.format("create table %s (%s int primary key, %s int, %s text, %s text)",
                    TABLE_NAME, C_ID, C_CREATED_AT, C_USER, C_TEXT);
            db.execSQL(sql);
            Log.d(TAG, "onCreate(): Commande SQL \"" + sql + "\"");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
            Log.d(TAG, "onUpgrade() : Mise à jour de la version de la BD. (V" + oldVersion + " --> V" + newVersion + ")");

        }
    }


    public Cursor query()
    {
        Cursor cursor;
        db = dbHelper.getReadableDatabase();
        cursor = db.query(TABLE_NAME, null, null, null, null, null, C_CREATED_AT + " DESC");
        return cursor; //Le curseur retourné pointe avant la première entrée
    }*/

}

