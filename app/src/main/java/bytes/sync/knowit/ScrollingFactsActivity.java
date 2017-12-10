package bytes.sync.knowit;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.glomadrian.grav.GravView;
import com.tapadoo.alerter.Alerter;
import com.vlad1m1r.lemniscate.BernoullisProgressView;

import java.net.Inet4Address;

import am.appwise.components.ni.NoInternetDialog;
import utils.Constants;
import utils.FetchResults;
import utils.ReadHelper;
import utils.WriteHelper;

import static android.view.View.GONE;

public class ScrollingFactsActivity extends AppCompatActivity {

    private static final String TAG = ScrollingFactsActivity.class.getCanonicalName();

    TextView factsTextView, plusOneTextView;
    BernoullisProgressView progressView;
    GravView gravView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facts_details_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

/*        //Get +1 TextView
        plusOneTextView = (TextView) findViewById(R.id.plusOne_textView);
        plusOneTextView.setVisibility(GONE);

        //Get Confetti
        konfettiView = (KonfettiView) findViewById(R.id.viewKonfetti);
        konfettiView.setVisibility(GONE);*/

        factsTextView = (TextView) findViewById(R.id.facts_textView);
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
                //Hide progress bar
                YoYo.with(Techniques.FadeOut)
                        .duration(2500)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                //progressView.setVisibility(GONE);
                                gravView.setVisibility(GONE);
                            }
                        })
                        .playOn(gravView);

                factsTextView.setVisibility(View.VISIBLE);
                YoYo.with(Techniques.FadeIn)
                        .duration(2500)
                        .onEnd(new YoYo.AnimatorCallback() {
                            @Override
                            public void call(Animator animator) {
                                Log.d(TAG,"TextView Animation Ends, starting confetti");
                                //showKonfettiAndPlusOneTextView();
                            }
                        })
                        .playOn(factsTextView);

                //Save facts unlocked number
                WriteHelper.setFactsUnlocked(ScrollingFactsActivity.this);

            }
        });
        factsTextView.setVisibility(GONE);

        //progressView = (BernoullisProgressView) findViewById(R.id.progressBar);
        //progressView.setVisibility(GONE);
        gravView = (GravView) findViewById(R.id.progress_gravView);
        gravView.setVisibility(GONE);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String facts = factsTextView.getText().toString();
                if(facts == null || (facts != null && facts.isEmpty())) {
                    Snackbar.make(view, "No facts to share.", Snackbar.LENGTH_LONG).show();
                    return;
                }
                shareIntent();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    /*private void showKonfettiAndPlusOneTextView()
    {
        konfettiView.setVisibility(View.VISIBLE);
        konfettiView.build()
                .addColors(Color.YELLOW, Color.GREEN, Color.RED)
                .setDirection(0.0, 359.0)
                .setSpeed(1f, 5f)
                .setFadeOutEnabled(true)
                .setTimeToLive(2000L)
                .addShapes(Shape.RECT)
                .addSizes(new Size(6, 3f))
                .setPosition(konfettiView.getX() + konfettiView.getWidth()/ 2, konfettiView.getY() + konfettiView.getHeight()/ 2)
                .burst(100);

        int pos[] = new int[2];
        konfettiView.getLocationOnScreen(pos);
        Log.d(TAG,"X: "+pos[0]+" Y: "+pos[1]);
        Log.d(TAG,"X Width: "+konfettiView.getMeasuredWidth()+" Y Height: "+konfettiView.getMeasuredHeight());

        plusOneTextView.setVisibility(View.VISIBLE);
        YoYo.with(Techniques.FadeIn)
                .duration(500)
                .playOn(plusOneTextView);

        YoYo.with(Techniques.FadeOut)
                .duration(3000)
                .onEnd(new YoYo.AnimatorCallback() {
                    @Override
                    public void call(Animator animator) {
                        plusOneTextView.setVisibility(View.GONE);
                        konfettiView.setVisibility(GONE);
                    }
                })
                .playOn(plusOneTextView);


        Log.d(TAG,"Konfetti Started in burst mode");

    }*/


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

            //Show progress bar
            //progressView.setVisibility(View.VISIBLE);
            gravView.setVisibility(View.VISIBLE);

            //Get url related details
            Intent intent = getIntent();
            Bundle bundle = intent.getExtras();
            boolean isQuestModeOn = bundle.getBoolean(Constants.QUEST_MODE);
            String digitOrDate = bundle.getString(Constants.DIGIT_OR_DATE);
            String category = bundle.getString(Constants.CATEGORY);

            if (!isQuestModeOn) {
                //Check the cache first, before hitting the api
                String facts = ReadHelper.getCachedFact(getApplicationContext(), category);
                if(facts != null && !facts.isEmpty())
                {
                    factsTextView.setText(facts);
                    Log.d(TAG,"Read from cache");
                }
                else
                {
                    FetchResults.getRandomFacts(category, this, factsTextView);
                    Log.d(TAG,"Read using API");
                }
            } else {
                FetchResults.getQuestFacts(category, digitOrDate, this, factsTextView);
            }
        }
    }


    private boolean isConnected(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        Log.d(TAG, "Network available: "+isConnected);
        return isConnected;
    }


    private void shareIntent()
    {
        String facts = factsTextView.getText().toString();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, facts);
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, facts));
        Log.d(TAG," content shared: "+facts);
    }





}
