package bytes.sync.knowit;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import utils.Facts;
import utils.FactsAdapter;
import utils.RealmHelper;

public class FavouriteActivity extends AppCompatActivity {

    private static final String TAG = FavouriteActivity.class.getCanonicalName();

    RecyclerView favRecyclerView;
    Realm realm;
    FactsAdapter factsAdapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        favRecyclerView = (RecyclerView) findViewById(R.id.fac_recyclerView);
        layoutManager = new LinearLayoutManager(this);
        favRecyclerView.setHasFixedSize(true);
        favRecyclerView.setLayoutManager(layoutManager);

        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(realmConfiguration);

        //Retrieve from realm
        RealmHelper realmHelper = new RealmHelper(realm);
        List<Facts> factsList = realmHelper.retrieve();
        //Bind the RecyclerView to the adapter
        factsAdapter = new FactsAdapter(this, factsList);
        favRecyclerView.setAdapter(factsAdapter);

        Log.d(TAG,"Recycler View Setup");

    }
}
