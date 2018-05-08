package library.itstar.wei.xk.view.act;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkUIClient;
import org.xwalk.core.XWalkView;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

import library.itstar.wei.xk.R;
import library.itstar.wei.xk.def.SharedPreferencesKey;
import library.itstar.wei.xk.local.SystemConfig;
import library.itstar.wei.xk.local.WebAppInterface;
import library.itstar.wei.xk.model.CheckWebURL;
import library.itstar.wei.xk.model.IPAsyncTask;
import library.itstar.wei.xk.model.JSONModel;
import library.itstar.wei.xk.model.PingModel;
import library.itstar.wei.xk.model.ShowDialog;
import library.itstar.wei.xk.state.ErrorPingState;
import library.itstar.wei.xk.utils.LogUtil;
import library.itstar.wei.xk.utils.ScreenUtil;


/**
 * Created by Ching Wei on 2018/4/13.
 */


public class WebLoadX extends CordovaActivity
{
    private CordovaWebView   webView         = null;
    private ProgressBar mProgressBar    = null;
    private ImageView   mClose          = null;
    //    private       SwipeRefreshLayout mySwipeRefreshLayout  = null;
//    private       PtrClassicFrameLayout mPtrFrame             = null;
    private String      _url            = null;
    private Thread      _thread_timeout = null;
    private boolean     timeout         = true;
    private boolean     webViewReady    = false;
    private       ArrayList< String > historyOverrideURL              = null;
    private       String              TAG                             = WebLoadX.class.getName();
    public static String              KEY_BUNDLE_WEB_URL              = "key_bundle_web_url";
    public static String              KEY_BUNDLE_CAN_BACK             = "key_bundle_can_back";
    public static String              KEY_BUNDLE_CAN_FINISH           = "key_bundle_can_finish";
    public static String              KEY_BUNDLE_HISTORY_URL          = "key_bundle_history_url";
    public static String              KEY_BUNDLE_SUPPORT_MUTI_WINDOWS = "key_bundle_support_muti_windows";
    private boolean     WEB_CAN_BACK    = false;
    private boolean     WEB_CAN_FINISH  = false;
    private boolean     isCloseEnable   = false;

    private float startX = 0;
    private float startY = 0;

    int mMotionDownX, mMotionDownY;

    @Override
    public void onCreate ( @Nullable Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.app_webloadx );

//        _url = JSONModel.instance().getWebUrl();
        webView = ( CordovaWebView ) findViewById( R.id.web_view );
        mProgressBar = ( ProgressBar ) findViewById( R.id.progress_bar );
        mClose = ( ImageView ) findViewById( R.id.web_close );
//        webView.setOriginAccessWhitelist( "https://www.jdb1688.net/", new String[2] );
        initData();
    }

