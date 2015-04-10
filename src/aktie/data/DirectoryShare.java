package aktie.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class DirectoryShare
{

    @Id
    @GeneratedValue
    private long id;
    private String communityId;
    private String memberId;

    private String directory;

    private long lastCrawl;
    private long numberFiles;
    private long numberSubFolders;

    public long getId()
    {
        return id;
    }

    public void setId ( long id )
    {
        this.id = id;
    }

    public String getCommunityId()
    {
        return communityId;
    }

    public void setCommunityId ( String communityId )
    {
        this.communityId = communityId;
    }

    public String getMemberId()
    {
        return memberId;
    }

    public void setMemberId ( String memberId )
    {
        this.memberId = memberId;
    }

    public String getDirectory()
    {
        return directory;
    }

    public void setDirectory ( String directory )
    {
        this.directory = directory;
    }

    public long getLastCrawl()
    {
        return lastCrawl;
    }

    public void setLastCrawl ( long lastCrawl )
    {
        this.lastCrawl = lastCrawl;
    }

    public long getNumberFiles()
    {
        return numberFiles;
    }

    public void setNumberFiles ( long numberFiles )
    {
        this.numberFiles = numberFiles;
    }

    public long getNumberSubFolders()
    {
        return numberSubFolders;
    }

    public void setNumberSubFolders ( long numberSubFolders )
    {
        this.numberSubFolders = numberSubFolders;
    }

}
