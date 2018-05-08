package library.itstar.wei.xk;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import library.itstar.wei.xk.def.SharedPreferencesKey;
import library.itstar.wei.xk.local.AppConfig;
import library.itstar.wei.xk.local.AyncUpdateListener;
import library.itstar.wei.xk.local.CallBackListener;
import library.itstar.wei.xk.local.SystemConfig;
import library.itstar.wei.xk.model.ConfigAsyncTimeoutTask;
import library.itstar.wei.xk.model.DownloadAsyncTask;
import library.itstar.wei.xk.model.JSONModel;
import library.itstar.wei.xk.model.PingModel;
import library.itstar.wei.xk.model.ShowDialog;
import library.itstar.wei.xk.state.ErrorPingState;
import library.itstar.wei.xk.utils.FileUtils;
import library.itstar.wei.xk.utils.LibraryFileProvider;
import library.itstar.wei.xk.utils.LogUtil;
import library.itstar.wei.xk.utils.NetWorkUtil;
import library.itstar.wei.xk.view.SpinKit.SpinKitView;
import library.itstar.wei.xk.view.act.WebLoadX;

/**
 * Created by Ching Wei on 2018/5/7.
 */

public class CrosswalkLib
{
    private static TextView mLaunchLoading = null;
    private static SpinKitView mSpinKitView = null;
    private static PingFinishListener mPingFinishListener = null;
    public static void init ( final Activity activity, String appVersion, String appApp, String isDev )
    {
        LogUtil.logInfo( LogUtil.TAG, "CrosswalkLib init" );
        LogUtil.logInfo( LogUtil.TAG, "App Version:[" + appVersion + "]; APP:[" + appApp + "]; isDev:[" + isDev + "]");
        AppConfig.setAppAPP( appApp );
        AppConfig.setAppVersion( appVersion );
        AppConfig.setIsDev( isDev );

        SystemConfig.construct( activity );
        SystemConfig.instance().resetWebURL();
        construct( activity );
    }

    public static void initView ( TextView launchLoading, SpinKitView spinKitView)
    {
        mLaunchLoading = launchLoading;
        mSpinKitView = spinKitView;
    }

    public static void addPingFinishListener(PingFinishListener pingFinishListener )
    {
        mPingFinishListener = pingFinishListener;
    }

    private static void construct( final Activity activity )
    {

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M )
        {
            int      PERMISSION_ALL = 1;
            String[] PERMISSIONS    = { Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_NETWORK_STATE };

            if ( !hasPermissions( activity, PERMISSIONS ) )
            {
                ActivityCompat.requestPermissions( activity, PERMISSIONS, PERMISSION_ALL );
            }
        }

        if ( !NetWorkUtil.checkInternetConnection( activity ) )
        {
            ShowDialog.instance().showButtonRestartMessageDialog( activity, activity.getString( R.string.dialog_please_check_your_wifi ), activity.getString( R.string.dialog_title_error ), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick ( DialogInterface dialogInterface, int i )
                {
                    ShowDialog.realse();
                }
            } );
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put( "app_version", AppConfig.getAppVersion() );
        map.put( "OS", AppConfig.getAppOS() );
        map.put( "App", AppConfig.getAppAPP() );
        map.put( "IsDev",  AppConfig.getIsDev() );

