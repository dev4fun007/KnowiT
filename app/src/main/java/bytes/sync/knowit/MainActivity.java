package bytes.sync.knowit;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.fujiyuu75.sequent.Animation;
import com.fujiyuu75.sequent.Direction;
import com.fujiyuu75.sequent.Sequent;
import com.tapadoo.alerter.Alerter;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import java.util.Calendar;

import utils.Constants;
import utils.ReadHelper;
import utils.WriteHelper;

public class MainActivity extends AppCompatActivity implements CalendarDatePickerDialogFragment.OnDateSetListener, CompoundButton.OnCheckedChangeListener{

    private static final String TAG = MainActivity.class.getCanonicalName();
    private static final String FRAG_TAG_DATE_PICKER = "fragment_date_picker_name";

    SwitchCompat switchCompat;
    ImageButton triviaButton, mathButton, dateButton, yearButton;
    private boolean isQuestModeOn = false;

    ImageView factsLeaderImage;
    TextView factsNumberTextView, factsLevelTextView;

    private JobScheduler jobScheduler;

    FloatingActionButton emailFab, twitterFab, facebookFab;

    RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchCompat = (SwitchCompat) findViewById(R.id.quest_switch);
        switchCompat.setOnCheckedChangeListener(this);

        relativeLayout = (RelativeLayout) findViewById(R.id.bottom_action_button_relativeLayout);

        factsLevelTextView = (TextView) findViewById(R.id.facts_leader_textView);
        factsNumberTextView = (TextView) findViewById(R.id.facts_leader_unlocked_info_textView);
        factsLeaderImage = (ImageView) findViewById(R.id.facts_leader_imageView);


