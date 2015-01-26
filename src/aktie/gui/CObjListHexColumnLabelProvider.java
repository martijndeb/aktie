package aktie.gui;

import aktie.crypto.Utils;

import java.util.Formatter;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public class CObjListHexColumnLabelProvider extends ColumnLabelProvider
{

    private String key;

    public CObjListHexColumnLabelProvider ( String k )
    {
        key = k;
    }

    @Override
    public String getText ( Object element )
    {
        CObjListGetter o = ( CObjListGetter ) element;
        String bs = o.getCObj().getString ( key );

        if ( bs != null )
        {
            byte bytes[] = Utils.toByteArray ( bs );
            StringBuilder sb = new StringBuilder();
            Formatter formatter = new Formatter ( sb );

            for ( int c = 0; c < bytes.length; c++ )
            {
                formatter.format ( "%02X", 0xFF & bytes[c] );
            }

            formatter.close();
            return sb.toString();
        }

        return "";
    }

}
