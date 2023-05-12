package net.info420.clonemusic;

/*
*
* Activité principale
*
* Cette activité est affichée lors de l'ouverture de l'application. Elle affiche les chansons qui
* sont présentes dans la base de données.
*
* Lors de la première ouverture, l'utilisateur doit faire un scan à partir de l'activité paramètres
* pour que sa musique se retrouve dans la liste.
*
* L'activité permet à l'utilisateur de sélectionner une chanson dans la liste pour la faire jouer.
* Lors de la lecture, l'application affiche un mediaController qui permet à l'utilisateur de contrôler
* la lecture (pause, jouer, avant, arrière, seek)
*
* */

import net.info420.clonemusic.LecteurMusique.MusicBinder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ActivitePrincipale extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MediaController.MediaPlayerControl {

    //Tag de débogage pour logcat
    public static String TAG = "main";

    //Code utilisé lorsque l'application demande la permission de lire les fichiers dans le téléphone
    int codePermissionStockage = 1;

    //Déclaration des éléments du layout
    AppCompatButton boutonMorceaux;
    AppCompatButton boutonListeLecture;
    ListView listViewMusique;
    ListView listViewPlaylists;

    //Déclaration des éléments locaux de la classe
    private LecteurMusique lecteurMusique;
    private boolean serviceAttache = false;
    private ControleurMusique controleurMusique;

    //Intents pour les différentes activités
    Intent intentParametres;
    Intent intentLecteurMusique;
    Intent intentInfos;
    Intent intentPlaylists;

    //Broadcastreciever pour la batterie
    BroadcastRecieverBatterie broadcastRecieverBatterie;
    //Gestionnaire pour l'accès à la base de données
    GestionnaireBD gestionnaireBD;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialisation intents du menu
        intentParametres = new Intent(this, ActiviteParametres.class);
        intentInfos = new Intent(this, ActiviteInfos.class);
        intentPlaylists = new Intent(this, ActiviteCreationPartie1.class);

        //Liaison des objets avec éléments du layout
        boutonMorceaux = findViewById(R.id.boutonMorceaux);
        boutonListeLecture = findViewById(R.id.boutonListeLecture);
        listViewMusique = findViewById(R.id.listViewMusique);
        listViewPlaylists = findViewById(R.id.listViewPlaylists);

        //Définition des listeners
        boutonMorceaux.setOnClickListener(this);
        boutonListeLecture.setOnClickListener(this);
        listViewMusique.setClickable(true);
        listViewMusique.setOnItemClickListener(this);

        //Rendre texte du bouton "Morceaux" plus grand pour indiquer qu'il est sélectionné par défaut
        boutonMorceaux.setTextSize(18);
        boutonListeLecture.setTextColor(getResources().getColor(R.color.gray, getTheme()));

        //Permet de désactiver l'animation des boutons. Le système désactive l'animation lors de l'application d'une couleur d'arrière-plan par programmation
        boutonMorceaux.setBackgroundColor(0);
        boutonListeLecture.setBackgroundColor(0);

        //Générer toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Clone Music");
        toolbar.setTitleTextColor(Color.WHITE);

        //Instanciation du gestionnaire de base de données
        gestionnaireBD = new GestionnaireBD(this);

        //Récupère les chansons dans la base de données, puis les affiche dans une listview
        Cursor curseur = gestionnaireBD.queryChansons();

        String[] from = {GestionnaireBD.TITRE_CHANSONS};
        int[] to = {android.R.id.text1};

        //Sélectionne toutes les occurences et les place dans le textview de chaque élément de la liste
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.item_liste, curseur, from, to, 0);
        MyViewBinder myViewBinder = new MyViewBinder();

        //Liaison du curseur à la liste
        adapter.setViewBinder(myViewBinder);
        listViewMusique.setAdapter(adapter);

        //Demande la permission de lire les fichiers à l'utilisateur si ce n'a pas déjà été fait
        if (verifPermissionStockage() == false) {
            demanderPermissionStockage();
        }

        //Créer le reciever et l'enregistrer
        broadcastRecieverBatterie = new BroadcastRecieverBatterie();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        registerReceiver(broadcastRecieverBatterie, intentFilter);
    }

    //Permet de démarrer le service de lecture lors de l'ouverture de l'application
    @Override
    protected void onStart()
    {
        super.onStart();

        if (intentLecteurMusique == null)
        {
            //Intent pour le démarrage du service
            intentLecteurMusique = new Intent(this, LecteurMusique.class);
            //Attacher le service à l'activité
            bindService(intentLecteurMusique, conexionServiceMusique, Context.BIND_AUTO_CREATE);
            //Démarrage du service
            startService(intentLecteurMusique);

            Log.d(TAG, "Service de lecture démarré");
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(rafraichisseur, new IntentFilter("LECTEUR_PRET"));
    }

    private class MyViewBinder implements SimpleCursorAdapter.ViewBinder {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int fieldIndex) {
            return false;
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(broadcastRecieverBatterie);
    }

    //Handler pour les onClick des boutons
    @Override
    public void onClick(View view)
    {

        //Permet de déterminer lequel des boutons a été cliqué
        switch (view.getId()) {
            case R.id.boutonMorceaux:
                Log.d(TAG, "Bouton Morceaux cliqué");

                //Changer la taille du texte et sa couleur pour mettre en évidence l'onglet sélectionné
                boutonMorceaux.setTextSize(18);
                boutonListeLecture.setTextSize(16);
                boutonListeLecture.setTextColor(getResources().getColor(R.color.gray, getTheme()));
                boutonMorceaux.setTextColor(getResources().getColor(R.color.white, getTheme()));

                //Changer la visibilité des listes pour montrer la bonne liste lorsque le bouton est cliqué
                listViewPlaylists.setVisibility(View.GONE);
                listViewMusique.setVisibility(View.VISIBLE);

                break;
            case R.id.boutonListeLecture:
                Log.d(TAG, "Bouton Listes de lecture cliqué");

                //Changer la taille du texte pour mettre en évidence l'onglet sélectionné
                boutonMorceaux.setTextSize(16);
                boutonListeLecture.setTextSize(18);
                boutonListeLecture.setTextColor(getResources().getColor(R.color.white, getTheme()));
                boutonMorceaux.setTextColor(getResources().getColor(R.color.gray, getTheme()));

                //Changer la visibilité des listes pour montrer la bonne liste lorsque le bouton est cliqué
                listViewPlaylists.setVisibility(View.VISIBLE);
                listViewMusique.setVisibility(View.GONE);

                break;
        }
    }

    //Création du menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        Log.d(TAG, "Menu créé");

        return true;
    }

    //Handler pour le clic sur les éléments du menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.itemInfos:
                startActivity(intentInfos);
                break;

            case R.id.itemParametres:
                startActivity(intentParametres);
                break;

            case R.id.itemPlaylist:
                startActivity(intentPlaylists);
                break;
        }

        return true;
    }

    //Permet de demander la permission de fouiller dans les fichiers de l'utilisateur
    //Source : https://www.youtube.com/watch?v=1D1Jo1sLBMo
    boolean verifPermissionStockage()
    {
        int res = ContextCompat.checkSelfPermission(ActivitePrincipale.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (res == PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Permission stockage accordée");
            return true;
        }
        else
        {
            Log.d(TAG, "Permission stockage manquante");
            return false;
        }
    }

    //Demande la permission si elle n'a pas déjà été accordée
    //Source : https://www.youtube.com/watch?v=1D1Jo1sLBMo
    void demanderPermissionStockage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ActivitePrincipale.this, Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            Toast.makeText(ActivitePrincipale.this, getString(R.string.stockage_refuse), Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(ActivitePrincipale.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, codePermissionStockage);
        }
    }

    //Handler pour le clic sur les éléments de la liste de chansons. Fait jouer la chanson sélectionnée
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        initControleurMusique();
        lecteurMusique.resetPause();
        lecteurMusique.setChanson(i);
        lecteurMusique.jouer();

        controleurMusique.show(0);

    }

    //Permet de lier le service de musique à la classe qui contrôle la lecture
    //Source : https://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
    private ServiceConnection conexionServiceMusique = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicBinder binder = (MusicBinder) iBinder;
            lecteurMusique = binder.getLecteur();
            serviceAttache = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceAttache = false;
        }
    };

    //Initialiser les fonctions correspondant aux éléments du controleur de média
    private void initControleurMusique() {

        if(controleurMusique == null)
        {
            Log.d(TAG, "Initialisation du controleur + lecteur");
            controleurMusique = new ControleurMusique(this);

            controleurMusique.setPrevNextListeners(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    jouerSuivant();
                }
            }, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    jouerPrecedent();
                }
            });

            controleurMusique.setMediaPlayer(this);
            controleurMusique.setAnchorView(findViewById(R.id.listViewMusique));
            controleurMusique.setEnabled(true);
            controleurMusique.show();
        }
        else
        {
            Log.d(TAG, "Contrôleur déjà initialisé");
        }
    }

    //Joue la chanson suivante
    private void jouerSuivant()
    {
        lecteurMusique.jouerSuivant();
    }

    //Joue la chanson précédente
    private void jouerPrecedent()
    {
        lecteurMusique.jouerPrecedent();
    }

    //Démarrage de la lecture de média
    @Override
    public void start()
    {
        lecteurMusique.jouer();
    }

    //Pause la lecture et garde sa progression en mémoire
    @Override
    public void pause()
    {
        lecteurMusique.pause();
    }

    //Récupère la longueur de la chanson
    @Override
    public int getDuration()
    {
        if(lecteurMusique != null && serviceAttache && lecteurMusique.lectureEnCours())
        {
            return lecteurMusique.getDuration();
        }
        else
        {
            return 0;
        }
    }

    //Récupère la position courante de la lecture
    @Override
    public int getCurrentPosition()
    {
        if(lecteurMusique != null && serviceAttache && lecteurMusique.lectureEnCours())
        {
            return lecteurMusique.getPosition();
        }
        else
        {
            return 0;
        }
    }

    //Déplacement de la position de lecture
    @Override
    public void seekTo(int i)
    {
        lecteurMusique.setPosition(i);
    }

    //Retourne l'état de la lecture. True si en lecture, false sinon
    @Override
    public boolean isPlaying()
    {
        if(lecteurMusique != null && serviceAttache)
        {
            return lecteurMusique.lectureEnCours();
        }
        else
        {
            return false;
        }
    }

    private BroadcastReceiver rafraichisseur = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (controleurMusique != null)
            {
                controleurMusique.show();
            }
        }
    };

    //Méthodes ajoutées par le controleur
    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause()
    {
        return true;
    }

    @Override
    public boolean canSeekBackward()
    {
        return true;
    }

    @Override
    public boolean canSeekForward()
    {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    public GestionnaireBD getBD()
    {
        return new GestionnaireBD(this);
    }


}