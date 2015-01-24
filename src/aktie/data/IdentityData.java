package aktie.data;

import javax.persistence.Entity;
import javax.persistence.Id;


/**
    Internal information about identity not shared with others

*/
@Entity
public class IdentityData
{

    public static int DONE = 0;
    public static int UPDATE = 1;
    public static int REQUESTED = 2;

    @Id
    private String id;
    private long firstSeen;

    private long lastCommunityNumber;
    private long lastTemplateNumber;
    private long lastMembershipNumber;

    private long lastConnectionAttempt;
    private long lastSuccessfulConnection;

    private long totalNonFileReceived;
    private long totalReceived;
    private long totalSent;

    private long lastIdentityUpdate;
    private int identityStatus;
    private int identityUpdatePriority;

    private long lastCommunityUpdate;
    private int communityStatus;
    private int communityUpdatePriority;

    private long lastMemberUpdate;
    private int memberStatus;
    private int memberUpdatePriority;

    private boolean mine;

    public String getId()
    {
        return id;
    }

    public void setId ( String id )
    {
        this.id = id;
    }

    public long getFirstSeen()
    {
        return firstSeen;
    }

    public void setFirstSeen ( long firstSeen )
    {
        this.firstSeen = firstSeen;
    }

    public long getLastCommunityNumber()
    {
        return lastCommunityNumber;
    }

    public void setLastCommunityNumber ( long lastCommunityNumber )
    {
        this.lastCommunityNumber = lastCommunityNumber;
    }

    public long getLastTemplateNumber()
    {
        return lastTemplateNumber;
    }

    public void setLastTemplateNumber ( long lastTemplateNumber )
    {
        this.lastTemplateNumber = lastTemplateNumber;
    }

    public long getLastMembershipNumber()
    {
        return lastMembershipNumber;
    }

    public void setLastMembershipNumber ( long lastMembershipNumber )
    {
        this.lastMembershipNumber = lastMembershipNumber;
    }

    public long getLastConnectionAttempt()
    {
        return lastConnectionAttempt;
    }

    public void setLastConnectionAttempt ( long lastConnectionAttempt )
    {
        this.lastConnectionAttempt = lastConnectionAttempt;
    }

    public long getLastSuccessfulConnection()
    {
        return lastSuccessfulConnection;
    }

    public void setLastSuccessfulConnection ( long lastSuccessfulConnection )
    {
        this.lastSuccessfulConnection = lastSuccessfulConnection;
    }

    public long getTotalNonFileReceived()
    {
        return totalNonFileReceived;
    }

    public void setTotalNonFileReceived ( long totalNonFileReceived )
    {
        this.totalNonFileReceived = totalNonFileReceived;
    }

    public long getTotalReceived()
    {
        return totalReceived;
    }

    public void setTotalReceived ( long totalReceived )
    {
        this.totalReceived = totalReceived;
    }

    public long getTotalSent()
    {
        return totalSent;
    }

    public void setTotalSent ( long totalSent )
    {
        this.totalSent = totalSent;
    }

    public long getLastCommunityUpdate()
    {
        return lastCommunityUpdate;
    }

    public void setLastCommunityUpdate ( long lastCommunityUpdate )
    {
        this.lastCommunityUpdate = lastCommunityUpdate;
    }

    public int getCommunityStatus()
    {
        return communityStatus;
    }

    public void setCommunityStatus ( int communityStatus )
    {
        this.communityStatus = communityStatus;
    }

    public long getLastMemberUpdate()
    {
        return lastMemberUpdate;
    }

    public void setLastMemberUpdate ( long lastMemberUpdate )
    {
        this.lastMemberUpdate = lastMemberUpdate;
    }

    public int getMemberStatus()
    {
        return memberStatus;
    }

    public void setMemberStatus ( int memberStatus )
    {
        this.memberStatus = memberStatus;
    }

    public int getCommunityUpdatePriority()
    {
        return communityUpdatePriority;
    }

    public void setCommunityUpdatePriority ( int communityUpdatePriority )
    {
        this.communityUpdatePriority = communityUpdatePriority;
    }

    public int getMemberUpdatePriority()
    {
        return memberUpdatePriority;
    }

    public void setMemberUpdatePriority ( int memberUpdatePriority )
    {
        this.memberUpdatePriority = memberUpdatePriority;
    }

    public long getLastIdentityUpdate()
    {
        return lastIdentityUpdate;
    }

    public void setLastIdentityUpdate ( long lastIdentityUpdate )
    {
        this.lastIdentityUpdate = lastIdentityUpdate;
    }

    public int getIdentityStatus()
    {
        return identityStatus;
    }

    public void setIdentityStatus ( int identityStatus )
    {
        this.identityStatus = identityStatus;
    }

    public int getIdentityUpdatePriority()
    {
        return identityUpdatePriority;
    }

    public void setIdentityUpdatePriority ( int identityUpdatePriority )
    {
        this.identityUpdatePriority = identityUpdatePriority;
    }

    public boolean isMine()
    {
        return mine;
    }

    public void setMine ( boolean mine )
    {
        this.mine = mine;
    }

}
