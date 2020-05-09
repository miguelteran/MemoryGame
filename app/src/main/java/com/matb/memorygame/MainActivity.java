package com.matb.memorygame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

/* External library for animation. Found: https://github.com/genzeb/flip.git */
import com.tekle.oss.android.animation.AnimationFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final long ANIMATION_TIME = 100;
    private static final long THREAD_DELAY = 600;

    // Keys for saved state
    private static final String PLAYER_A_SCORE = "playerAScore";
    private static final String PLAYER_B_SCORE = "playerBScore";
    private static final String NUM_FLIPS = "numFlips";
    private static final String IMAGES = "images";
    private static final String MATCHED_IMAGES = "matchedImages";
    private static final String SELECTED_POSITIONS = "selectedPositions";
    private static final String SELECTED_IMAGES = "selectedImages";
    private static final String TURN = "turn";
    private static final String MATCHES_TO_WIN = "matchesToWin";
    private static final String NUM_MATCHING_CARDS = "numMatchingCards";

    private List<Integer> images;
    private List<Integer> matchedImages;
    private List<Integer> selectedPositions;
    private int playerAScore;
    private int playerBScore;
    private int numFlips;
    private int matchesToWin;
    private int numCardsToMatch;
    private boolean playerATurn;
    private Set<Integer> selectedImages;

    private TextView playerAHeaderTV;
    private TextView playerBHeaderTV;
    private TextView playerAScoreTV;
    private TextView playerBScoreTV;
    private GridView gridView;

    private void initializeGameState()
    {
        Log.d(LOG_TAG, "Initializing game state");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.matchesToWin = Integer.parseInt(preferences.getString(getString(R.string.matches_to_win),
                                            getString(R.string.default_matches_to_win)));
        this.numCardsToMatch = Integer.parseInt(preferences.getString(getString(R.string.matching_cards),
                                                getString(R.string.default_matching_cards)));
        this.images = ImageAssets.getImages(this.matchesToWin * 2, this.numCardsToMatch);
        this.selectedPositions = new ArrayList<>();
        this.matchedImages = new ArrayList<>();
        this.selectedImages = new HashSet<>();
        this.playerAScore = 0;
        this.playerBScore = 0;
        this.numFlips = 0;
        this.playerATurn = true;
    }

    private void restoreGameState(Bundle savedState)
    {
        Log.d(LOG_TAG, "Restoring game state");
        this.images = savedState.getIntegerArrayList(IMAGES);
        this.selectedPositions = savedState.getIntegerArrayList(SELECTED_POSITIONS);
        this.selectedImages = new HashSet<>(savedState.getIntegerArrayList(SELECTED_IMAGES));
        this.matchedImages = savedState.getIntegerArrayList(MATCHED_IMAGES);
        this.matchesToWin = savedState.getInt(MATCHES_TO_WIN);
        this.numCardsToMatch = savedState.getInt(NUM_MATCHING_CARDS);
        this.playerAScore = savedState.getInt(PLAYER_A_SCORE);
        this.playerBScore = savedState.getInt(PLAYER_B_SCORE);
        this.numFlips = savedState.getInt(NUM_FLIPS);
        this.playerATurn = savedState.getBoolean(TURN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null)
        {
            initializeGameState();
        }
        else
        {
            restoreGameState(savedInstanceState);
        }

        setContentView(R.layout.activity_main);

        this.playerAScoreTV = findViewById(R.id.player_A_score);
        this.playerBScoreTV = findViewById(R.id.player_B_score);
        this.playerAHeaderTV = findViewById(R.id.player_A_header);
        this.playerBHeaderTV = findViewById(R.id.player_B_header);

        // Set initial scores in the UI
        setScores();

        // Make active player name and score bold
        setHeadersTypeface();

        ImagesAdapter adapter = new ImagesAdapter(this.getApplicationContext(),
                                                  this.images,
                                                  this.selectedPositions,
                                                  this.matchedImages);

        this.gridView = findViewById(R.id.products_grid);
        this.gridView.setAdapter(adapter);
        this.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
            {
                if (numFlips == numCardsToMatch || selectedPositions.contains(i) || matchedImages.contains(images.get(i))) // first condition means previous animation hasn't finished
                {
                    return;
                }

                numFlips++;

                final ViewFlipper viewFlipper = view.findViewById(R.id.grid_item);
                AnimationFactory.flipTransition(viewFlipper,
                        AnimationFactory.FlipDirection.LEFT_RIGHT, ANIMATION_TIME);

                final String player = playerATurn ? getString(R.string.player_A) : getString(R.string.player_B);
                Log.d(LOG_TAG, player + " pressed image i: " + i);

                addToSelectedPositions(viewFlipper, i);
                selectedImages.add(images.get(i));

                if (numFlips == numCardsToMatch)
                {
                    // Run in a delayed thread in order to not interrupt the animation
                    viewFlipper.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (selectedImages.size() == 1) // if all attempts chose the same image
                            {
                                increaseScore();

                                String msg = player + " " + getString(R.string.matched);
                                Log.d(LOG_TAG, msg);

                                int score = playerATurn ? playerAScore : playerBScore;
                                Log.d(LOG_TAG, player + " score: " + score);

                                if (score == matchesToWin)
                                {
                                    Log.d(LOG_TAG, player + " won");
                                    generateVictoryDialog(player);
                                    return;
                                }

                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                                Integer image = selectedImages.iterator().next();
                                matchedImages.add(image);
                                ((ImagesAdapter) gridView.getAdapter()).addDisabledImages(image);

                            }
                            else
                            {
                                // Flip card only if it is visible
                                for (Integer position : selectedPositions)
                                {
                                    if (position >= gridView.getFirstVisiblePosition() &&
                                            position <= gridView.getLastVisiblePosition())
                                    {
                                        View v = gridView.getChildAt(position -
                                                gridView.getFirstVisiblePosition());
                                        ViewFlipper item = v.findViewById(R.id.grid_item);
                                        AnimationFactory.flipTransition(item,
                                                AnimationFactory.FlipDirection.LEFT_RIGHT, ANIMATION_TIME);
                                    }
                                }
                            }

                            ((ImagesAdapter) gridView.getAdapter()).setSelectedPositions(new ArrayList<Integer>());
                            ((ImagesAdapter) gridView.getAdapter()).notifyDataSetChanged();

                            viewFlipper.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // It's turn for the other player
                                    numFlips = 0;
                                    playerATurn = !playerATurn;
                                    selectedPositions = new ArrayList<>();
                                    selectedImages = new HashSet<>();

                                    setHeadersTypeface();

                                    String newPlayer = playerATurn ? getString(R.string.player_A)
                                            : getString(R.string.player_B);
                                    Log.d(LOG_TAG, "Switching to " + newPlayer);
                                }
                            }, ANIMATION_TIME);

                        }
                    }, THREAD_DELAY);
                }
            }
        });

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.d(LOG_TAG, "Saving state");
        outState.putInt(PLAYER_A_SCORE, this.playerAScore);
        outState.putInt(PLAYER_B_SCORE, this.playerBScore);
        outState.putInt(NUM_FLIPS, this.numFlips);
        outState.putInt(MATCHES_TO_WIN, this.matchesToWin);
        outState.putInt(NUM_MATCHING_CARDS, this.numCardsToMatch);
        outState.putBoolean(TURN, this.playerATurn);
        outState.putIntegerArrayList(IMAGES, (ArrayList) this.images);
        outState.putIntegerArrayList(SELECTED_POSITIONS, (ArrayList) this.selectedPositions);
        outState.putIntegerArrayList(MATCHED_IMAGES, (ArrayList) this.matchedImages);
        outState.putIntegerArrayList(SELECTED_IMAGES, new ArrayList<>(this.selectedImages));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed()
    {
        // Create pop up window for exit confirmation
        new AlertDialog.Builder(this)
            .setMessage(R.string.exit_message)
            .setNegativeButton(android.R.string.no, null)
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    MainActivity.super.onBackPressed();
                }
            }).create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();
        if (itemId == R.id.settings_menu_item)
        {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }
        else if (itemId == R.id.shuffle_cards_menu_item)
        {
            shuffleCards();
            return true;
        }
        else if (itemId == R.id.restart_game_menu_item)
        {
            generateRestartGameDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generateRestartGameDialog()
    {
        new AlertDialog.Builder(this)
                .setMessage(R.string.restart_message)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startNewGame();
                    }
                }).create().show();
    }

    private void generateVictoryDialog(String player)
    {
        new AlertDialog.Builder(this)
                .setMessage(player + " " + getString(R.string.victory_message))
                .setPositiveButton(R.string.new_game_button, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startNewGame();
                    }
                }).create().show();
    }

    private void startNewGame()
    {
        Log.d(LOG_TAG, "Starting a new game");
        initializeGameState();
        setScores();
        setHeadersTypeface();
        ((ImagesAdapter) this.gridView.getAdapter()).setImageIds(this.images);
        ((ImagesAdapter) this.gridView.getAdapter()).setSelectedPositions(this.selectedPositions);
        ((ImagesAdapter) this.gridView.getAdapter()).setDisabledImages(this.matchedImages);
        ((ImagesAdapter) this.gridView.getAdapter()).notifyDataSetChanged();
    }

    private void setScores()
    {
        this.playerAScoreTV.setText(String.format("%d", this.playerAScore));
        this.playerBScoreTV.setText(String.format("%d", this.playerBScore));
    }

    private void increaseScore()
    {
        if (this.playerATurn)
        {
            this.playerAScore++;
            this.playerAScoreTV.setText(String.format("%d", this.playerAScore));
        }
        else
        {
            this.playerBScore++;
            this.playerBScoreTV.setText(String.format("%d", this.playerBScore));
        }
    }

    private void setHeadersTypeface()
    {
        if (this.playerATurn)
        {
            this.playerAHeaderTV.setTypeface(Typeface.DEFAULT_BOLD);
            this.playerAScoreTV.setTypeface(Typeface.DEFAULT_BOLD);
            this.playerBHeaderTV.setTypeface(Typeface.DEFAULT);
            this.playerBScoreTV.setTypeface(Typeface.DEFAULT);
        }
        else
        {
            this.playerAHeaderTV.setTypeface(Typeface.DEFAULT);
            this.playerAScoreTV.setTypeface(Typeface.DEFAULT);
            this.playerBHeaderTV.setTypeface(Typeface.DEFAULT_BOLD);
            this.playerBScoreTV.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }

    private void addToSelectedPositions(ViewFlipper viewFlipper, int i)
    {
        selectedPositions.add(i);
        viewFlipper.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                ((ImagesAdapter) gridView.getAdapter()).setSelectedPositions(selectedPositions);
                ((ImagesAdapter) gridView.getAdapter()).notifyDataSetChanged();
            }
        }, ANIMATION_TIME);
    }

    private void shuffleCards()
    {
        // Save which images were selected
        List<Integer> selectedImagesFromPositions = new ArrayList<>();
        for (Integer position : this.selectedPositions)
        {
            selectedImagesFromPositions.add(this.images.get(position));
        }

        Collections.shuffle(this.images);

        // Update positions
        this.selectedPositions = new ArrayList<>();
        for (Integer image : selectedImagesFromPositions)
        {
            this.selectedPositions.add(this.images.indexOf(image));
        }

        // Update adapter
        ((ImagesAdapter) this.gridView.getAdapter()).setImageIds(this.images);
        ((ImagesAdapter) this.gridView.getAdapter()).setSelectedPositions(this.selectedPositions);
        ((ImagesAdapter) this.gridView.getAdapter()).setDisabledImages(this.matchedImages);
        ((ImagesAdapter) this.gridView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
    {
        Log.d(LOG_TAG, "Settings changed");
        startNewGame();
    }

    @Override
    protected void onDestroy()
    {
        Log.d(LOG_TAG, "Destroying activity");
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