        twitterFab = (FloatingActionButton) findViewById(R.id.twitter_fab);
        twitterFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl(Constants.TWITTER_URL);
            }
        });
        facebookFab = (FloatingActionButton) findViewById(R.id.facebook_fab);
        facebookFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrl(Constants.FACEBOOK_URL);
            }
        });
        emailFab = (FloatingActionButton) findViewById(R.id.email_fab);
        emailFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dev4fun007@gmail.com"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "[KnowiT] Suggestions or Error Report");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });



        triviaButton = (ImageButton) findViewById(R.id.trivia_imageView);
        triviaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isQuestModeOn)
                    startFactsActivity("trivia", "");
                else
                {
                    showDialogAndStartActivity("trivia");
                }
            }
        });

        mathButton = (ImageButton) findViewById(R.id.math_imageView);
        mathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isQuestModeOn)
                    startFactsActivity("math", "");
                else
                {
                    showDialogAndStartActivity("math");
                }
            }
        });

        dateButton = (ImageButton) findViewById(R.id.date_imageView);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isQuestModeOn)
                    startFactsActivity("date", "");
                else
                {
                    //Get the month and day value   MM/DD
                    CalendarDatePickerDialogFragment cdp = new CalendarDatePickerDialogFragment()
                            .setOnDateSetListener(MainActivity.this)
                            .setFirstDayOfWeek(Calendar.SUNDAY)
                            .setDoneText("Done")
                            .setCancelText("Cancel");

                    cdp.show(getSupportFragmentManager(), FRAG_TAG_DATE_PICKER);

                }
            }
        });

        yearButton = (ImageButton) findViewById(R.id.year_imageView);
        yearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isQuestModeOn)
                    startFactsActivity("year", "");
                else
                {
                    showDialogAndStartActivity("year");
                }
            }
        });


        jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), FactsCachingService.class.getName()));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        //Schedule the job
        boolean resultCode = jobScheduler.schedule(builder.build()) == JobScheduler.RESULT_SUCCESS;
        Log.d(TAG, "Job Schedule: "+resultCode);


        //Create a CachedFacts Map on separate thread
        new Thread(new InitializationThread(this)).start();
    }


    private void showDialogAndStartActivity(final String category)
    {
        //QuestMode is on, show input dialog
        new LovelyTextInputDialog(MainActivity.this)
                .setTopColorRes(R.color.colorAccent)
                .setTitle(R.string.dialogTitle)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setInputFilter(R.string.text_input_error_message, new LovelyTextInputDialog.TextFilter() {
                    @Override
                    public boolean check(String text) {
                        return text.matches("\\d+");
                    }
                })
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        Log.d(TAG, "Dialog Input: "+text);
                        if(!text.matches("\\d+"))
                        {
                            Alerter.create(MainActivity.this)
                                    .setTitle(R.string.invalidNumber)
                                    .setText(R.string.text_input_error_message)
                                    .setDuration(2000)
                                    .setBackgroundColorRes(R.color.colorPrimary)
                                    .show();
                        }
                        //Start the activity in Quest Mode
                        startFactsActivity(category, text);
                    }
                })
                .show();
    }



    @Override
    public void onResume()
    {
        super.onResume();

        isQuestModeOn = ReadHelper.getQuestModeState(this);
        switchCompat.setChecked(isQuestModeOn);

        int factsUnlocked = ReadHelper.getFactsUnlocked(this);
        setFactsLevel(factsUnlocked);

        CalendarDatePickerDialogFragment calendarDatePickerDialogFragment = (CalendarDatePickerDialogFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAG_TAG_DATE_PICKER);
        if (calendarDatePickerDialogFragment != null) {
            calendarDatePickerDialogFragment.setOnDateSetListener(this);
        }

        //Show bottom buttons animation
        Sequent.origin(relativeLayout).duration(Constants.BOTTOM_FAB_DURATION).delay(Constants.BOTTOM_FAB_DELAY)
               .flow(Direction.FORWARD).anim(this, Animation.BOUNCE_IN)
               .start();
    }



    private void startFactsActivity(String category, String digitOrDate)
    {
        //start facts activity with trivia param
        Intent intent = new Intent(MainActivity.this, FactiT.class);
        intent.putExtra(Constants.CATEGORY, category);
        if(isQuestModeOn)
        {
            //Quest mode is on, add digit or date field as well
            intent.putExtra(Constants.QUEST_MODE, true);
            intent.putExtra(Constants.DIGIT_OR_DATE, digitOrDate);
        }
        else
        {
            intent.putExtra(Constants.QUEST_MODE, false);
        }
        startActivity(intent);
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        isQuestModeOn = isChecked;
        Log.d(TAG, "Quest mode state: " + isQuestModeOn);
        WriteHelper.setQuestModeState(MainActivity.this, isChecked);
    }


    private void setFactsLevel(int factsUnlocked)
    {
        factsNumberTextView.setText(String.valueOf(factsUnlocked));

        if(factsUnlocked >= 0 && factsUnlocked <5)
        {
            factsLeaderImage.setImageResource(R.drawable.beginner);
            factsLevelTextView.setText(R.string.beginner);
        }
        else if(factsUnlocked >= 5 && factsUnlocked <10)
        {
            factsLeaderImage.setImageResource(R.drawable.expert);
            factsLevelTextView.setText(R.string.expert);
        }
        else if(factsUnlocked >= 10)
        {
            factsLeaderImage.setImageResource(R.drawable.wizard);
            factsLevelTextView.setText(R.string.wizard);
        }
    }



    private void openUrl(String url)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if(browserIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(browserIntent);
        }
        else
        {
            Alerter.create(MainActivity.this)
                    .setDuration(2000)
                    .setTitle("Oh No")
                    .setText("You might have to install a web browser!")
                    .show();
        }
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_favorite:
                //Open the recycler view activity
                Intent intent = new Intent(this, FavouriteActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        Log.d(TAG, "Date set, mm: "+monthOfYear+" day: "+dayOfMonth);
        //Now start the facts activity
        startFactsActivity("date", ++monthOfYear+"/"+dayOfMonth);
    }
}
