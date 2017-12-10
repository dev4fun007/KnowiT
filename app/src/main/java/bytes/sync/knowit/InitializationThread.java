package bytes.sync.knowit;

import android.content.Context;
import android.util.Log;

import utils.ReadHelper;
import utils.WriteHelper;

/**
 * Created by Luffy on 04-Dec-17.
 */

public class InitializationThread implements Runnable {

    private static final String TAG = InitializationThread.class.getCanonicalName();

    private Context context;

    public InitializationThread(Context context)
    {
        this.context = context;
    }

    @Override
    public void run() {

        //Setting this thread to background, to prevent competition with the UI thread for resources
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        if(ReadHelper.getCacheMapInit(context) == false)
        {
            Log.d(TAG, "CachedMap not present, creating one");
            WriteHelper.initCachedFactsMap(context);
        }
        else
        {
            Log.d(TAG,"Facts Cached Map already present");
        }

    }
}
