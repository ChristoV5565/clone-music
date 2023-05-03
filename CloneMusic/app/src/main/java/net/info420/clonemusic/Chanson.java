package net.info420.clonemusic;

import android.net.Uri;

public class Chanson {
    private Uri uri;
    private String titre;
    private long longueur;
    private long ID;

    public Chanson(long ID, Uri uri, String titre, long longueur)
    {
        this.uri = uri;
        this.titre = titre;
        this.longueur = longueur;
        this.ID = ID;
    }

    public long getID() {
        return ID;
    }

    public Uri getUri() {
        return uri;
    }

    public String getTitre() {
        return titre;
    }

    public long getLongueur() {
        return longueur;
    }
}
