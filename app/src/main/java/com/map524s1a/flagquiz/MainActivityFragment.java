package com.map524s1a.flagquiz;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;


//import android.support.v4.app.DialogFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    // String used when logging error messages
    private static final String TAG = "FlagQuiz Activity";

    private int QUESTIONS_PER_QUIZ = 10;

    private List<String> fileNameList = new ArrayList<>();   // flag file names
    private List<String> quizCountriesList = new ArrayList<>(); // countries in current quiz
    private Set<String> regionsSet; // world regions in current quiz
    private String correctAnswer; // correct country for the current flag
    private int totalGuesses; // number of guesses made
    private int correctAnswers; // number of correct guesses
    private int guessRows; // number of rows displaying guess Buttons
    private SecureRandom random = new SecureRandom(); // used to randomize the quiz
    private Handler handler = new Handler(); // used to delay loading next flag
    private Animation shakeAnimation; // animation for incorrect guess

    private LinearLayout quizLinearLayout; // layout that contains the quiz
    private TextView questionNumberTextView; // shows current question #
    private ImageView flagImageView; // displays a flag
    private LinearLayout[] guessLinearLayouts; // rows of answer Buttons
    private TextView answerTextView; // displays correct answer
    private static Drawable defaultButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        System.out.println("Beginning of onCreateView method in MainActivityFragment Class");

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_main, container, false);

