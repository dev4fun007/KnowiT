package bytes.sync.knowit;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.glomadrian.grav.GravView;
import com.github.nisrulz.sensey.Sensey;
import com.github.nisrulz.sensey.ShakeDetector;
import com.github.nisrulz.sensey.WristTwistDetector;
import com.tapadoo.alerter.Alerter;

import am.appwise.components.ni.NoInternetDialog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import utils.Constants;
import utils.Facts;
import utils.FetchResults;
import utils.ReadHelper;
import utils.RealmHelper;
import utils.WriteHelper;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FactiT extends AppCompatActivity {

    private static final String TAG = FactiT.class.getCanonicalName();

    GravView gravView;
    TextView factsTextView, factsTextView2;
    CardView cardView1, cardView2;
    ImageButton nextImageButton, favImageButton, shareImageButton;

    private boolean isFirstCardViewTurn = true;
    private String category;
    private boolean isQuestModeEnabled = false;

    ShakeDetector.ShakeListener shakeListener;
    Vibrator vibrator;

    Realm realm;
    static String currentFact = "";

    LinearLayout bottomBarLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fact_it);
        Toolbar toolbar = (Toolbar) findViewById(R.id.factit_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        cardView1 = (CardView) findViewById(R.id.info_cardView_1);
        cardView2 = (CardView) findViewById(R.id.info_cardView_2);
        bottomBarLinearLayout = (LinearLayout) findViewById(R.id.bottomBar_linearLayout);

        //Setup Realm
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().build();
        realm = Realm.getInstance(realmConfiguration);
        final RealmHelper realmHelper = new RealmHelper(realm);

        favImageButton = (ImageButton) findViewById(R.id.fav_imageButton);
        favImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Add this fact to the favorites list
                Alerter.create(FactiT.this).setBackgroundColorRes(R.color.colorPrimaryDark)
                        .enableSwipeToDismiss()
                        .setIcon(R.drawable.ic_favorite_white_48dp)
                        .setDuration(2000)
                        .setTitle("Added to favorite")
                        .setText("Wow, this fact did blow your mind")
                        .show();

                //Saving to realm
                Facts fact = new Facts();
                fact.setFact(currentFact);
                fact.setCategory(category);

                //Save to realm
                realmHelper.save(fact);
                Log.d(TAG,"Saved the fact: "+fact.toString());
            }
        });


        nextImageButton = (ImageButton) findViewById(R.id.next_imageButton);
        nextImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change the cardview turn
                isFirstCardViewTurn = !isFirstCardViewTurn;

                //Fetch the next fact
                fetchFacts();
            }
        });


        shareImageButton = (ImageButton) findViewById(R.id.share_imageButton);
        shareImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareIntent();
            }
        });



        //Facts text view
        factsTextView = (TextView) findViewById(R.id.facts_textView_1);
        factsTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Text has been changed
                Log.d(TAG, "New Text in facts text view: "+editable.toString());

                showFactsWithAnimation(1);  //Fact View index 1
                //Save facts unlocked number
                WriteHelper.setFactsUnlocked(FactiT.this);
                currentFact = editable.toString();
            }
        });
        factsTextView.setVisibility(VISIBLE);


        //Facts text view
        factsTextView2 = (TextView) findViewById(R.id.facts_textView_2);
        factsTextView2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //Text has been changed
                Log.d(TAG, "New Text in facts text view: "+editable.toString());

                showFactsWithAnimation(2);  //Fact View index 2

                //Save facts unlocked number
                WriteHelper.setFactsUnlocked(FactiT.this);
                currentFact = editable.toString();
            }
        });
        factsTextView2.setVisibility(VISIBLE);



        //Grav for the progress indicator
        gravView = (GravView) findViewById(R.id.progress_gravView);
        gravView.setVisibility(GONE);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        Sensey.getInstance().init(this);
        shakeListener = new ShakeDetector.ShakeListener() {
            @Override
            public void onShakeDetected() {
                Log.d(TAG,"Shake detected");
                //Change the cardview turn
                isFirstCardViewTurn = !isFirstCardViewTurn;

                if(!isQuestModeEnabled) {
                    //Fetch the next fact
                    fetchFacts();
                    vibrator.vibrate(100);
                }
            }

            @Override
            public void onShakeStopped() {
                Log.d(TAG,"Shake stopped");
            }
        };
    }




    @Override
    public void onStart()
    {
        super.onStart();

        //Check for network
        if(!isConnected(this))
        {
            new NoInternetDialog.Builder(this).build();
        }
        else {
            init();
        }
    }


    @Override
    public void onResume()
    {
        super.onResume();
        //Add Shake listener
        Sensey.getInstance().startShakeDetection(shakeListener);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        //Remove Shake listener
        Sensey.getInstance().stopShakeDetection(shakeListener);
    }




    private void fetchFacts()
    {
        //Show progress bar
        gravView.setVisibility(View.VISIBLE);
        if(category == null || category.isEmpty())
        {
            if(isFirstCardViewTurn)
                factsTextView.setText("Sorry, we ran out of facts, try again in a while");
            else
                factsTextView2.setText("Sorry, we ran out of facts, try again in a while");

            View view = isFirstCardViewTurn ? cardView1:cardView2;
            view.setVisibility(VISIBLE);
            YoYo.with(Techniques.SlideInLeft)
                    .duration(Constants.SLIDE_IN_ANIMATION_DURATION)
                    .playOn(isFirstCardViewTurn ? cardView1:cardView2);
            return;
        }

        //Check the cache first, before hitting the api
        String facts = ReadHelper.getCachedFact(getApplicationContext(), category);
        if(facts != null && !facts.isEmpty())
        {
            if(isFirstCardViewTurn)
                factsTextView.setText(facts);
            else
                factsTextView2.setText(facts);
            Log.d(TAG,"Read from cache");
        }
        else
        {
            if(isFirstCardViewTurn)
                FetchResults.getRandomFacts(category, this, factsTextView);
            else
                FetchResults.getRandomFacts(category, this, factsTextView2);
            Log.d(TAG,"Read using API");
        }
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


    private void shareIntent()
    {
        String facts = "";
        if(isFirstCardViewTurn)
            facts = factsTextView.getText().toString();
        else
            facts = factsTextView2.getText().toString();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, facts);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, facts));
        Log.d(TAG," content shared: "+facts);
    }



    private void init()
    {
        cardView1.setVisibility(GONE);
        cardView2.setVisibility(GONE);

        //Get url related details
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        boolean isQuestModeOn = bundle.getBoolean(Constants.QUEST_MODE);
        String digitOrDate = bundle.getString(Constants.DIGIT_OR_DATE);
        String category = bundle.getString(Constants.CATEGORY);
        this.category = category;
        isQuestModeEnabled = isQuestModeOn;
        if (!isQuestModeOn) {
            bottomBarLinearLayout.setWeightSum(3f);
            nextImageButton.setVisibility(View.VISIBLE);
            fetchFacts();
        } else {
            gravView.setVisibility(VISIBLE);
            //Quest mode is on, so hide the next button
            nextImageButton.setVisibility(GONE);
            bottomBarLinearLayout.setWeightSum(2f);

            if(isFirstCardViewTurn)
                FetchResults.getQuestFacts(category, digitOrDate, this, factsTextView);
            else
                FetchResults.getQuestFacts(category, digitOrDate, this, factsTextView2);

            //Stop Shake listener for quest mode
            Sensey.getInstance().stopShakeDetection(shakeListener);
        }
    }


    private void showFactsWithAnimation(int factView)
    {
        View showView = null;
        View hideView = null;

        if(factView == 1)
        {
            showView = cardView1;
            hideView = cardView2;
        }
        else if(factView ==2)
        {
            showView = cardView2;
            hideView = cardView1;
        }

        //Hide the other view
        if(hideView != null && hideView.getVisibility() == VISIBLE)
        {
            hideView(hideView);
        }

        //Show the facts card view
        showView(showView);
    }


    private void hideView(final View hideView)
    {
        //Slide away this cardview first before showing
        YoYo.with(Techniques.SlideOutRight)
                .duration(Constants.SLIDE_OUT_ANIMATION_DURATION)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        hideView.setVisibility(GONE);
                    }
                })
                .playOn(hideView);
        Log.d(TAG,"View slided out to right");
    }


    private void showView(final View showView)
    {
        //Show the first card view
        showView.setVisibility(VISIBLE);
        YoYo.with(Techniques.SlideInLeft)
                .duration(Constants.SLIDE_IN_ANIMATION_DURATION)
                .playOn(showView);
        Log.d(TAG,"View Slided into view, and then hiding progress bar");

        //Hide the grav progress bar
        YoYo.with(Techniques.FadeOutDown)
                .duration(Constants.SLIDE_OUT_ANIMATION_DURATION)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        gravView.setVisibility(GONE);
                    }
                })
                .playOn(gravView);
    }
}
