package library.itstar.wei.xk.model;

import java.util.Arrays;
import java.util.List;

import library.itstar.wei.xk.utils.LogUtil;

/**
 * Created by Ching Wei on 2018/3/9.
 */

public class CheckWebURL
{
    public static CheckWebURL instance ()
    {

        if ( _instance == null )
        {
            _instance = new CheckWebURL();
        }

        return _instance;
    }

    private List< String > getProxyData ()
    {
        return Arrays.asList(
                "-jb_",
                "-tgp_",
                "-sp_",
                "-cq9_",
                "-ab_",
                "-ag_",
                "-gd_",
                "-n2_",
                "-ea_",
                "-pt-",
                "chatClient",
                "assign",
                "-bb_",
                "-mg_"
        );
    }
    private List< String > getGameData ()
    {
        return Arrays.asList(
                "jb_",
                "tgp_",
                "sp_",
                "cq9_",
                "ab_",
                "ag_",
                "-gd_",
                "-n2_",
                "-ea_",
                "-pt-",
                "-bb_",
                "-mg_"
        );
    }

    private List< String > getCloseData1 ()
    {
        return Arrays.asList(
                "-pt-",
                "-bb_",
                "-jb_"
        );
    }
    private List< String > getCloseData2 ()
    {
        return Arrays.asList(
                "pt",
                "bb",
                "jb"
        );
    }
    private List< String > getSuppourMutiWindowsData1 ()
    {
        return Arrays.asList(
                "-tgp_",
                "xxxxxxxxxxxxxxxxx123"
        );
    }
    private List< String > getSuppourMutiWindowsData2 ()
    {
        return Arrays.asList(
                "tgp",
                "xxxxxxxxxxxxxxxxx123"
        );
    }

    private List< String > getNativeData1 ()
    {
        return Arrays.asList(
                "-bb_",
                "xxxxxxxxxxxxxxxxx123"
        );
    }
    private List< String > getNativeData2 ()
    {
        return Arrays.asList(
                "bb",
                "xxxxxxxxxxxxxxxxx123"
        );
    }

    public boolean isSuppotMutiWindows( String aURL )
    {
        LogUtil.logError( "onCreateWindowRequested", "isSuppotMutiWindows: " +aURL );
        if( aURL.contains( "MbProxy" ) )
        {
            return checkMode1( aURL, getSuppourMutiWindowsData1() );
        }
        else if( aURL.contains( "/game/page/html/" ) )
//        else if( aURL.contains( "/mobile/game/" ) )
        {
            return checkMode2( aURL, getSuppourMutiWindowsData2() );
        }
        else if( aURL.contains( "/game/proxy/" ) )
//        else if( aURL.contains( "/mobile/game/" ) )
        {
            return checkMode3( aURL, getSuppourMutiWindowsData2() );
        }
        return false;
    }

    public boolean isCloseButton( String aURL )
    {
        if( aURL.contains( "MbProxy" ) )
        {
            return checkMode1( aURL, getCloseData1() );
        }
        else if( aURL.contains( "/game/page/html/" ) )
//        else if( aURL.contains( "/mobile/game/" ) )
        {
            return checkMode2( aURL, getCloseData2() );
        }
        return false;
    }

    public boolean isThirdBrower( String aURL )
    {
        if( aURL.contains( "MbProxy" ) )
        {
            return checkMode1( aURL, getProxyData() );
        }
        else if( aURL.contains( "/game/page/html/" ) )
//        else if( aURL.contains( "/mobile/game/" ) )
        {
            return checkMode2( aURL, getGameData() );
        }

        return false;
    }

    public boolean isNativeBrower( String aURL )
    {
        if( aURL.contains( "MbProxy" ) )
        {
            return checkMode1( aURL, getNativeData1() );
        }
        else if( aURL.contains( "/game/page/html/" ) )
//        else if( aURL.contains( "/mobile/game/" ) )
        {
            return checkMode2( aURL, getNativeData2() );
        }

        return false;
    }

    private boolean checkMode1( String aURL, List< String > aList )
    {
        for( int i = 0; i < aList.size(); i++ )
        {
            if( aURL.contains( aList.get( i ) ) )
            {
                return true;
            }
        }
        return false;
    }
    private boolean checkMode2( String aURL, List< String > aList )
    {
        String aFilterURL = "/game/page/html/";
        String qqTmp = aURL.substring( aURL.indexOf( aFilterURL ) + aFilterURL.length() );
        String aaTmp = qqTmp.substring( qqTmp.indexOf( "/mobile/" ) + "/mobile/".length() );
//        String qqTmp = aURL.substring( aURL.indexOf( "/mobile/game/" ) + 13 );

        String[] splitTmp = aaTmp.split( "/" );
        for( int i = 0; i < aList.size(); i++ )
        {
            if( splitTmp[0].contains( aList.get( i ) ) && splitTmp[0].length() == aList.get( i ).length() )
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkMode3( String aURL, List< String > aList )
    {
        String aFilterURL = "/game/proxy/";
        String qqTmp = aURL.substring( aURL.indexOf( aFilterURL ) + aFilterURL.length() );
        String aaTmp = qqTmp.substring( qqTmp.indexOf( "/mobile/" ) + "/mobile/".length() );
//        String qqTmp = aURL.substring( aURL.indexOf( "/mobile/game/" ) + 13 );

        String[] splitTmp = aaTmp.split( "/" );
        for( int i = 0; i < aList.size(); i++ )
        {
            if( splitTmp[0].contains( aList.get( i ) ) && splitTmp[0].length() == aList.get( i ).length() )
            {
                return true;
            }
        }
        return false;
    }


    private static CheckWebURL _instance = null;
}
