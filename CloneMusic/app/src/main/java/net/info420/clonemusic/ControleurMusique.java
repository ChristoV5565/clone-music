package net.info420.clonemusic;

import android.content.Context;
import android.widget.MediaController;

//Source : https://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787
public class ControleurMusique extends MediaController
{
    public ControleurMusique(Context context)
    {
        super(context, false);
    }

    //Surcharger cette méthode permet d'empêcher le contrôleur de se cacher pendant la lecture de la chanson
    public void hide()
    {

    }

}
