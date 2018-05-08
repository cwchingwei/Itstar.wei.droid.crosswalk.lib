package library.itstar.wei.xk.def;

import library.itstar.wei.xk.local.AppConfig;

public interface WebServiceSet
{
    public final static String APP_LOCAL_SERVICE_DEV           = "http://869app.info/";
    public final static String APP_LOCAL_SERVICE_PRO           = "https://appctl.55bckapp.com/";
    public final static String APP_LOCAL_PATH_REDIRE           = "/mob_controller/judgeUpdate.php";
    public final static String APP_LOCAL_PATH_IP                = "http://bot.whatismyipaddress.com";

//    public final static String APP_LOCAL_SERVICE_DEV           = "http://192.168.42.128/";
//    public final static String APP_LOCAL_SERVICE_PRO           = "http://192.168.42.128/";
//    public final static String APP_LOCAL_PATH_REDIRE           = "/test1/index.php";

    public final static String APP_SSL_CONFIG               = AppConfig.isDev()? APP_LOCAL_SERVICE_DEV + APP_LOCAL_PATH_REDIRE : APP_LOCAL_SERVICE_PRO + APP_LOCAL_PATH_REDIRE;
    public final static String APP_SSL_GETIP               = AppConfig.isDev()? APP_LOCAL_PATH_IP : APP_LOCAL_PATH_IP;
}