        ConfigAsyncTimeoutTask.instance().getSystemConfig(
                new CallBackListener()
                {
                    @Override
                    public void onTaskCompleted ( String response )
                    {
                        if ( response == null || response.trim().equalsIgnoreCase( "null" ) )
                        {
                            return;
                       }
                        LogUtil.logInfo( LogUtil.TAG, response );

                        JSONModel.instance().setJSONObject( response );

                        if ( JSONModel.instance().getFeebackUpdate() )
                        {
                            FileUtils.writeFile( activity.getFilesDir().toString() + File.separator + "web.txt", response );
//                            Intent intent = new Intent( MainThreadActivity.this, WebLoadX.class );
//                            startActivity( intent );

                            if( mSpinKitView != null )
                            mSpinKitView.setVisibility( View.GONE );
                            if( mLaunchLoading != null )
                            mLaunchLoading.setText( activity.getString( R.string.launch_loading ) );

                            dofor:
//                    for( String url : JSONModel.instance().getNewWebUrl() )
                            LogUtil.logInfo( LogUtil.TAG, "==Ping START==" );
                            for ( Iterator i = JSONModel.instance().getNewWebUrl().iterator(); i.hasNext(); )
                            {
                                String cUrl = ( String ) i.next();
                                PingModel.instance().doPing( cUrl, new PingModel.CallBackListener()
                                {
                                    @Override
                                    public void onTaskCompleted ( ErrorPingState errorPingState )
                                    {
//                                    state.getWebUrlsTime().add( errorPingState.getUrlTime() );
//                                    state.getWebUrls().add( errorPingState.getFastDomain() );
                                    }
                                } );

                                if ( !i.hasNext() )
                                {
                                    String tmpUrl = SystemConfig.instance().getSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, null );
                                    if ( tmpUrl == null )
                                    {
                                        SystemConfig.instance().putSharedPreString( SharedPreferencesKey.SHARED_PRERENCES_KEY_WEB_URL, cUrl );
                                        Toast.makeText( activity, activity.getString( R.string.dialog_please_check_connect_lose ), Toast.LENGTH_SHORT ).show();
                                    }
                                }
                            }

                            LogUtil.logInfo( LogUtil.TAG, "==Ping END==" );

                            if( mPingFinishListener != null )
                            mPingFinishListener.callBeta();
//                            Beta.checkUpgrade( false, false );
//                            runOnUiThread( new Runnable()
//                            {
//                                @Override
//                                public void run ()
//                                {
                            if( mSpinKitView != null )
                                mSpinKitView.setVisibility( View.GONE );
                            if( mLaunchLoading != null )
                            {
                                mLaunchLoading.setVisibility( View.VISIBLE );
                                mLaunchLoading.setText( activity.getString( R.string.launch_finish ) );
                            }

//                                }
//                            });

                            Intent intent = new Intent( activity, WebLoadX.class );
                            activity.startActivity( intent );
                        }
                        else
                        {
                            final String url = JSONModel.instance().getAppUpDateUrl().replace( ".html", ".apk" );
                            LogUtil.logInfo( LogUtil.TAG, "download apk..." + url );
                            if( !URLUtil.isValidUrl( url ) )
                            {
                                ShowDialog.instance().showMessageErrorDialog( activity, "请联络站台管理员", "网页开启错误" );
                            }
                            else
                            {
                                new Thread( new Runnable()
                                {
                                    @Override
                                    public void run ()
                                    {
                                        try
                                        {
                                            DownloadAsyncTask.instance().apkDownload(
                                                    new CallBackListener()
                                                    {
                                                        @Override
                                                        public void onTaskCompleted ( String response )
                                                        {
                                                            Intent intent = new Intent( Intent.ACTION_VIEW );

                                                            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N )
                                                            {
                                                                intent.setFlags( Intent.FLAG_GRANT_READ_URI_PERMISSION );
                                                                Uri contentUri = LibraryFileProvider.getUriForFile( activity, BuildConfig.APPLICATION_ID + ".fileProvider", new File( response ) );
                                                                intent.setDataAndType( contentUri, "application/vnd.android.package-archive" );
                                                            }
                                                            else
                                                            {
                                                                intent.setDataAndType( Uri.fromFile( new File( response ) ), "application/vnd.android.package-archive" );
                                                                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                            }
                                                            activity.startActivity( intent );
                                                            activity.finish();
                                                        }
                                                    },
                                                    new AyncUpdateListener()
                                                    {
                                                        @Override
                                                        public void onProgressUpdate ( String response )
                                                        {
                                                            if( mLaunchLoading != null )
                                                            mLaunchLoading.setText( String.format( "%s %%", response ) );
                                                        }
                                                    },
                                                    url,
                                                    activity.getExternalCacheDir().getAbsolutePath()
                                            );
                                        }
                                        catch ( Exception e )
                                        {
                                            e.printStackTrace();
                                        }
                                    }
                                } ).start();
                            }
                        }
//                        _launch_loading.setText( MainThreadActivity.this.getString( R.string.launch_finish ) );
                    }
                },
                new AyncUpdateListener()
                {
                    @Override
                    public void onProgressUpdate ( String response )
                    {
                        if( Integer.parseInt( response ) > 3 )
                        {
//                            Beta.checkUpgrade( false, false );
                            if( mPingFinishListener != null ) mPingFinishListener.callBeta();
                            String webFilePath = activity.getFilesDir().toString() + File.separator + "web.txt";
                            String mem_string = null;
                            if( !FileUtils.isFileExist( webFilePath ) )
                                mem_string = load(activity,"defaultAppInfo");
                            else
                                mem_string = FileUtils.readFile( webFilePath, "UTF-8" ).toString();

                            if( JSONModel.instance().isJSONValid( mem_string ) )
                            {
                                JSONModel.instance().setJSONObject( mem_string );
                            }
                            else
                            {
                                JSONModel.instance().setJSONObject( load(activity,"defaultAppInfo") );
                            }
                            Intent intent = new Intent( activity, WebLoadX.class );
                            activity.startActivity( intent );
                        }
                        else
                        {
//                            _launch_loading.setText( MainThreadActivity.this.getString( R.string.launch_loading ) + response );
                        }
                    }
                }, map, activity
        );
    }

    public static void releaseView ()
    {
        mLaunchLoading = null;
        mSpinKitView = null;
    }

    private static boolean hasPermissions ( Context context, String... permissions )
    {
        if ( context != null && permissions != null )
        {
            for ( String permission : permissions )
            {
                if ( ActivityCompat.checkSelfPermission( context, permission ) != PackageManager.PERMISSION_GRANTED )
                {
                    return false;
                }
            }
        }
        return true;
    }

    private static String load ( Context context, String key )
    {
        String     value      = null;
        Properties properties = new Properties();
        try
        {
            properties.load( context.getAssets().open( "publish.properties" ) );
            value = properties.getProperty( key );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        System.out.println( "value: " + value );
        return value;
    }

    public interface PingFinishListener
    {
        void callBeta();
    }
}
