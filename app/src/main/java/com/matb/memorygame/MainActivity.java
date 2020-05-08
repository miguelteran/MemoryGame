package com.matb.memorygame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.tekle.oss.android.animation.AnimationFactory;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements VictoryDialog.VictoryDialogListener
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
    private static final String SELECTED_POSITION = "selectedPosition";
    private static final String TURN = "turn";
    private static final String START_NEW_GAME = "startNewGame";

    private List<Integer> images;
    private List<Integer> matchedImages;
    private int selectedPosition;
    private int playerAScore;
    private int playerBScore;
    private int numFlips;
    private int matchesToWin = 1;
    private boolean playerATurn;
    private boolean startNewGame;

    private TextView playerAHeaderTV;
    private TextView playerBHeaderTV;
    private TextView playerAScoreTV;
    private TextView playerBScoreTV;

    private void initializeGameState()
    {
        Log.d(LOG_TAG, "Initializing game state");
        this.images = ImageAssets.getImages(matchesToWin * 2);
        this.selectedPosition = -1;
        this.matchedImages = new ArrayList<>();
        this.playerAScore = 0;
        this.playerBScore = 0;
        this.numFlips = 0;
        this.playerATurn = true;
        this.startNewGame = false;
    }

    private void restoreGameState(Bundle savedState)
    {
        Log.d(LOG_TAG, "Restoring game state");
        this.images = savedState.getIntegerArrayList(IMAGES);
        this.selectedPosition = savedState.getInt(SELECTED_POSITION);
        this.matchedImages = savedState.getIntegerArrayList(MATCHED_IMAGES);
        this.playerAScore = savedState.getInt(PLAYER_A_SCORE);
        this.playerBScore = savedState.getInt(PLAYER_B_SCORE);
        this.numFlips = savedState.getInt(NUM_FLIPS);
        this.playerATurn = savedState.getBoolean(TURN);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null || savedInstanceState.getBoolean(START_NEW_GAME))
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
        this.playerAScoreTV.setText(String.format("%d", this.playerAScore));
        this.playerBScoreTV.setText(String.format("%d", this.playerBScore));

        // Make active player name and score bold
        setHeadersTypeface();

        final GridView gridView = findViewById(R.id.products_grid);
        gridView.setAdapter(new ImagesAdapter(this.getApplicationContext(), this.images, new ArrayList<Integer>(){{add(selectedPosition);}}, this.matchedImages));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l)
            {
                if (numFlips == 2 || i == selectedPosition) // first condition means previous animation hasn't finished
                {
                    return;
                }

                numFlips++;

                final ViewFlipper viewFlipper = view.findViewById(R.id.grid_item);
                AnimationFactory.flipTransition(viewFlipper,
                        AnimationFactory.FlipDirection.LEFT_RIGHT, ANIMATION_TIME);

                final String player = playerATurn ? getString(R.string.player_A) : getString(R.string.player_B);
                Log.d(LOG_TAG, player + " pressed image i: " + i);

                if (numFlips == 1)
                {
                    // Save first attempt
                    selectedPosition = i;
                    final List<Integer> positions = new ArrayList<>();
                    positions.add(selectedPosition);
                    viewFlipper.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            ((ImagesAdapter) gridView.getAdapter()).setSelectedPositions(positions);
                            ((ImagesAdapter) gridView.getAdapter()).notifyDataSetChanged();
                        }
                    }, ANIMATION_TIME);
                }
                else
                {
                    // Run in a delayed thread in order to not interrupt the animation
                    viewFlipper.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Integer image = images.get(selectedPosition);
                            if (image.equals(images.get(i))) // there was a match
                            {
                                increaseScore();

                                String msg = player + " matched!";
                                Log.d(LOG_TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                                matchedImages.add(image);
                                ((ImagesAdapter) gridView.getAdapter()).addDisabledImages(image);
                            }
                            else
                            {
                                // Flip card only if it is visible
                                if (selectedPosition >= gridView.getFirstVisiblePosition() &&
                                        selectedPosition <= gridView.getLastVisiblePosition())
                                {
                                    View v = gridView.getChildAt(selectedPosition -
                                            gridView.getFirstVisiblePosition());
                                    ViewFlipper item = v.findViewById(R.id.grid_item);
                                    AnimationFactory.flipTransition(item,
                                            AnimationFactory.FlipDirection.LEFT_RIGHT, ANIMATION_TIME);
                                }

                                AnimationFactory.flipTransition(viewFlipper,
                                        AnimationFactory.FlipDirection.LEFT_RIGHT, ANIMATION_TIME);
                            }

                            ((ImagesAdapter) gridView.getAdapter()).setSelectedPositions(new ArrayList<Integer>());
                            ((ImagesAdapter) gridView.getAdapter()).notifyDataSetChanged();

                            int score = playerATurn ? playerAScore : playerBScore;
                            Log.d(LOG_TAG, player + " score: " + score);

                            if (score == matchesToWin)
                            {
                                Log.d(LOG_TAG, player + " won");

                                Bundle bundle = new Bundle();
                                bundle.putString(getString(R.string.winner_key), player);

                                // Pop up dialog indicating which player won
                                DialogFragment victoryDialog = new VictoryDialog();
                                victoryDialog.setArguments(bundle);
                                victoryDialog.show(getSupportFragmentManager(), "victory");
                            }
                            else
                            {
                                viewFlipper.postDelayed(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        // It's turn for the other player
                                        numFlips = 0;
                                        playerATurn = !playerATurn;
                                        selectedPosition = -1;

                                        setHeadersTypeface();

                                        String newPlayer = playerATurn ? getString(R.string.player_A)
                                                : getString(R.string.player_B);
                                        Log.d(LOG_TAG, "Switching to " + newPlayer);
                                    }
                                }, ANIMATION_TIME);
                            }
                        }
                    }, THREAD_DELAY);
                }
            }
        });
    }

    @Override
    public void onNewGameClick(DialogFragment dialog)
    {
        dialog.dismiss();

        this.startNewGame = true;
        Log.d(LOG_TAG, "Recreating activity");
        this.recreate();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (!this.startNewGame)
        {
            Log.d(LOG_TAG, "Saving state");
            outState.putInt(PLAYER_A_SCORE, this.playerAScore);
            outState.putInt(PLAYER_B_SCORE, this.playerBScore);
            outState.putInt(NUM_FLIPS, this.numFlips);
            outState.putInt(SELECTED_POSITION, this.selectedPosition);
            outState.putBoolean(TURN, this.playerATurn);
            outState.putIntegerArrayList(IMAGES, (ArrayList) this.images);
            outState.putIntegerArrayList(MATCHED_IMAGES, (ArrayList) this.matchedImages);
        }

        outState.putBoolean(START_NEW_GAME, this.startNewGame);
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
}
