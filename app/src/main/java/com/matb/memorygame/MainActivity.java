package com.matb.memorygame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.tekle.oss.android.animation.AnimationFactory;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    private static final long ANIMATION_TIME = 100;
    private static final long THREAD_DELAY = 600;

    private List<Integer> images;
    private List<Integer> matchedImages; // to keep track of images that have been matched
    private int playerAScore;
    private int playerBScore;
    private int numFlips;
    private int selectedImage;
    private int selectedPosition;
    private boolean playerATurn;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.images = ImageAssets.getImages(10);
        this.matchedImages = new ArrayList<>();
        this.playerAScore = 0;
        this.playerBScore = 0;
        this.numFlips = 0;
        this.playerATurn = true;
        this.selectedPosition = -1;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

                final String player = playerATurn ? "A" : "B";
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
                                if (playerATurn)
                                {
                                    playerAScore++;
                                }
                                else
                                {
                                    playerBScore++;
                                }

                                String msg = player + " matched!";
                                Log.d(LOG_TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();

                                matchedImages.add(selectedImage);
                                ((ImagesAdapter) gridView.getAdapter()).setDisabledImages(matchedImages);
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

                            viewFlipper.postDelayed(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    // It's turn for the other player
                                    numFlips = 0;
                                    playerATurn = !playerATurn;
                                    selectedPosition = -1;

                                    String newPlayer = playerATurn ? "A" : "B";
                                    Log.d(LOG_TAG, "Switching to " + newPlayer);
                                }
                            }, ANIMATION_TIME);
                        }
                    }, THREAD_DELAY);
                }
            }
        });
    }
}
