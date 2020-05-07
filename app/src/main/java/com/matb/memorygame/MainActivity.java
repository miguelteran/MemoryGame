package com.matb.memorygame;

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

    private List<Integer> images;
    private int playerAScore;
    private int playerBScore;
    private int numFlips;
    private int selectedImage;
    private int selectedPosition;
    private int matchesToWin = 10;
    private boolean playerATurn;

    private TextView playerAHeaderTV;
    private TextView playerBHeaderTV;
    private TextView playerAScoreTV;
    private TextView playerBScoreTV;

    public void initializeGame()
    {
        this.images = ImageAssets.getImages(matchesToWin * 2);
        this.playerAScore = 0;
        this.playerBScore = 0;
        this.numFlips = 0;
        this.playerATurn = true;
        this.selectedPosition = -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        initializeGame();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.playerAScoreTV = findViewById(R.id.player_A_score);
        this.playerBScoreTV = findViewById(R.id.player_B_score);
        this.playerAHeaderTV = findViewById(R.id.player_A_header);
        this.playerBHeaderTV = findViewById(R.id.player_B_header);

        // Set initial scores in the UI
        this.playerAScoreTV.setText(String.format("%d", this.playerAScore));
        this.playerBScoreTV.setText(String.format("%d", this.playerBScore));

        // Make active player name and score bold
        this.playerAHeaderTV.setTypeface(Typeface.DEFAULT_BOLD);
        this.playerAScoreTV.setTypeface(Typeface.DEFAULT_BOLD);

        final GridView gridView = findViewById(R.id.products_grid);
        gridView.setAdapter(new ImagesAdapter(this.getApplicationContext(), this.images));
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
                    selectedImage = images.get(i);
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
                            if (selectedImage == images.get(i)) // there was a match
                            {
                                increaseScore();

                                String msg = player + " matched!";
                                Log.d(LOG_TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();

                                ((ImagesAdapter) gridView.getAdapter()).addDisabledImages(selectedImage);
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

                                        changeHeadersTypeface();

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

        Log.d(LOG_TAG, "Recreating activity");
        this.recreate();
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

    private void changeHeadersTypeface()
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