//        fileNameList = new ArrayList<>();   // diamond operator
//        quizCountriesList = new ArrayList<>();
//        random = new SecureRandom();
//        handler = new Handler();

        // load the shake animation that's used for incorrect answers
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.incorrect_shake);
//        shakeAnimation.setRepeatCount(3); // animaion repeats 3 times

        // get references to GUI components
        quizLinearLayout =
                (LinearLayout) view.findViewById(R.id.quizLinearLayout);
        questionNumberTextView =
                (TextView) view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView) view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[4];
        guessLinearLayouts[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        guessLinearLayouts[3] =
                (LinearLayout) view.findViewById(R.id.row4LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        // configure listeners for the guess Buttons
        for (LinearLayout row : guessLinearLayouts) {
            for (int column = 0; column < row.getChildCount(); column++) {
                Button button = (Button) row.getChildAt(column);
                defaultButton = button.getBackground();
                button.setOnClickListener(guessButtonListener);
            }
        }

        correctAnswers = 0;
        // set questionNumberTextView's text
        questionNumberTextView.setText(getString(R.string.question, 1, QUESTIONS_PER_QUIZ));


        System.out.println("End of onCreateView method in MainActivityFragment Class");
        return view;    // return the fragment's view for display
    }

    // update guessRows based on value in SharedPreferences
    public void updateGuessRows(SharedPreferences sharedPreferences) {
        System.out.println("Beginning of updateGuessRows method in MainActivityFragment Class");
        // get the number of guess buttons that should be displayed
        String choices = sharedPreferences.getString(MainActivity.CHOICES, null);
        guessRows = Integer.parseInt(choices) / 2;

        // hide all quess button LinearLayouts
        for (LinearLayout layout : guessLinearLayouts)
            layout.setVisibility(View.GONE);

        // display appropriate guess button LinearLayouts
        for (int row = 0; row < guessRows; row++)
            guessLinearLayouts[row].setVisibility(View.VISIBLE);

        System.out.println("End of updateGuessRows method in MainActivityFragment Class");
    }

    // update world regions for quiz based on values in SharedPreferences
    public void updateRegions(SharedPreferences sharedPreferences) {
        System.out.println("Beginning of updateRegions method in MainActivityFragment Class");
        regionsSet =
                sharedPreferences.getStringSet(MainActivity.REGIONS, null);
        System.out.println("End of updateRegions method in MainActivityFragment Class");
    }

    public void updateQuestions(SharedPreferences sharedPreferences) {
        QUESTIONS_PER_QUIZ = Integer.parseInt(sharedPreferences.getString(MainActivity.QUESTIONS, "10"));
    }

    // set up and start the next quiz
    public void resetQuiz() {
        System.out.println("Beginning of resetQuiz method in MainActivityFragment Class");
        // use AssetManager to get image file names for enabled regions
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear(); // empty list of image file names

        try {
            // loop through each region
            for (String region : regionsSet) {
                // get a list of all flag image files in this region
                String[] fileNames = assets.list(region);
                System.out.println(Arrays.toString(fileNames));

                for (String fileName : fileNames)
                    fileNameList.add(fileName.replaceAll(".png", ""));
            }
            System.out.println(fileNameList);
        } catch (IOException exception) {
            Log.e(TAG, "Error loading image file names", exception);
        }

        correctAnswers = 0; // reset the number of correct answers made
        totalGuesses = 0; // reset the total number of guesses the user made
        quizCountriesList.clear(); // clear prior list of quiz countries

        int flagCounter = 1;
        int numberOfFlags = fileNameList.size();
        System.out.println("numberOfFlags --> " + numberOfFlags);

        // add FLAGS_IN_QUIZ random file names to the quizCountriesList
        while (flagCounter <= QUESTIONS_PER_QUIZ) {
            int randomIndex = random.nextInt(numberOfFlags);

            // get the random file name
            String filename = fileNameList.get(randomIndex);

            // if the region is enabled and it hasn't already been chosen
            if (!quizCountriesList.contains(filename)) {
                quizCountriesList.add(filename); // add the file to the list
                ++flagCounter;
            }
        }

        System.out.println(quizCountriesList);

        loadNextFlag(); // start the quiz by loading the first flag
        System.out.println("End of resetQuiz method in MainActivityFragment Class");
    }

    // after the user guesses a correct flag, load the next flag
    private void loadNextFlag() {
        System.out.println("Beginning of loadNextFlag method in MainActivityFragment Class");
        // get file name of the next flag and remove it from the list
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText(""); // clear answerTextView

        // display current question number
        questionNumberTextView.setText(getString(
                R.string.question, (totalGuesses + 1), QUESTIONS_PER_QUIZ));

        // extract the region from the next image's name
        String region = nextImage.substring(0, nextImage.indexOf('-'));

        // use AssetManager to load next image from assets folder
        AssetManager assets = getActivity().getAssets();

        // get an InputStream to the asset representing the next flag
        // and try to use the InputStream
        try (InputStream stream =
                     assets.open(region + "/" + nextImage + ".png")) {
            // load the asset as a Drawable and display on the flagImageView
            Drawable flag = Drawable.createFromStream(stream, nextImage);
            flagImageView.setImageDrawable(flag);
            animate(false); // animate the flag onto the screen
        } catch (IOException exception) {
            Log.e(TAG, "Error loading " + nextImage, exception);
        }

        Collections.shuffle(fileNameList); // shuffle file names

        // put the correct answer at the end of fileNameList
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));

        // add 2, 4, 6 or 8 guess Buttons based on the value of guessRows
        for (int row = 0; row < guessRows; row++) {
            // place Buttons in currentTableRow
            for (int column = 0; column < guessLinearLayouts[row].getChildCount(); column++) {
                // get reference to Button to configure
                Button newGuessButton = (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);
                newGuessButton.setBackground(defaultButton);

                // get country name and set it as newGuessButton's text
                String filename = fileNameList.get((row * 2) + column);
                String countryName = getCountryName(filename);
                newGuessButton.setText(countryName);
            }
        }

        // randomly replace one Button with the correct answer
        int row = random.nextInt(guessRows); // pick random row
        int column = random.nextInt(2); // pick random column
        System.out.println("column --> " + column);
        LinearLayout randomRow = guessLinearLayouts[row]; // get the row
        String countryName = getCountryName(correctAnswer);
        ((Button) randomRow.getChildAt(column)).setText(countryName);
    }

    // parses the country flag file name and returns the country name
    private String getCountryName(String name) {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }

    // disables all the guess buttons on the screen
    // highlights the button corresponding to the correct answer
    public void disableAllGuessButtons() {
        for (int row = 0; row < guessRows; row++) {
            LinearLayout guessRow = guessLinearLayouts[row];
            for (int i = 0; i < guessRow.getChildCount(); i++){
                guessRow.getChildAt(i).setEnabled(false);
                Button temp = (Button)guessRow.getChildAt(i);
                System.out.println("Text on the button!: " + temp.getText() + ", Text of the answer " + getCountryName(correctAnswer));
                System.out.println("Condition: " + temp.getText().equals(getCountryName(correctAnswer)));
                if(temp.getText().equals(getCountryName(correctAnswer))){
                    temp.setBackgroundColor(Color.GREEN);
                }
            }
        }
    }

    // called when a guess Button is touched
    private OnClickListener guessButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Button guessButton = ((Button) view);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses; // increment number of guesses the user has made
            disableAllGuessButtons();

            if (guess.equals(answer)) {
                ++correctAnswers;

                // display correct answer in green text
                answerTextView.setText(answer + "!");
                answerTextView.setTextColor(
                        getResources().getColor(R.color.correctAnswer,
                                getContext().getTheme()));
                guessButton.setBackgroundColor(Color.GREEN);
            }
            else {
                flagImageView.startAnimation(shakeAnimation); // play shake

                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(getResources().getColor(
                        R.color.incorrectAnswer, getContext().getTheme()));

                guessButton.setBackgroundColor(Color.RED);

            }

            // if the user has correctly identified FLAGS_IN_QUIZ flags
            if (totalGuesses == QUESTIONS_PER_QUIZ) {
                // DialogFragment to display quiz stats and start new quiz
                @SuppressLint("ValidFragment")
                DialogFragment quizResults =
                        new DialogFragment() {
                            // create an AlertDialog and return it
                            @Override
                            public Dialog onCreateDialog(Bundle bundle) {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getActivity());
                                builder.setMessage(
                                        getString(R.string.results,
                                                (((double)correctAnswers)/((double)totalGuesses)*100)));
                                // "Reset Quiz" Button
                                builder.setPositiveButton(R.string.reset_quiz,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int id) {
                                                resetQuiz();
                                            }
                                        }
                                );
                                return builder.create(); // return the AlertDialog
                            }
                        };
                // use FragmentManager to display the DialogFragment
                quizResults.setCancelable(false);
                quizResults.show(getFragmentManager(), "quiz results");
            }
            else {
                // load the next flag after a 2-second delay
                handler.postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                animate(true); // animate the flag off the screen
                            }
                        }, 3000); // 3 second delay
            }
        }
    };

    // animates the entire quizLinearLayout on or off screen
    private void animate(boolean animateOut) {
        // prevent animation into the the UI for the first flag
        if (totalGuesses == 0)
            return;

        // calculate center x and center y
        int centerX = (quizLinearLayout.getLeft() +
                quizLinearLayout.getRight()) / 2;
        int centerY = (quizLinearLayout.getTop() +
                quizLinearLayout.getBottom()) / 2;

        // calculate animation radius
        int radius = Math.max(quizLinearLayout.getWidth(),
                quizLinearLayout.getHeight());

        Animator animator;

        // if the quizLinearLayout should animate out rather than in
        if (animateOut) {
            // create circular reveal animation
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, radius, 0);
            animator.addListener(
                    new AnimatorListenerAdapter() {
                        // called when the animation finishes
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loadNextFlag();
                        }
                    }
            );
        } else { // if the quizLinearLayout should animate in
            animator = ViewAnimationUtils.createCircularReveal(
                    quizLinearLayout, centerX, centerY, 0, radius);
        }

        animator.setDuration(500); // set animation duration to 500 ms
        animator.start(); // start the animation
    }

}
