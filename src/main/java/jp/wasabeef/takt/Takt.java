package jp.wasabeef.takt;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class Takt {

  private final static Program program = new Program();

  private Takt() {
  }

  public static Program stock(Application application) {
    return program.prepare(application);
  }


  public static void finish() {
    program.stop();
  }

  public static class Program {
    private String paras = "";
    private int threshold = 30;
    private Context appContext = null;
    private Metronome metronome;
    private boolean show = true;
    private boolean isPlaying = false;
    private WindowManager wm;
    private View stageView;
    private TextView fpsText;
    private LayoutParams params;
    private final DecimalFormat decimal = new DecimalFormat("#.0' fps'");

    public Program() {
    }

    private Program prepare(final Application application) {
      appContext = application;

      metronome = new Metronome();
      params = new LayoutParams();
      params.width = LayoutParams.WRAP_CONTENT;
      params.height = LayoutParams.WRAP_CONTENT;
      params.type = LayoutParams.TYPE_TOAST;
      params.flags = LayoutParams.FLAG_KEEP_SCREEN_ON | LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCH_MODAL;
      params.format = PixelFormat.TRANSLUCENT;
      params.gravity = Seat.BOTTOM_RIGHT.getGravity();
      params.x = 10;

      wm = WindowManager.class.cast(application.getSystemService(Context.WINDOW_SERVICE));
      LayoutInflater inflater = LayoutInflater.from(application);
      stageView = inflater.inflate(R.layout.stage, new RelativeLayout(application));
      fpsText = (TextView) stageView.findViewById(R.id.takt_fps);
      fpsText.setTextColor(Color.BLUE);

      listener(new Audience() {
        @Override
        public void heartbeat(double fps) {
          if (fpsText != null) {
            fpsText.setText(decimal.format(fps));

            paras ="fps_num="+fps+"&mem_usage="+PerformanceUtils.getFreeMemory(appContext)/1024+"/"+PerformanceUtils.getTotalMemory()/1024+"&app_version="+getAppVersion()+"&phonename="+getBrand()+"&sdk_version="+getVersion()+"&frameclass="+getRunningActivityName();
            if(fps<threshold) {
              new Thread(new Runnable() {
                @Override
                public void run() {
                  String result = TransferData("http://192.168.225.52/frame/postframe/?", paras);
                  Log.e("uploadresult",result);
                }
              }).start();

            }
          }
        }
      });

      return this;
    }

    public void play() {
      metronome.start();

      if (show && !isPlaying) {
        wm.addView(stageView, params);
        isPlaying = true;
      }
    }

    public void stop() {
      metronome.stop();
      if (show && stageView != null) {
        wm.removeView(stageView);
        isPlaying = false;
      }
    }

    public Program color(int color) {
      fpsText.setTextColor(color);
      return this;
    }

    public Program size(float size) {
      fpsText.setTextSize(size);
      return this;
    }

    /*
     * alpha from = 0.0, to = 1.0
     */
    public Program alpha(float alpha) {
      fpsText.setAlpha(alpha);
      return this;
    }

    public Program interval(int ms) {
      metronome.setInterval(ms);
      return this;
    }

    public Program listener(Audience audience) {
      metronome.addListener(audience);
      return this;
    }

    public Program hide() {
      show = false;
      return this;
    }

    public Program seat(Seat seat) {
      params.gravity = seat.getGravity();
      return this;
    }

    public Program threshold(int threshold){
      this.threshold = threshold;
      return this;
    }

    public static String TransferData(String serverUrl, String strd){
      try{
        //TransferData("http://10.168.13.23:8080/TestServer/vs?methodName=insertLogs&", paras);
        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setReadTimeout(90000);
        conn.setRequestMethod("GET");
        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        out.write(strd);
        out.flush();
        out.close();
        BufferedReader read = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line = "";
        String strRes = "";
        while( (line=read.readLine())!= null)
        {
          strRes += line;
        }
        read.close();
        return  strRes;
      }catch(Exception e){
        e.printStackTrace();
      }
      return "";
    }

      private  String getRunningActivityName(){
      ActivityManager activityManager=(ActivityManager)appContext.getSystemService(Context.ACTIVITY_SERVICE);
      String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
      return runningActivity;
    }

    private String getAppVersion() {
      String version=null;
      PackageInfo packageInfo =null;
      PackageManager pckMan = appContext.getPackageManager();
      try{
        packageInfo =  pckMan.getPackageInfo("com.cubic.autohome",0);
        version = packageInfo.versionName;
      }
      catch (PackageManager.NameNotFoundException e){
        e.printStackTrace();
      }
      return version;
    }

    private String getBrand(){
      return android.os.Build.MODEL;
    }

    private String getVersion(){
      return android.os.Build.VERSION.RELEASE;
    }

  }

}
