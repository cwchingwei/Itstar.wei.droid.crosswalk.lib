package library.itstar.wei.xk.local;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;
import org.xwalk.core.XWalkCookieManager;

import library.itstar.wei.xk.def.SharedPreferencesKey;
import library.itstar.wei.xk.state.JavascriptCommand;
import library.itstar.wei.xk.utils.LogUtil;

import static org.chromium.base.ThreadUtils.runOnUiThread;


/**
 * Created by Ching Wei on 2018/4/18.
 */

public class WebAppInterface
{
    private Context mContext;
    private CordovaWebView mWebView = null;
    public WebAppInterface ( Context aContext )
    {
        mContext = aContext;
    }

    public void addWebVIew ( CordovaWebView webView )
    {
        this.mWebView = webView;
    }
    //Android.glReady("logout")
    @org.xwalk.core.JavascriptInterface
    public void postMessage ( String str )
    {
        Log.e( "WebAppInterface", "postMessage: " + str );
        try
        {
//            JavascriptCommand state = FastJsonUtils.getSingleBean( str, JavascriptCommand.class );
            JavascriptCommand state  = new JavascriptCommand();
            JSONObject        object = new JSONObject( str );
            state.setRegister( object.getString( "register" ) );
            if( state.getRegister() != null )
            {
                Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( state.getRegister() ) );
                mContext.startActivity( intent );
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
    @org.xwalk.core.JavascriptInterface
    public void logout ()
    {
        Log.e( "Web fun ", "logout" );
        runOnUiThread( new Runnable()
        {
            @Override
            public void run ()
            {
                SystemConfig.instance().reset();
                mWebView.clearCache(true);
                mWebView.getNavigationHistory().clear();
                clearCookies( mContext );
                SystemConfig.instance().clearLoginCookie();
                SystemConfig.instance().resetWebURL();
                restartAPP ();
            }
        } );
    }

    @org.xwalk.core.JavascriptInterface
    public void loadURL ( final String str )
    {
        runOnUiThread( new Runnable()
        {
            @Override
            public void run ()
            {
                XWalkCookieManager xm = new XWalkCookieManager();
                xm.setAcceptCookie( true );
                xm.setAcceptFileSchemeCookies( true );
                LogUtil.logInfo( "loadURL", "Cookie " + SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LOGIN_COOKIES, "" ));
                xm.setCookie( str, SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LOGIN_COOKIES, "" ) );

                mWebView.getSettings().setSupportMultipleWindows( true );
                mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );
                mWebView.loadUrl( str, null );
            }
        } );
    }

    private void restartAPP ()
    {
//        Intent intent = new Intent( mContext, MainThreadActivity.class );
//        intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
//        mContext.startActivity( intent );

        android.os.Process.killProcess( android.os.Process.myPid() );
        System.exit( 0 );
    }

    @SuppressWarnings("deprecation")
    public static void clearCookies ( Context context )
    {

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            Log.d("Web fun ", "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
//            Log.d("Web fun ", "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr =CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager =CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }
}