package net.info420.clonemusic;

import net.info420.clonemusic.LecteurMusique.MusicBinder;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import java.util.ArrayList;

public class ActivitePrincipale extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, MediaController.MediaPlayerControl {

    //Tag de débogage pour logcat
    public static String TAG = "main";
    int codePermissionStockage = 1;

    //Déclaration des éléments du layout
    AppCompatButton boutonMorceaux;
    AppCompatButton boutonListeLecture;
    ListView listViewMusique;
    ListView listViewPlaylists;
    MenuItem itemPlaylist;

    //Déclaration des éléments locaux de la classe
    ArrayList<Chanson> arrayListChansons;
    ArrayAdapter arrayAdapter;
    private LecteurMusique lecteurMusique;
    private boolean serviceAttache = false;
    private ControleurMusique controleurMusique;

    //Intents pour les différentes activités
    Intent intentParametres;
    Intent intentLecteurMusique;
    Intent intentInfos;

    //Broadcastreciever pour la batterie
    BroadcastRecieverBatterie broadcastRecieverBatterie;

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
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialisation intents du menu
        intentParametres = new Intent(this, ActiviteParametres.class);
        intentInfos = new Intent(this, ActiviteInfos.class);

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

        //Initialisation de la liste de chansons derrière la listView
        arrayListChansons = new ArrayList<Chanson>();

        scanMusique(); //TODO: Remplacer par un appel à la base de données

        ArrayList<String> listeChansons = genererListeTitres(arrayListChansons);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listeChansons);
        listViewMusique.setAdapter(arrayAdapter);

        //Demande la permission de lire les fichiers à l'utilisateur si ce n'a pas déjà été fait
        if (verifPermissionStockage() == false) {
            demanderPermissionStockage();
        }

        //Créer le reciever et l'enregistrer
        broadcastRecieverBatterie = new BroadcastRecieverBatterie();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
        registerReceiver(broadcastRecieverBatterie, intentFilter);
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
                boutonMorceaux.setTextColor(getResources().getColor(R.color.black, getTheme()));

                //Changer la visibilité des listes pour montrer la bonne liste lorsque le bouton est cliqué
                listViewPlaylists.setVisibility(View.GONE);
                listViewMusique.setVisibility(View.VISIBLE);

                break;
            case R.id.boutonListeLecture:
                Log.d(TAG, "Bouton Listes de lecture cliqué");

                //Changer la taille du texte pour mettre en évidence l'onglet sélectionné
                boutonMorceaux.setTextSize(16);
                boutonListeLecture.setTextSize(18);
                boutonListeLecture.setTextColor(getResources().getColor(R.color.black, getTheme()));
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
        }



        return true;
    }

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

    public void scanMusique() {
        int id;
        int colonneId;

        String titre;
        int colonneTitre;

        long longueur;
        int colonneLongueur;

        int colonneUri;
        Uri uriChanson;

        //Permet d'aller lire la musique dans le téléphone
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor curseurFichiersAudio = contentResolver.query(uri, null, null, null, null);

        if (curseurFichiersAudio != null && curseurFichiersAudio.moveToFirst()) {
            //Récupère les numéros des colonnes de la table des chansons retournées
            colonneId = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media._ID);

            //NOUVEAU
            colonneUri = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media.DATA);

            colonneTitre = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media.TITLE);
            colonneLongueur = curseurFichiersAudio.getColumnIndex(MediaStore.Audio.Media.DURATION);

            //Itère sur la liste de toutes les chansons et les rajoute à la liste des chansons disponibles sur le téléphone dans l'application
            do {
                //Récupère les données pertinentes de la ligne visée par le curseur
                id = curseurFichiersAudio.getInt(colonneId);
                titre = curseurFichiersAudio.getString(colonneTitre);
                longueur = curseurFichiersAudio.getLong(colonneLongueur);
                uriChanson = Uri.parse(curseurFichiersAudio.getString(colonneUri));


                //Ajoute les données dans un objet chanson, qui est lui-même ajouté à la liste de toutes les chansons
                //arrayListChansons.add(new Chanson(id, titre, longueur));
                arrayListChansons.add(new Chanson(id, uriChanson, titre, longueur));
            }
            //Itère sur les chansons jusqu'à la dernière
            while (curseurFichiersAudio.moveToNext());

        }
    }

    public ArrayList<String> genererListeTitres(ArrayList<Chanson> liste)
    {
        ArrayList<String> listeTitres = new ArrayList<String>();

        for (Chanson chanson : liste) {
            listeTitres.add(chanson.getTitre());
        }

        return listeTitres;
    }

    //Handler pour le clic sur les éléments de la liste de chansons
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        lecteurMusique.setChanson(i);
        lecteurMusique.jouer();
        initControleurMusique();

    }

    //Source : https://code.tutsplus.com/tutorials/create-a-music-player-on-android-song-playback--mobile-22778
    private ServiceConnection conexionServiceMusique = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicBinder binder = (MusicBinder) iBinder;
            lecteurMusique = binder.getLecteur();
            //TODO : VÉRIFIER SI LES CHAMPS DE lA CLASSE SONT COMPATIBLES
            lecteurMusique.setListeMusique(arrayListChansons);
            serviceAttache = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            serviceAttache = false;
        }
    };

    //Initialiser les fonctions correspondant aux éléments du controleur de média
    private void initControleurMusique() {

        Log.d(TAG, "Initialisation du controleur + lecteur");

        if(controleurMusique == null)
        {
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


        }

        lecteurMusique.resetPause();

        controleurMusique.setMediaPlayer(this);
        controleurMusique.setAnchorView(findViewById(R.id.listViewMusique));
        controleurMusique.setEnabled(true);
        controleurMusique.show();
    }

    //Joue la chanson suivante
    private void jouerSuivant()
    {
        lecteurMusique.jouerSuivant();
        controleurMusique.show(0);
    }

    //Joue la chanson précédente
    private void jouerPrecedent()
    {
        lecteurMusique.jouerPrecedent();
        controleurMusique.show(0);
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
}