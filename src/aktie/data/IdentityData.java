package aktie.data;

import javax.persistence.Column;
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

    private long nextClosestCommunityNumber;
    private long nextClosestTempalteNumber;
    private long nextClosestMembershipNumber;

    private int numClosestCommunityNumber;
    private int numClosestTemplateNumber;
    private int numClosestMembershipNumber;

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
    private String lastCommunityUpdateFrom;
    @Column ( columnDefinition = "INTEGER(10) default 0" )
    private int communityUpdateCycle;

    private long lastMemberUpdate;
    private int memberStatus;
    private int memberUpdatePriority;
    private String lastMemberUpdateFrom;
    @Column ( columnDefinition = "INTEGER(10) default 0" )
    private int memberUpdateCycle;

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

    public long getNextClosestCommunityNumber()
    {
        return nextClosestCommunityNumber;
    }

    public void setNextClosestCommunityNumber ( long nextClosestCommunityNumber )
    {
        this.nextClosestCommunityNumber = nextClosestCommunityNumber;
    }

    public long getNextClosestTempalteNumber()
    {
        return nextClosestTempalteNumber;
    }

    public void setNextClosestTempalteNumber ( long nextClosestTempalteNumber )
    {
        this.nextClosestTempalteNumber = nextClosestTempalteNumber;
    }

    public long getNextClosestMembershipNumber()
    {
        return nextClosestMembershipNumber;
    }

    public void setNextClosestMembershipNumber ( long nextClosestMembershipNumber )
    {
        this.nextClosestMembershipNumber = nextClosestMembershipNumber;
    }

    public int getNumClosestCommunityNumber()
    {
        return numClosestCommunityNumber;
    }

    public void setNumClosestCommunityNumber ( int numClosestCommunityNumber )
    {
        this.numClosestCommunityNumber = numClosestCommunityNumber;
    }

    public int getNumClosestTemplateNumber()
    {
        return numClosestTemplateNumber;
    }

    public void setNumClosestTemplateNumber ( int numClosestTemplateNumber )
    {
        this.numClosestTemplateNumber = numClosestTemplateNumber;
    }

    public int getNumClosestMembershipNumber()
    {
        return numClosestMembershipNumber;
    }

    public void setNumClosestMembershipNumber ( int numClosestMembershipNumber )
    {
        this.numClosestMembershipNumber = numClosestMembershipNumber;
    }

    public String getLastCommunityUpdateFrom()
    {
        return lastCommunityUpdateFrom;
    }

    public void setLastCommunityUpdateFrom ( String lastCommunityUpdateFrom )
    {
        this.lastCommunityUpdateFrom = lastCommunityUpdateFrom;
    }

    public int getCommunityUpdateCycle()
    {
        return communityUpdateCycle;
    }

    public void setCommunityUpdateCycle ( int communityUpdateCycle )
    {
        this.communityUpdateCycle = communityUpdateCycle;
    }

    public String getLastMemberUpdateFrom()
    {
        return lastMemberUpdateFrom;
    }

    public void setLastMemberUpdateFrom ( String lastMemberUpdateFrom )
    {
        this.lastMemberUpdateFrom = lastMemberUpdateFrom;
    }

    public int getMemberUpdateCycle()
    {
        return memberUpdateCycle;
    }

    public void setMemberUpdateCycle ( int memberUpdateCycle )
    {
        this.memberUpdateCycle = memberUpdateCycle;
    }

}
