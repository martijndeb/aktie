package aktie.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class FileInfo
{

    @Id
    private String id;
    private String communityId;
    private String wholeDigest;
    private String fragmentDigest;
    private long numberHasFile;
    private boolean hasLocal;

    public String getId()
    {
        return id;
    }

    public void setId ( String id )
    {
        this.id = id;
    }

    public String getWholeDigest()
    {
        return wholeDigest;
    }

    public void setWholeDigest ( String wholeDigest )
    {
        this.wholeDigest = wholeDigest;
    }

    public String getFragmentDigest()
    {
        return fragmentDigest;
    }

    public void setFragmentDigest ( String fragmentDigest )
    {
        this.fragmentDigest = fragmentDigest;
    }

    public long getNumberHasFile()
    {
        return numberHasFile;
    }

    public void setNumberHasFile ( long numberHasFile )
    {
        this.numberHasFile = numberHasFile;
    }

    public boolean isHasLocal()
    {
        return hasLocal;
    }

    public void setHasLocal ( boolean hasLocal )
    {
        this.hasLocal = hasLocal;
    }

    public String getCommunityId()
    {
        return communityId;
    }

    public void setCommunityId ( String communityId )
    {
        this.communityId = communityId;
    }

}
