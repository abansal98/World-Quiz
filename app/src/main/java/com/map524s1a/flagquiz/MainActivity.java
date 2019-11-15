package com.map524s1a.flagquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private boolean phoneDevice = true; // used to force portrait mode
    private boolean preferencesChanged = true; // did preferences change?

    // keys for reading data from SharedPreferences
    public static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regionsToInclude";
    public static final String QUESTIONS = "pref_number_of_ques";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Beginning of onCreate method in MainActivity Class");

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // set default values in the app's SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register listener for SharedPreferences changes
        PreferenceManager.getDefaultSharedPreferences(this).
                registerOnSharedPreferenceChangeListener(
                        preferencesChangeListener);

        // determine screen size
        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        // if device is a tablet, set phoneDevice to false
        if (screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE)
            phoneDevice = false; // not a phone-sized device

        // if running on phone-sized device, allow only portrait orientation
        if (phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        System.out.println("End of onCreate method in MainActivity Class");
    }

    // called after onCreate completes execution
    protected void onStart() {
        System.out.println("Beginning of onStart method in MainActivity Class");
        super.onStart();

        if (preferencesChanged) {
            // now that the default preferences have been set,
            // initialize MainActivityFragment and start the quiz
            MainActivityFragment quizFragment = (MainActivityFragment)
                    getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateQuestions(
                    PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChanged = false;
        }
        System.out.println("End of onStart method in MainActivity Class");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        System.out.println("Beginning of onCreateOptionsMenu method in MainActivity Class");

        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            System.out.println("End of onCreateOptionsMenu method in MainActivity Class");
            return true;
        }
        else {
//            getMenuInflater().inflate(R.menu.menu_main, menu);
            System.out.println("End of onCreateOptionsMenu method in MainActivity Class");
            return true;
        }

        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        System.out.println("End of onCreateOptionsMenu method in MainActivity Class");
//        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println("Beginning of onOptionsItemSelected method in MainActivity Class");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);
        System.out.println("End of onOptionsItemSelected method in MainActivity Class");
        return super.onOptionsItemSelected(item);
    }
	
	   // listener for changes to the app's SharedPreferences
   private OnSharedPreferenceChangeListener preferencesChangeListener =
      new OnSharedPreferenceChangeListener() {
         // called when the user changes the app's preferences
         @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            System.out.println("Beginning of onSharedPreferenceChanged method in MainActivity Class");
            preferencesChanged = true; // user changed app setting

            MainActivityFragment quizFragment = (MainActivityFragment)
               getSupportFragmentManager().findFragmentById(
                  R.id.quizFragment);

            if (key.equals(CHOICES)) { // # of choices to display changed
               quizFragment.updateGuessRows(sharedPreferences);
               quizFragment.resetQuiz();
            }
            else if (key.equals(REGIONS)) { // regions to include changed
               Set<String> regions =
                  sharedPreferences.getStringSet(REGIONS, null);

               if (regions != null && regions.size() > 0) {
                  quizFragment.updateRegions(sharedPreferences);
                  quizFragment.resetQuiz();
               }
               else {
                  // must select one region--set North America as default
                  SharedPreferences.Editor editor =
                     sharedPreferences.edit();
                  regions.add(getString(R.string.default_region));
                  editor.putStringSet(REGIONS, regions);
                  editor.apply();

                  Toast.makeText(MainActivity.this,
                     R.string.default_region_message,
                     Toast.LENGTH_SHORT).show();
               }
            }

            Toast.makeText(MainActivity.this,
               R.string.restarting_quiz,
               Toast.LENGTH_SHORT).show();

            System.out.println("End of onSharedPreferenceChanged method in MainActivity Class");
         }
      };
}
