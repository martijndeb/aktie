package aktie.user;

import aktie.GenericProcessor;
import aktie.data.CObj;
import aktie.data.RequestFile;
import aktie.gui.GuiCallback;

public class UsrReqFileProcessor extends GenericProcessor
{

    private RequestFileHandler handler;
    private GuiCallback callback;

    public UsrReqFileProcessor ( RequestFileHandler h, GuiCallback cb )
    {
        handler = h;
        callback = cb;
    }

    @Override
    public boolean process ( CObj b )
    {
        String type = b.getType();

        if ( CObj.USR_DOWNLOAD_FILE.equals ( type ) )
        {
            RequestFile rf = handler.createRequestFile ( b );

            if ( callback != null )
            {
                callback.update ( rf );
            }

            return true;
        }

        return false;
    }

}
