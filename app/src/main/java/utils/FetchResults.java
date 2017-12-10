package utils;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Luffy on 29-Oct-17.
 */

public class FetchResults {

    private static final String TAG = FetchResults.class.getCanonicalName();

    private static String BASIC_URL = "http://numbersapi.com/random/{category}";
    private static String QUEST_URL = "http://numbersapi.com/{digitOrDate}/{category}";

    private static Map<String, List<String>> cacheMap = new LinkedHashMap<>();
    private static AtomicInteger atomicIntegerRequestCounter = new AtomicInteger(0);
    private static int MAX_CACHE_REQUEST_QUEUE = 8;


    public static synchronized void getRandomFacts(String category, Context context, final TextView textView) {

        final String url = BASIC_URL.replace("{category}", category);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: "+response);
                        textView.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("Oops, we ran out of facts, please try again");
                Log.d(TAG, "Error getting result", error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public static synchronized void getRandomFacts(final String category, final Context context) {

        final String url = BASIC_URL.replace("{category}", category);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response to cache: "+response);
                        //Save to cache

                        if(cacheMap.containsKey(category))
                        {
                            //Append the entry
                            List<String> list = cacheMap.get(category);
                            list.add(response);
                            cacheMap.put(category, list);
                        }
                        else
                        {
                            //Add the entry
                            List<String> list = new ArrayList<>();
                            list.add(response);
                            cacheMap.put(category, list);
                        }

                        if(atomicIntegerRequestCounter.incrementAndGet() >= MAX_CACHE_REQUEST_QUEUE)
                        {
                            Log.d(TAG,"AtomicIntegerCOunter: "+atomicIntegerRequestCounter.get());
                            //Save the cacheMap, as it is full now
                            WriteHelper.setCachedFactsMap(context, cacheMap);

                            //reset the static fields
                            atomicIntegerRequestCounter.set(0);
                            cacheMap.clear();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error getting result", error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    public static synchronized void getQuestFacts(String category, String number, Context context, final TextView textView)
    {
        final String url = QUEST_URL.replace("{category}", category).replace("{digitOrDate}", number);
        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response: "+response);
                        textView.setText(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView.setText("Oops, we ran out of facts, please try again");
                Log.d(TAG, "Error getting result", error);
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}
