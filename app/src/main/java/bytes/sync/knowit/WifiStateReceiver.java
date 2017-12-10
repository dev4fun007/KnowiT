package bytes.sync.knowit;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiStateReceiver extends BroadcastReceiver {

    private static final String TAG = WifiStateReceiver.class.getCanonicalName();

    private static final int JOB_ID = 2;


    @Override
    public void onReceive(Context context, Intent intent) {

        if("android.net.wifi.STATE_CHANGE".equals(intent.getAction()))
        {
            //Wifi state changed, check if still connected to wifi
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            if (networkInfo != null && networkInfo.isConnected()) {
                WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                if(wifiInfo.getSSID() != null && !wifiInfo.getSSID().isEmpty())
                {
                    Log.d(TAG,"Connected to wifi");
                    //Schedule job
                    JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(context.getPackageName(), FactsCachingService.class.getName()));
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
                    //Schedule the job
                    if(jobScheduler != null) {
                        boolean resultCode = jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS;
                        Log.d(TAG, "Job Schedule: " + resultCode);
                    }
                }
            }
            else
            {
                Log.d(TAG, "Not connected to wifi, canceling scheduled job");
                if(jobScheduler != null)
                    jobScheduler.cancel(JOB_ID);
            }
        }

    }
}
