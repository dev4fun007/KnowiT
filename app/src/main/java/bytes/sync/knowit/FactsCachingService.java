package bytes.sync.knowit;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

import utils.FetchResults;
import utils.ReadHelper;

public class FactsCachingService extends JobService {

    private static final String TAG = FactsCachingService.class.getCanonicalName();



    public FactsCachingService() {
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.d(TAG,"JobScheduler started the job");
        Context context = getApplicationContext();

        if(isConnected(context))
        {
            if(ReadHelper.getCacheMapInit(context)) {
                //Cache the results
                //Two trivia
                FetchResults.getRandomFacts("trivia", context);
                FetchResults.getRandomFacts("trivia", context);

                //Two Maths
                FetchResults.getRandomFacts("math", context);
                FetchResults.getRandomFacts("math", context);

                //Two Year
                FetchResults.getRandomFacts("year", context);
                FetchResults.getRandomFacts("year", context);

                //Two Date
                FetchResults.getRandomFacts("date", context);
                FetchResults.getRandomFacts("date", context);
            }
            else
            {
                jobFinished(jobParameters, true);
            }
        }
        else
        {
            jobFinished(jobParameters, true);
        }

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }



    private boolean isConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "Network available: "+isConnected);
        return isConnected;
    }

}
