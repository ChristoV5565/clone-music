package net.info420.clonemusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.net.URLDecoder;

public class ActiviteCreationPartie2 extends AppCompatActivity {

    public static final String TAG = "CreationPt1";

    TextView titrePlaylist;
    ListView listViewMusique;
    GestionnaireBD gestionnaireBD;
    Intent intentMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_creation_partie2);

        titrePlaylist = findViewById(R.id.titrePlayList);
        titrePlaylist.setText(getIntent().getExtras().getString("titre"));

        //tests
        listViewMusique = findViewById(R.id.listViewMusique);
        //Instanciation du gestionnaire de base de données

        gestionnaireBD = new GestionnaireBD(this);

        //Récupère les chansons dans la base de données, puis les affiche dans une listview

        String[] IDChansons = getIntent().getExtras().getStringArray("selection");


        for(int compteur = 0; compteur < IDChansons.length; compteur ++)
        {
            Log.d(TAG, IDChansons[compteur]);

            //String allo = IDChansons[compteur];

            int increment = Integer.parseInt(IDChansons[compteur]);
            increment ++;
            IDChansons[compteur] = String.valueOf(increment);
        }

        Cursor curseur = gestionnaireBD.queryChansons(IDChansons);

        String[] from = {GestionnaireBD.TITRE_CHANSONS};
        int[] to = {android.R.id.text1};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.liste_cocher, curseur, from, to, 0);
        ActiviteCreationPartie2.MyViewBinder myViewBinder = new ActiviteCreationPartie2.MyViewBinder();

        //Liaison du curseur à la liste
        adapter.setViewBinder(myViewBinder);
        listViewMusique.setAdapter(adapter);

        //Liaison les intent aux activités
        intentMain = new Intent(this, ActiviteCreationPartie1.class);

        //Générer toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Clone Music");
        toolbar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_params, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(intentMain);
        return true;
    }

    private class MyViewBinder implements SimpleCursorAdapter.ViewBinder
    {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int fieldIndex) {
            return false;
        }
    }
}