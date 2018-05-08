package itstar.wei.droid.crosswalk.lib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import library.itstar.wei.xk.CrosswalkLib;
import library.itstar.wei.xk.view.SpinKit.SpinKitView;
import library.itstar.wei.xk.view.SpinKit.sprite.Sprite;
import library.itstar.wei.xk.view.SpinKit.style.ThreeBounce;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate ( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        SpinKitView spinKitView = ( SpinKitView ) findViewById( R.id.spin_kit );
        Sprite           sprite      = new ThreeBounce();
        spinKitView.setIndeterminateDrawable( sprite );

        CrosswalkLib.init( this, "1.3.001.004", "gbb", "0" );
        CrosswalkLib.initView( ( TextView ) findViewById( R.id.launch_loading ), spinKitView );
        CrosswalkLib.addPingFinishListener( new CrosswalkLib.PingFinishListener()
        {
            @Override
            public void callBeta ()
            {

            }
        });
    }
}
