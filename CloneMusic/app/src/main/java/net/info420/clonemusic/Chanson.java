package net.info420.clonemusic;

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
