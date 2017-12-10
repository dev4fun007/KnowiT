package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Luffy on 30-Oct-17.
 */

public class ReadHelper {

    private static final String TAG = ReadHelper.class.getCanonicalName();


    public static Boolean getQuestModeState(Context context)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        Log.d(TAG,"Quest Mode Preference Read");
        return perf.getBoolean(Constants.QUEST_MODE_KEY, false);
    }

    public static int getFactsUnlocked(Context context)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        Log.d(TAG,"Facts Unlocked Preference Read");
        return perf.getInt(Constants.FACTS_UNLOCKED_KEY, 0);
    }


    public static Boolean getCacheMapInit(Context context)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        Log.d(TAG,"Cache map init Preference Read");
        return perf.getBoolean(Constants.CACHED_MAP_INITIALIZED, false);
    }


    public static synchronized Map<String, List<String>> getCachedFactsMap(Context context)
    {
        Map<String, List<String>> factsMap = null;
        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();
            FileInputStream fis = context.openFileInput(Constants.CACHED_FACTS_MAP);
            ObjectInputStream ois = new ObjectInputStream(fis);
            factsMap = (Map<String, List<String>>) ois.readObject();
            ois.close();
            Log.d(TAG,"facts Map: "+factsMap);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Unable to read", e);
        }
        finally {
            lock.unlock();
        }

        return factsMap;
    }


    public static synchronized String getCachedFact(Context context, String category)
    {
        String fact = "";
        Map<String, List<String>> factsMap;//new LinkedHashMap<String, List<String>>();
        ReentrantLock lock = new ReentrantLock();

        try {
            //Lock the access, other threads will wait
            lock.lock();
            FileInputStream fis = context.openFileInput(Constants.CACHED_FACTS_MAP);
            ObjectInputStream ois = new ObjectInputStream(fis);
            factsMap = (Map<String, List<String>>) ois.readObject();
            Log.d(TAG,"FactsMap: "+factsMap);
            List<String> facts = factsMap.get(category);
            ois.close();
            Log.d(TAG,"FactsMap List: "+facts);
            if(facts != null && facts.size() > 0)
            {
                fact = facts.remove(0);
                factsMap.put(category, facts);
            }

            //Set the new facts map, as the fact is already used
            WriteHelper.setCachedFactsMap(context, factsMap);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Unable to read", e);
        }
        finally {
            lock.unlock();
        }

        return fact;
    }

}
