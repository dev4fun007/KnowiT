package utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Luffy on 10-Dec-17.
 */

public class RealmHelper {

    private static final String TAG = ReadHelper.class.getCanonicalName();

    Realm realm;

    public RealmHelper(Realm realm) {
        this.realm = realm;
    }

    //WRITE
    public void save(final Facts facts)
    {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Facts fact = realm.copyToRealm(facts);
                Log.d(TAG,"Facts saved in Realm: "+fact.toString());
            }
        });
    }


    //READ
    public List<Facts> retrieve()
    {
        List<Facts> facts = new ArrayList<>();
        RealmResults<Facts> allFacts = realm.where(Facts.class).findAll();
        facts.addAll(allFacts);
        Log.d(TAG,"Facts from realm: "+facts.toString());
        return facts;
    }

}
