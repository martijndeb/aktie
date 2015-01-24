package aktie.data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CommunityMember
{

    public static int DONE = 0;
    public static int UPDATE = 1;
    public static int REQUESTED = 2;

    @Id
    private String id;
    private String communityId;
    private String memberId;
    private long lastSubscriptionNumber;
    private long lastPostNumber;
    private long lastFileNumber;

    private long lastSubscriptionUpdate;
    private int subscriptionStatus;
    private int subscriptionUpdatePriority;

    private long lastPostUpdate;
    private int postStatus;
    private int postUpdatePriority;

    private long lastFileUpdate;
    private int fileStatus;
    private int fileUpdatePriority;

    public CommunityMember()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId ( String id )
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

    public long getLastSubscriptionNumber()
    {
        return lastSubscriptionNumber;
    }

    public void setLastSubscriptionNumber ( long lastSubscriptionNumber )
    {
        this.lastSubscriptionNumber = lastSubscriptionNumber;
    }

    public long getLastPostNumber()
    {
        return lastPostNumber;
    }

    public void setLastPostNumber ( long lastPostNumber )
    {
        this.lastPostNumber = lastPostNumber;
    }

    public long getLastFileNumber()
    {
        return lastFileNumber;
    }

    public void setLastFileNumber ( long lastFileNumber )
    {
        this.lastFileNumber = lastFileNumber;
    }

    public long getLastSubscriptionUpdate()
    {
        return lastSubscriptionUpdate;
    }

    public void setLastSubscriptionUpdate ( long lastSubscriptionUpdate )
    {
        this.lastSubscriptionUpdate = lastSubscriptionUpdate;
    }

    public int getSubscriptionStatus()
    {
        return subscriptionStatus;
    }

    public void setSubscriptionStatus ( int subscriptionStatus )
    {
        this.subscriptionStatus = subscriptionStatus;
    }

    public long getLastPostUpdate()
    {
        return lastPostUpdate;
    }

    public void setLastPostUpdate ( long lastPostUpdate )
    {
        this.lastPostUpdate = lastPostUpdate;
    }

    public int getPostStatus()
    {
        return postStatus;
    }

    public void setPostStatus ( int postStatus )
    {
        this.postStatus = postStatus;
    }

    public long getLastFileUpdate()
    {
        return lastFileUpdate;
    }

    public void setLastFileUpdate ( long lastFileUpdate )
    {
        this.lastFileUpdate = lastFileUpdate;
    }

    public int getFileStatus()
    {
        return fileStatus;
    }

    public void setFileStatus ( int fileStatus )
    {
        this.fileStatus = fileStatus;
    }

    public int getSubscriptionUpdatePriority()
    {
        return subscriptionUpdatePriority;
    }

    public void setSubscriptionUpdatePriority ( int subscriptionUpdatePriority )
    {
        this.subscriptionUpdatePriority = subscriptionUpdatePriority;
    }

    public int getPostUpdatePriority()
    {
        return postUpdatePriority;
    }

    public void setPostUpdatePriority ( int postUpdatePriority )
    {
        this.postUpdatePriority = postUpdatePriority;
    }

    public int getFileUpdatePriority()
    {
        return fileUpdatePriority;
    }

    public void setFileUpdatePriority ( int fileUpdatePriority )
    {
        this.fileUpdatePriority = fileUpdatePriority;
    }

}