//    private void initPtr()
//    {
//        mPtrFrame = ( PtrClassicFrameLayout ) findViewById( R.id.store_house_ptr_frame );
//        mPtrFrame.setLastUpdateTimeRelateObject( this );
//        mPtrFrame.setPtrHandler( new PtrHandler()
//        {
//            @Override
//            public boolean checkCanDoRefresh ( PtrFrameLayout frame, View content, View header )
//            {
//                return PtrDefaultHandler.checkContentCanBePulledDown( frame, content, header );
//            }
//
//            @Override
//            public void onRefreshBegin ( PtrFrameLayout frame )
//            {
//                webView.reload( XWalkView.RELOAD_NORMAL );
//            }
//        } );
//        // the following are default settings
//        mPtrFrame.setResistance( 1.7f );
//        mPtrFrame.setRatioOfHeaderHeightToRefresh( 1.5f );
//        mPtrFrame.setDurationToClose( 200 );
//        mPtrFrame.setDurationToCloseHeader( 1000 );
//        // default is false
//        mPtrFrame.setPullToRefresh( true );
//        // default is true
//        mPtrFrame.setKeepHeaderWhenRefresh( true );
//    }

    public boolean onKeyDown ( int keyCode, KeyEvent event )
    {
        if ( event.getAction() == KeyEvent.ACTION_DOWN )
        {
            switch ( keyCode )
            {
                case KeyEvent.KEYCODE_BACK:
                    LogUtil.logError( "XWalkView", "KEYCODE_BACK" );
                    if ( WEB_CAN_FINISH && !webView.getNavigationHistory().canGoBack() )
                    {
                        LogUtil.logError( "XWalkView", "KEY_BUNDLE_CAN_FINISH" );
                        finish();
                    }
            }
        }
        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            LogUtil.logError( "XWalkView", "dispatchKeyEvent KEYCODE_BACK" );
            if( WEB_CAN_BACK )
            {
                return super.dispatchKeyEvent(event);
            }
        }
        return true;
    }

    @Override
    protected void onPause ()
    {
        super.onPause();
        if ( webView != null && webViewReady )
        {
            if( historyOverrideURL != null )
            {
                historyOverrideURL.clear();
            }
            webView.pauseTimers();
            webView.onHide();
        }
    }

    @Override
    protected void onResume ()
    {
        super.onResume();
        if ( webView != null && webViewReady )
        {
            webView.resumeTimers();
            webView.onShow();
        }
    }

    @Override
    public void onDestroy ()
    {
        LogUtil.logError( "XWalkView", "onDestroy" );
        super.onDestroy();
        webViewReady = true;
        if ( webView != null )
        {
            webView.onDestroy();
        }
    }

    @Override
    protected void onXWalkReady ()
    {
        initListener();
//        initPtr();

        XWalkPreferences.setValue( XWalkPreferences.ANIMATABLE_XWALK_VIEW, false );
        XWalkPreferences.setValue( XWalkPreferences.REMOTE_DEBUGGING, true );
//        XWalkPreferences.setValue( XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true );
//        XWalkPreferences.setValue( XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true );

        webView.getSettings().setAllowFileAccess( true );
        webView.getSettings().setAllowFileAccessFromFileURLs( true );
        webView.getSettings().setAllowUniversalAccessFromFileURLs( true );
        webView.getSettings().setLoadWithOverviewMode( true );
        webView.getSettings().setUseWideViewPort( true );
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically( true );
//        webView.getSettings().setLayoutAlgorithm( XWalkSettings.LayoutAlgorithm.SINGLE_COLUMN );
        webView.getSettings().setSupportZoom( true );// 设置可以支持缩放
        webView.getSettings().setBuiltInZoomControls( true );// 设置出现缩放工具 是否使用WebView内置的缩放组件，由浮动在窗口上的缩放控制和手势缩放控制组成，默认false
        webView.getSettings().setSupportMultipleWindows( true );
        webView.getSettings().setDomStorageEnabled( true );
        String language = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LANGUAGE_COOKIES, null );
        if( language != null && language.equalsIgnoreCase( "zh_cn" ) )
        {
            LogUtil.logError( "WebLoadX", "language: " + language );
            webView.getSettings().setAcceptLanguages("zh-cn");
        }
        webView.setSaveEnabled( true );
        webView.setKeepScreenOn( true );//        webView.loadUrl(" http://www.youtube.com/watch?v=1QkK3ts_7ok");

        webView.getSettings().setJavaScriptEnabled( true );
//        webView.getSettings().setUserAgentString( " Mozilla/5.0(Android 7.0;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Mobile Safari/537.36" );
        // Do anyting with the embedding API

//        webView.loadUrl("http://www.liaohuqiu.net/", null);
//        webView.loadUrl("http://10.159.5.71/test/index_test.php", null);

        initData();
//        XWalkCookieManager xm = new XWalkCookieManager();
//        xm.setAcceptCookie( true );
//        xm.setAcceptFileSchemeCookies( true );
//        xm.setCookie( _url, "web_mem=fbdda4c089aa308f60b0f5accca8d95d79a59d77bef05633a62f331692273668ac62500121957f000f13a2b498d74f8e" );
        webView.loadUrl( _url, null );
//        webView.loadUrl( "http://www.mu1.wlb.fwebs.org/mb/index/app#gameLobby", null );
        webViewReady = true;
