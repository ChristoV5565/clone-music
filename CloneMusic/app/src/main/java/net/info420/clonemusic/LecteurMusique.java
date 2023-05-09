package net.info420.clonemusic;

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
import java.util.ArrayList;


public class LecteurMusique extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener{

    private MediaPlayer mediaPlayer;
    private ArrayList<Chanson> listeMusique;
    private int position;
    private final IBinder musicBinder = new MusicBinder();
    private final String TAG = "LecteurMusique";
    private boolean surPause = false;

    GestionnaireBD gestionnaireBD;

    @Override
    public void onCreate() {
        super.onCreate();

        position = 0;
        mediaPlayer = new MediaPlayer();

        init();

    }

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

            //Position est la position dans la liste

            //Chanson chanson = listeMusique.get(position);
            //long chansonCourante = chanson.getID();
            //Uri uriChanson = chanson.getUri();
            gestionnaireBD = new GestionnaireBD(this);
            int test = gestionnaireBD.queryChansons(position);

            Uri uriChanson = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,test);

            try
            {
                mediaPlayer.setDataSource(getApplicationContext() ,uriChanson);
            } catch (IOException e) {
                Log.e(TAG, getString(R.string.erreur_io), e);
            }

            mediaPlayer.prepareAsync();
        }

        Log.d(TAG, "JOUER");
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

    public void setListeMusique(ArrayList<Chanson> listeMusique)
    {
        this.listeMusique = listeMusique;
    }

    public class MusicBinder extends Binder
    {
        LecteurMusique getLecteur()
        {
            return LecteurMusique.this;
        }
    }

    public LecteurMusique() {
    }

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
            position = listeMusique.size() - 1;
        }
        resetPause();
        jouer();
    }

    public void jouerSuivant()
    {
        position ++;

        //Retourne au début de la liste si on tente de jouer une chanson après la toute dernière de la liste
        if(position >= listeMusique.size())
        {
            position = 0;
        }
        resetPause();
        jouer();
    }
}
