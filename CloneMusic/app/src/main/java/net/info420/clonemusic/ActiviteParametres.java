package net.info420.clonemusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActiviteParametres extends AppCompatActivity implements Preference.OnPreferenceClickListener {

    Intent intentMain;
    PreferenceFragmentClone preferenceFragmentClone;
    static Preference scannerMusique;
    public static final String TAG = "Parametres";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parametres);

        //Liaison les intent aux activités
        intentMain = new Intent(this, ActivitePrincipale.class);

        //Générer toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Clone Music");
        toolbar.setTitleTextColor(Color.WHITE);

        preferenceFragmentClone = new PreferenceFragmentClone();

        getFragmentManager().beginTransaction().add(R.id.fragmentParametres, preferenceFragmentClone).commit();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        scannerMusique.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {


        /*
        *
        * Étapes :
        *
        * Drop
        * Scan
        *
        */

        GestionnaireBD gestionnaireBD = new GestionnaireBD(this);

        gestionnaireBD.resetChansons();

        Log.d(TAG, "allo bb");

            int id;
            int colonneId;

            String titre;
            int colonneTitre;

            Chanson chanson;

            //Base de données
            //curseurChansons = this.getBD().queryChansons();

            //Permet d'aller lire la musique dans le téléphone
            ContentResolver contentResolver = getContentResolver();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor curseurFichiersAudio = contentResolver.query(uri, null, null, null, null);

            if (curseurFichiersAudio != null && curseurFichiersAudio.moveToFirst()) {
                //Récupère les numéros des colonnes de la table des chansons retournées
                colonneId = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media._ID);
                colonneTitre = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media.TITLE);

                //Itère sur la liste de toutes les chansons et les rajoute à la liste des chansons disponibles sur le téléphone dans l'application
                do {
                    //Récupère les données pertinentes de la ligne visée par le curseur
                    id = curseurFichiersAudio.getInt(colonneId);
                    titre = curseurFichiersAudio.getString(colonneTitre);

                    chanson = new Chanson(id, titre);

                    gestionnaireBD.insertChanson(chanson);

                    //arrayListChansons.add(chanson);
                }

                //Itère sur les chansons jusqu'à la dernière
                while (curseurFichiersAudio.moveToNext());

        }
        return false;
    }

    public static class PreferenceFragmentClone extends android.preference.PreferenceFragment
    {
        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            scannerMusique = findPreference("scan");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_params, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        startActivity(intentMain);
        return true;
    }
}