//        LogUtil.logError( "XWalkView", "initWebView: " + getWebviewVersionInfo( webView ) );
    }

    private void initData()
    {
        _url = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, null );
        historyOverrideURL = new ArrayList<>();
        if( getIntent().hasExtra( KEY_BUNDLE_WEB_URL ) )
        {
            _url = getIntent().getStringExtra( KEY_BUNDLE_WEB_URL );

            if( getIntent().hasExtra( KEY_BUNDLE_HISTORY_URL ) )
            {
                LogUtil.logError( "XWalkView", "initData: KEY_BUNDLE_HISTORY_URL: " );
                historyOverrideURL = getIntent().getStringArrayListExtra( KEY_BUNDLE_HISTORY_URL );
//                if( historyOverrideURL.size() > 0 && CheckWebURL.instance().isCloseButton( historyOverrideURL.get( 0 ) ) )
                {
//                    LogUtil.logError( "XWalkView", "initData: historyOverrideURL"  );
                    mClose.setVisibility( View.VISIBLE );
                }
            }

            WEB_CAN_BACK = true;
            WEB_CAN_FINISH = true;
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_SENSOR );
//            XWalkPreferences.setValue( XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, true );
//            webView.getSettings().setSupportMultipleWindows( true );
        }
        else
        {
            setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
//            webView.getSettings().setSupportMultipleWindows( false );
//            XWalkPreferences.setValue( XWalkPreferences.SUPPORT_MULTIPLE_WINDOWS, false );
        }

        if( getIntent().hasExtra( KEY_BUNDLE_SUPPORT_MUTI_WINDOWS ) )
        {
            webView.getSettings().setSupportMultipleWindows( false );
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically( false );
//            mPtrFrame.setPtrHandler( null );
        }

//        try
//        {


