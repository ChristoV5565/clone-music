package net.info420.clonemusic;

/*
*
* Classe chanson
*
* Cette classe permet de définir une chanson au niveau de l'application et de la base de données.
* Permet de stocker un titre et un identifiant. L'identifiant est utilié par mediaPlayer pour faire
* jouer la musique sur le téléphone
*
* */

public class Chanson {
    private String titre;
    private long ID;

    public Chanson(long ID, String titre)
    {
        this.titre = titre;
        this.ID = ID;
    }

    public long getID()
    {
        return ID;
    }

    public String getTitre()
    {
        return titre;
    }

}
