package net.info420.clonemusic;

/*
*
* Activité paramètres
*
* Cette activité permet à l'utilisateur de gérer les paramètres de l'application.
*
* Paramètres :
*
* Scanner la musique - Lance un scan des fichiers audio du téléphone. Le scan récupère certaines informations
*   des fichiers (titre, identifiant machine) et les place dans un objet Chanson, qui est ensuite ajouté
*   à la base de données de l'application. Chaque chanson a sa propre entrée dans la base de données.
*
* */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ActiviteParametres extends AppCompatActivity implements Preference.OnPreferenceClickListener {

    //Intent pour le démarrage de l'activité main quand l'utilisateur veut retourner en arrière
    Intent intentMain;
    //Fragment qui contient les paramètres
    PreferenceFragmentClone preferenceFragmentClone;
    //Préférence pour le scan de la musique
    static Preference scannerMusique;
    //Tag pour debugging
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

        //Déclaration du fragment et mise dans l'application
        preferenceFragmentClone = new PreferenceFragmentClone();
        getFragmentManager().beginTransaction().add(R.id.fragmentParametres, preferenceFragmentClone).commit();
    }

    //Permet de mettre le listener pour les paramètres. Ne peut pas être fait directement dans le onCreate() sinon le fragment est null
    @Override
    public void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        scannerMusique.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        //Données nécessaires pour insérer les chansons dans la BD
        int id;
        String titre;
        int colonneId;
        int colonneTitre;
        Chanson chanson;
        Uri uri;

        //Instancier le gestionnaire
        GestionnaireBD gestionnaireBD = new GestionnaireBD(this);
        //Supprime la table de chansons pour repartir à neuf
        gestionnaireBD.resetChansons();

        //Permet d'aller lire la musique dans le téléphone
        ContentResolver contentResolver = getContentResolver();
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        //Curseur qui pointe sur les médias
        Cursor curseurFichiersAudio = contentResolver.query(uri, null, null, null, null);

        if (curseurFichiersAudio != null && curseurFichiersAudio.moveToFirst())
        {
            //Récupère les numéros des colonnes de la table des chansons retournées
            colonneId = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media._ID);
            colonneTitre = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media.TITLE);

            //Itère sur la liste de toutes les chansons et les rajoute à la liste des chansons disponibles sur le téléphone dans l'application
            do {
                //Récupère les données pertinentes de la ligne visée par le curseur
                id = curseurFichiersAudio.getInt(colonneId);
                titre = curseurFichiersAudio.getString(colonneTitre);

                //Création de la chanson
                chanson = new Chanson(id, titre);

                //Mettre la chanson dans la base de données
                gestionnaireBD.insertChanson(chanson);
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

            //Récupérer la préférence pour pouvoir lui mettre son onClickListner()
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