//            String clogin = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LOGIN_COOKIES, "" );
////            if ( clogin.length() > 0 )
//            {
////                xm.setCookie( new URL( _url ).getHost(), SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LOGIN_COOKIES, "" ) );
//                xm.setCookie( new URL( _url ).getHost(), "web_mem=fbdda4c089aa308f60b0f5accca8d95d79a59d77bef05633a62f331692273668ac62500121957f000f13a2b498d74f8e" );
//            }
////            xm.flushCookieStore();
//
////            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP )
////            {
////                CookieManager.getInstance().flush();
////            }
////            else
////            {
////                CookieSyncManager.createInstance( this.getApplicationContext() );
////                CookieSyncManager.getInstance().sync();
////            }
//        }
//        catch ( MalformedURLException e )
//        {
//            LogUtil.logException( TAG, e );
//        }
    }

    private void initListener ()
    {
        mClose.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick ( View view )
            {

            }
        } );

        mClose.setOnTouchListener( new View.OnTouchListener()
        {
            @Override
            public boolean onTouch ( View view, MotionEvent motionEvent )
            {
                final int X = (int) motionEvent.getRawX();
                final int Y = (int) motionEvent.getRawY();
                float endX = motionEvent.getX();
                float endY = motionEvent.getY();
                switch ( motionEvent.getAction() & MotionEvent.ACTION_MASK )
                {
                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = ( RelativeLayout.LayoutParams ) view.getLayoutParams();
                        mMotionDownX = X - lParams.leftMargin;
                        mMotionDownY = Y - lParams.topMargin;

                        startX = motionEvent.getX();
                        startY = motionEvent.getY();

                        isCloseEnable = true;
                        Log.i( "layout_floating", "ACTION_DOWN: ");
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i( "layout_floating", "ACTION_UP: " + isCloseEnable);
                        if( isAClick( startX, endX, startY, endY ) )
                        {
                            Log.i( "layout_floating", "ACTION_CLICK: " + isCloseEnable);
                            if( isCloseEnable )
                            {
                                finish();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if( !isAClick( startX, endX, startY, endY ) )
                        {
                            Log.i( "layout_floating", "ACTION_MOVE: ");
                            isCloseEnable = false;
                        }

                        RelativeLayout.LayoutParams layoutParams = ( RelativeLayout.LayoutParams ) view.getLayoutParams();

                        if( ( X - mMotionDownX < 0) || ( X - mMotionDownX > ScreenUtil.getScreenWidthPx( WebLoadX.this )) )
                        {
                            return true;
                        }
                        else
                        {
                            layoutParams.leftMargin = X - mMotionDownX;
                        }
                        if( ( Y - mMotionDownY < 0) || ( Y - mMotionDownY > ScreenUtil.getScreenHeightPx( WebLoadX.this )))
                        {
                            return true;
                        }
                        else
                        {
                            layoutParams.topMargin = Y - mMotionDownY;
                        }

                        view.setLayoutParams( layoutParams );
                        break;
                }
                return false;
            }
        } );

//        mySwipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
//                                                   {
//                                                       @Override
//                                                       public void onRefresh ()
//                                                       {
//                                                           webView.reload( XWalkView.RELOAD_NORMAL );
//                                                       }
//                                                   }
//        );
        webView.setUIClient( new XWalkUIClient( webView )
        {
            @Override
            public boolean onCreateWindowRequested ( XWalkView view, InitiateBy initiator, ValueCallback< XWalkView > callback )
            {
                LogUtil.logInfo( "MyXWalkUIClient", "onCreateWindowRequested: " );

                final XWalkView tempView = new XWalkView( view.getContext() );
                tempView.setUIClient( new XWalkUIClient( tempView )
                {
                    @Override
                    public void onPageLoadStarted ( XWalkView view, String url )
                    {
                        LogUtil.logInfo( "onCreateWindowRequested", "onPageLoadStarted: ur;l: " + url);

                        historyOverrideURL.add( url );
                        Intent intent = null;
//                        if(CheckWebURL.instance().isNativeBrower( historyOverrideURL.get( 0 ) ) )
//                        {
//                            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
//                                    .enableUrlBarHiding()
//                                    .build();
//                            CustomTabActivityHelper.openCustomTab( WebLoadX.this, customTabsIntent, Uri.parse( url ), new WebviewFallback() );
//                            return;
//                        }
//                        else
                        {
                            intent = new Intent( view.getContext(), WebLoadX.class );
                            Bundle extras = new Bundle();
                            extras.putString( KEY_BUNDLE_WEB_URL, url );
                            extras.putBoolean( KEY_BUNDLE_CAN_BACK, false );
                            extras.putBoolean( KEY_BUNDLE_CAN_FINISH, true );
                            if( CheckWebURL.instance().isSuppotMutiWindows( historyOverrideURL.get( 0 ) ) )
                            {
                                Log.i( "CheckWebURL", "KEY_BUNDLE_SUPPORT_MUTI_WINDOWS: ");
                                extras.putBoolean( KEY_BUNDLE_SUPPORT_MUTI_WINDOWS, true );
                            }
                            Log.i( "CheckWebURL", "KEY_BUNDLE_SUPPORT_MUTI_WINDOWS NONONO: ");
                            intent.putExtras( extras );
//                        if( CheckWebURL.instance().isCloseButton( historyOverrideURL.get( 0 ) ) )
                            {
                                intent.putStringArrayListExtra( KEY_BUNDLE_HISTORY_URL, historyOverrideURL );
                            }
                            intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                            startActivity( intent );
                        }

//                        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
//                        CustomTabActivityHelper.openCustomTab( WebLoadX.this, customTabsIntent, Uri.parse( url ), new WebviewFallback() );

                        tempView.onDestroy();
                    }
                } );
                callback.onReceiveValue( tempView );

//                Intent intent = new Intent( WebLoadX.this, WebLoadX.class );
//                Bundle extras  = new Bundle();
//                extras.putString( KEY_BUNDLE_WEB_URL, url );
//                intent.putExtras( extras );
//                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
//                view.getContext().startActivity( intent );

                return true;
            }

            @Override
            public void onFullscreenToggled ( XWalkView view, boolean enterFullscreen )
            {
                LogUtil.logInfo( "MyXWalkUIClient", "onFullscreenToggled: " + enterFullscreen );
            }

            @Override
            public void onPageLoadStarted ( XWalkView view, String url )
            {
                timeout = true;
                _thread_timeout = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{Thread.sleep( 12000 );} catch (InterruptedException e) {}
                        if(timeout) {doGoPing( WebLoadX.this, webView, getString( R.string.error_msg_client_timeout ) );}
                    }
                });
                _thread_timeout.start();
            }

            @Override
            public void onPageLoadStopped ( XWalkView view, String url, LoadStatus status )
            {
                XWalkCookieManager cookieManager = null;
                cookieManager = new XWalkCookieManager();
                String allCookies = null;
                try
                {
                    if( _url.indexOf( "https://" ) != -1 )
                    {
                        allCookies = cookieManager.getCookie( "https://" + new URL(_url).getHost() );
                    }
                    else
                    {
                        allCookies = cookieManager.getCookie( "http://" + new URL(_url).getHost() );
                    }
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }
                if( allCookies != null )
                    cookieStore( allCookies );
                LogUtil.logError( "onPageLoadStopped", "cookie: " + allCookies );
            }
        } );
        webView.setResourceClient( new XWalkResourceClient( webView )
        {
            @Override
            public void onReceivedSslError ( XWalkView view, ValueCallback< Boolean > callback, SslError error )
            {
                LogUtil.logInfo( "XWalkResourceClient", "onReceivedSslError: " );
                callback.onReceiveValue( true );
            }

            @Override
            public boolean shouldOverrideUrlLoading ( XWalkView view, String url )
            {
                LogUtil.logInfo( "XWalkResourceClient", "shouldOverrideUrlLoading: " + url );
//                WEB_CAN_FINISH = false;
//                if( CheckWebURL.instance().isThirdBrower( url ) )
//                {
//                    final XWalkView tempView = new XWalkView( view.getContext() );
//                    Intent intent = new Intent( view.getContext(), WebLoadX.class );
//                    Bundle extras  = new Bundle();
//                    extras.putString( KEY_BUNDLE_WEB_URL, url );
//                    extras.putBoolean( KEY_BUNDLE_CAN_BACK, false );
//                    extras.putBoolean( KEY_BUNDLE_CAN_FINISH, true );
//                    intent.putExtras( extras );
//                    intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
//                    view.getContext().startActivity( intent );
//                    tempView.onDestroy();
//                    return true;
//                }
//                else
//                {
//                    return super.shouldOverrideUrlLoading( view, url );
//                }
                return super.shouldOverrideUrlLoading( view, url );
            }

            @Override
            public void onLoadFinished ( XWalkView view, String url )
            {
                LogUtil.logInfo( "XWalkResourceClient", "onLoadFinished: " + url );
            }

            @Override
            public void onLoadStarted ( XWalkView view, String url )
            {
                super.onLoadStarted( view, url );
                view.loadUrl( "javascript:(function() { " +
                        "var apContainer = document.querySelector(\"#alphaContainer\");" +
                        "var viewPortMeta = document.querySelector('meta[name=\"viewport\"]');" +
                        "var touchIntro = document.querySelector(\"#touchIntro\");" +
                        "if( apContainer ) {apContainer.style.display =\"none\";} " +
                        "if( touchIntro ) {touchIntro.style.display =\"none\";} " +
                        "if (viewPortMeta) { if(!viewPortMeta.content.includes('device-width')){ viewPortMeta.content += ',width=device-width';}}" +
                        "})()" );
            }

            @Override
            public void onProgressChanged ( XWalkView view, int progressInPercent )
            {
                super.onProgressChanged( view, progressInPercent );
                mProgressBar.setProgress( progressInPercent );
                if ( progressInPercent == 100 )
                {
                    mProgressBar.setVisibility( View.GONE );
//                    mPtrFrame.refreshComplete();
                    _thread_timeout.interrupt();
                    timeout = false;
                }
                else
                {
                    mProgressBar.setVisibility( View.VISIBLE );
                }
//                LogUtil.logInfo( "MyXWalkResourceClient", "onProgressChanged: " + progressInPercent );
            }

            @Override
            public void onReceivedLoadError ( XWalkView view, int errorCode, String description, String failingUrl )
            {
                if ( errorCode == ERROR_HOST_LOOKUP )
                {
                    doGoPing( WebLoadX.this, webView, getString( R.string.error_msg_host_lookup ) );
                }
                else if ( errorCode == ERROR_CONNECT )
                {
                    doGoPing( WebLoadX.this, webView, getString( R.string.error_msg_connect ) );
                }
                else if ( errorCode == ERROR_TIMEOUT )
                {
                    doGoPing( WebLoadX.this, webView, getString( R.string.error_msg_timeout_server ) );
                }
            }
        } );
        WebAppInterface webAppInterface = new WebAppInterface(this);
        webAppInterface.addWebVIew( webView );
        webView.addJavascriptInterface( webAppInterface, "Android" );
    }

    private static String getWebviewVersionInfo ( XWalkView aWebView )
    {
        // Overridden UA string
        String alreadySetUA = aWebView.getSettings().getUserAgentString();

        // Next call to getUserAgentString() will get us the default
        aWebView.getSettings().setUserAgentString( null );

        // Devise a method for parsing the UA string
        String webViewVersion = ( aWebView.getSettings().getUserAgentString() );

        // Revert to overriden UA string
        aWebView.getSettings().setUserAgentString( alreadySetUA );

        return webViewVersion;
    }

    private boolean isAClick ( float startX, float endX, float startY, float endY )
    {
        float differenceX = Math.abs( startX - endX );
        float differenceY = Math.abs( startY - endY );
        return !( differenceX > 7/* =5 */ || differenceY > 7 );
    }

    private void cookieStore( String aCookie )
    {
        try
        {
            String[] a = aCookie.split( ";" );
            for( int i = 0; i < a.length; i++ )
            {
                String[] b = a[i].trim().split( "=" );
                if( b[0].trim().equalsIgnoreCase( "Language" ) )
                {
                    SystemConfig.instance().putSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_WEBVIEW_LANGUAGE_COOKIES, b[1] );
                }
            }
        }
        catch ( Exception e )
        {
//            LogUtil.logException( TAG, e );
        }
    }

    private void doGoPing ( final Activity aActivity, final XWalkView aWebView, final String aErrorMessage )
    {
        if ( WEB_CAN_FINISH )
        {
            return;
        }
        IPAsyncTask.instance().doQueryCurrentIP();

        SystemConfig.instance().resetWebURL();
        new Thread( new Runnable()
        {
            @Override
            public void run ()
            {
                try
                {
                    PingModel.instance().createError();
                    dofor:
//                    for( String url : JSONModel.instance().getNewWebUrl() )
                    for ( Iterator i = JSONModel.instance().getNewWebUrl().iterator(); i.hasNext(); )
                    {
                        String cUrl = ( String ) i.next();
                        PingModel.instance().doPing( cUrl, new PingModel.CallBackListener()
                        {
                            @Override
                            public void onTaskCompleted ( ErrorPingState errorPingState )
                            {
//                                state.getWebUrlsTime().add( errorPingState.getUrlTime() );
//                                state.getWebUrls().add( errorPingState.getFastDomain() );
                            }
                        } );

                        if ( !i.hasNext() )
                        {
                            String tmpUrl = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, null );
                            if ( tmpUrl == null )
                            {
//                                PingModel.instance().errorMsg( WebLoadT.this, aErrorMessage);
//                                Log.e( "PingStats", " " + PingModel.instance().errorMsg( WebLoadT.this, aErrorMessage) );

                                runOnUiThread( new Runnable()
                                {
                                    @Override
                                    public void run ()
                                    {
//                            webBase.setVisibility( View.GONE );
                                        String errorMsg = PingModel.instance().errorMsg( aActivity, aErrorMessage );
                                        ShowDialog.instance().showButtonRestartMessageDialog( aActivity, errorMsg, aActivity.getString( R.string.dialog_please_no_connect ), new DialogInterface.OnClickListener()
                                        {
                                            @Override
                                            public void onClick ( DialogInterface dialogInterface, int i )
                                            {
                                                ShowDialog.realse();
                                                runOnUiThread( new Runnable()
                                                {
                                                    @Override
                                                    public void run ()
                                                    {
                                                        PingModel.instance().createError();
                                                        aWebView.reload( XWalkView.RELOAD_IGNORE_CACHE );
                                                    }
                                                } );
                                            }
                                        } );
                                    }
                                } );
                            }
                            else
                            {
                                runOnUiThread( new Runnable()
                                {
                                    @Override
                                    public void run ()
                                    {
                                        _url = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, null );
//                                        Log.e( TAG, "reload _url: "+ _url );
                                        aWebView.loadUrl( _url );
                                    }
                                } );

                            }
                        }
                    }

//                    String tmpUrl =  SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, null );
//                    Log.e( "PingStats", "_url: " + tmpUrl );
                }
                catch ( Exception e )
                {
//                    LogUtil.logException( TAG, e );
                }
            }
        } ).start();
    }
}

