package net.info420.clonemusic;

/*
*
* Classe LecteurMusique
*
* Permet de contrôler la lecture de fichiers audio sur le téléphone.
*
* */

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;

public class LecteurMusique extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    //Lecteur de musique
    private MediaPlayer mediaPlayer;
    //Position de la chanson
    private int position;
    //Nombre de chansons total dans le téléphone
    private int nbChansons;
    //Lie le lecteur et le service ensemble
    private final IBinder musicBinder = new MusicBinder();
    //Tag pour le debugging
    private final String TAG = "LecteurMusique";
    //Flag pour la lecture
    private boolean surPause = false;
    //Gestionnaire pour la base de données
    GestionnaireBD gestionnaireBD;

    @Override
    public void onCreate() {
        super.onCreate();

        position = 0;
        mediaPlayer = new MediaPlayer();

        gestionnaireBD = new GestionnaireBD(this);

        nbChansons = gestionnaireBD.queryNombreChansons();
        Log.d(TAG,"Nombre de chansons : " + nbChansons);

        init();

    }

    //Lorsque invoqué, continue la chanson si elle était sur pause, ou la démarre s'il s'agit d'une nouvelle chanson
    public void jouer()
    {
        if(surPause)
        {
            surPause = false;
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.reset();

            gestionnaireBD = new GestionnaireBD(this);
            int idMedia = gestionnaireBD.queryChansons(position);

            Uri uriChanson = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idMedia);

            try
            {
                mediaPlayer.setDataSource(getApplicationContext() ,uriChanson);
            } catch (IOException e) {
                Log.e(TAG, getString(R.string.erreur_io), e);
            }

            mediaPlayer.prepareAsync();
        }

        Log.d(TAG, "Jouer position : " + position);
    }

    public void resetPause()
    {
        surPause = false;
    }

    public void setChanson(int position)
    {
        this.position = position;
    }

    public void init()
    {
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    public class MusicBinder extends Binder
    {
        LecteurMusique getLecteur()
        {
            return LecteurMusique.this;
        }
    }

    public LecteurMusique() {}

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        jouerSuivant();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer)
    {
        mediaPlayer.start();
        Intent rafraichir = new Intent("LECTEUR_PRET");
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(rafraichir);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }

    public int getPosition()
    {
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration()
    {
        return mediaPlayer.getDuration();
    }

    public boolean lectureEnCours()
    {
        return mediaPlayer.isPlaying();
    }

    public void pause()
    {
        surPause = true;
        mediaPlayer.pause();
        Log.d(TAG, "Sur pause");
    }

    public void setPosition(int position)
    {
        mediaPlayer.seekTo(position);
    }

    public void play()
    {
        surPause = false;
        mediaPlayer.start();
    }

    public void jouerPrecedent()
    {
        position --;

        //Retourne à la fin de la liste si l'index de la chanson précédente est en-dessous de 0
        if(position < 0)
        {
            position = nbChansons - 1;
        }
        resetPause();
        jouer();
    }

    public void jouerSuivant()
    {
        position ++;

        //Retourne au début de la liste si on tente de jouer une chanson après la toute dernière de la liste
        if(position >= nbChansons)
        {
            position = 0;
        }
        resetPause();
        jouer();
    }
}
