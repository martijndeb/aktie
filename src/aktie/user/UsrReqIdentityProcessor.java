package aktie.user;

import aktie.GenericProcessor;
import aktie.data.CObj;

public class UsrReqIdentityProcessor extends GenericProcessor
{

    private IdentityManager identManager;

    public UsrReqIdentityProcessor ( IdentityManager i )
    {
        identManager = i;
    }

    @Override
    public boolean process ( CObj b )
    {
        String type = b.getType();

        if ( CObj.USR_IDENTITY_UPDATE.equals ( type ) )
        {
            identManager.requestIdenties();
            return true;
        }

        return false;
    }

}
