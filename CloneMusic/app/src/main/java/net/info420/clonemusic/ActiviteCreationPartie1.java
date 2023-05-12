package net.info420.clonemusic;

/*
*
* Activité création partie 1
*
* Cette activité permet à l'utilisateur créer une liste de lecture. En premier lieu, l'utilisateur
* séletionne les chansons à ajouter et donne un titre à la liste. Il est ensuite redirigé vers la
* partie 2 qui lui permettra de mettre les chansons en ordre
*
* */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ActiviteCreationPartie1 extends AppCompatActivity implements View.OnClickListener{

    //Tag utilisé pour le debugging
    public static final String TAG = "CreationPt1";
    //Intent qui redirige vers l'activité principale
    Intent intentActivitePrincipale;
    //Intent qui redirige vers la partie 2 de la création (ordre des chansons)
    Intent intentPartie2;
    //Gestionnaire qui permet la modification et la lecture de la base de données
    GestionnaireBD gestionnaireBD;
    //Listview qui contient les chansons dans la base de données
    ListView listViewMusique;
    //Permet d'aller vers la partie 2 quand des chansons sont sélectionnées
    Button boutonContinuer;
    //Titre de la liste de lecture choisi par l'utilisateur
    EditText titrePlaylist;
    //Permet d'ajouter des IDs de chansons dans la liste sans faire de trous entre les occurences.
    int ajout;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activite_creation_partie1);

        //Initialisation de l'intent vers l'activité principale
        intentActivitePrincipale = new Intent(this, ActivitePrincipale.class);

        //Initialisation de la listview
        listViewMusique = findViewById(R.id.listViewMusique);
        listViewMusique.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listViewMusique.setItemsCanFocus(false);
        listViewMusique.setClickable(true);

        //Initialisation du bouton
        boutonContinuer = findViewById(R.id.boutonContinuer);
        boutonContinuer.setOnClickListener(this);

        //Initialisation du titre
        titrePlaylist = findViewById(R.id.titrePlayList);

        //Instanciation du gestionnaire de base de données
        gestionnaireBD = new GestionnaireBD(this);

        //Générer toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Clone Music");
        toolbar.setTitleTextColor(Color.WHITE);

        //Récupère les chansons dans la base de données, puis les affiche dans une listview
        Cursor curseur = gestionnaireBD.queryChansons();

        String[] donnees = {GestionnaireBD.TITRE_CHANSONS};
        int[] vue = {android.R.id.text1};

        //Sélectionne les titres de chansons à partir de la base de données, puis les place dans les items de la liste
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.liste_cocher, curseur, donnees, vue, 0);
        ActiviteCreationPartie1.MyViewBinder myViewBinder = new ActiviteCreationPartie1.MyViewBinder();
        adapter.setViewBinder(myViewBinder);
        listViewMusique.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.boutonContinuer:

                if (titrePlaylist.getText().toString().equals("")) {
                    //Le titre est vide
                    Toast.makeText(this, R.string.titre_vide, Toast.LENGTH_LONG).show();
                } else if (listViewMusique.getCheckedItemPositions().size() == 0) {
                    //Aucune chanson n'est sélectionnée
                    Toast.makeText(this, getString(R.string.selection_vide), Toast.LENGTH_LONG).show();
                } else {
                    //Cette variable est utilisée pour s'assurer qu'il n'y a pas d'espace entre les index des chansons dans la liste
                    ajout = 0;

                    //Récupère les éléments de la liste qui sont cochés
                    SparseBooleanArray coche = listViewMusique.getCheckedItemPositions();

                    Log.d(TAG, "Taille : " + coche.size());

                    //Tableau qui contient les index des chansons
                    int[] chansons = new int[coche.size()];
                    //Tableau de string qui est équivalent à celui des chansons. Utilisé pour transférer les données dans le Intent
                    String[] chansonsString = new String[coche.size()];

                    //Extrait les numéros des chansons dont les cases sont cochées
                    for (int compteur = 0; compteur < coche.size(); compteur++) {
                        //Donne le numéro de l'item de la liste
                        int position = coche.keyAt(compteur);

                        //Si la case est cochée, ajouter son numéro à la liste
                        if (coche.valueAt(compteur)) {

                            Log.d(TAG, "Chanson " + position + " ajoutée");
                            chansons[ajout] = position;
                            ajout++;
                        }
                    }

                    //Convertit le tableau de int en String
                    for (int compteur = 0; compteur < coche.size(); compteur++) {
                        chansonsString[compteur] = String.valueOf(chansons[compteur]);
                    }

                    //Initialisation du Intent, transfert des données dans un bundle, puis démarrage de la prochaine activité
                    intentPartie2 = new Intent(getApplicationContext(), ActiviteCreationPartie2.class);
                    Bundle bundle = new Bundle();
                    bundle.putStringArray("selection", chansonsString);
                    bundle.putString("titre", titrePlaylist.getText().toString());
                    intentPartie2.putExtras(bundle);
                    startActivity(intentPartie2);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_params, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        startActivity(intentActivitePrincipale);
        return true;
    }

    private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int fieldIndex) {
            return false;
        }
    }
}