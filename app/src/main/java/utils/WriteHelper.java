package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.SyncStateContract;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Luffy on 30-Oct-17.
 */

public class WriteHelper {

    private static final String TAG = ReadHelper.class.getCanonicalName();


    public static void setQuestModeState(Context context, boolean isQuestModeOn)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        SharedPreferences.Editor editor = perf.edit();
        editor.putBoolean(Constants.QUEST_MODE_KEY, isQuestModeOn).apply();
        Log.d(TAG,"Is App Enabled Preference Saved: "+ isQuestModeOn);
    }

    public static void setFactsUnlocked(Context context)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        SharedPreferences.Editor editor = perf.edit();
        int unlocked = ReadHelper.getFactsUnlocked(context);
        editor.putInt(Constants.FACTS_UNLOCKED_KEY, ++unlocked).apply();
        Log.d(TAG,"Facts Unlocked Preference Saved: "+ unlocked);
    }



    public static synchronized void initCachedFactsMap(Context context)
    {
        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();
            FileOutputStream fos = context.openFileOutput(Constants.CACHED_FACTS_MAP, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            //Create an empty cached facts map
            List<String> facts = new LinkedList<>();
            Map<String, List<String>> factsMap = new LinkedHashMap<>();
            factsMap.put("math", facts);
            factsMap.put("date", facts);
            factsMap.put("trivia", facts);
            factsMap.put("year", facts);

            //Save the hash map
            oos.writeObject(factsMap);
            oos.flush();
            oos.close();
            setCacheInit(context, true);
            Log.d(TAG,"Cached Facts Map init with: "+factsMap);
        }
        catch (Exception e)
        {
            Log.e(TAG,"Cannot init cached hash map", e);
            setCacheInit(context, false);
        }
        finally {
            lock.unlock();
        }
    }


    public static synchronized void setCachedFactsMap(Context context, Map<String, List<String>> cachedMap)
    {
        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();
            FileOutputStream fos = context.openFileOutput(Constants.CACHED_FACTS_MAP, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            //Save the hash map
            oos.writeObject(cachedMap);
            oos.flush();
            oos.close();
            Log.d(TAG,"Cached Facts Map init with: "+ cachedMap);
        }
        catch (Exception e)
        {
            Log.e(TAG,"Cannot init cached hash map", e);
            setCacheInit(context, false);
        }
        finally {
            lock.unlock();
        }
    }


    public static void setCacheInit(Context context, boolean isInit)
    {
        SharedPreferences perf = context.getSharedPreferences(Constants.SHARED_PREF,0);
        SharedPreferences.Editor editor = perf.edit();
        int unlocked = ReadHelper.getFactsUnlocked(context);
        editor.putBoolean(Constants.CACHED_MAP_INITIALIZED, isInit).apply();
        Log.d(TAG,"Is Cache Map Init Preference Saved: "+ isInit);
    }


    public static synchronized void setCachedFacts(Context context, String category, String fact)
    {
        FileOutputStream fos;
        ReentrantLock lock = new ReentrantLock();
        try {
            lock.lock();
            fos = context.openFileOutput(Constants.CACHED_FACTS_MAP, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            Map<String, List<String>> cachedMap = ReadHelper.getCachedFactsMap(context);
            if(cachedMap != null)
            {
                List<String> facts = cachedMap.get(category);
                if(facts == null)
                {
                    facts = new LinkedList<>();
                    Log.d(TAG,"Cached Facts, list was null so created a new one");
                }
                else if(facts.size() < Constants.CACHED_QUEUE_MAX_SIZE)
                {
                    facts.add(fact);
                    Log.d(TAG,"Fact added");
                }
                else
                {
                    Log.d(TAG,"Cached queue already full, not writing");
                }

                cachedMap.put(category, facts);
            }
            else
            {
                cachedMap = new LinkedHashMap<String, List<String>>();
                Log.d(TAG,"Cache Map was null, so created a new one and saved");
            }
            oos.writeObject(cachedMap);
            oos.flush();
            oos.close();
        }
        catch (Exception e)
        {
            Log.e(TAG,"Cannot write to the cached queue", e);
        }
        finally {
            lock.unlock();
        }
    }

}
