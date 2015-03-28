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
                if ( rf != null )
                {
                    callback.update ( rf );
                }

                else
                {
                    CObj err = new CObj();
                    err.pushString ( CObj.ERROR, "Could not download " + b.getString ( CObj.NAME ) );
                }

            }

            return true;
        }

        return false;
    }

}
