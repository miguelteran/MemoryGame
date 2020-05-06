package com.matb.memorygame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
{
    private static String LOG_TAG = MainActivity.class.getSimpleName();

    private List<Integer> images;
    private List<Integer> matchedImages; // to keep track of images that have been matched
    private int playerAScore;
    private int playerBScore;
    private int numFlips;
    private int selectedImage;
    private int selectedViewId;
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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridView gridView = findViewById(R.id.products_grid);
        gridView.setAdapter(new ImagesAdapter(this.getApplicationContext(), this.images));
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                numFlips++;

                String player = playerATurn ? "A" : "B";
                String msg = player + " pressed image i: " + i;
                Log.d(LOG_TAG, msg);

                if (numFlips == 1)
                {
                    // Save first attempt
                    selectedImage = images.get(i);
                    selectedViewId = view.getId();
                }
                else
                {
                    if (selectedImage == images.get(i))
                    {
                        if (playerATurn)
                        {
                            playerAScore++;
                        }
                        else
                        {
                            playerBScore++;
                        }

                        Log.d(LOG_TAG, player + " matched!");
                        Toast.makeText(MainActivity.this, player + " matched!", Toast.LENGTH_LONG).show();

                        // Hide images that were matched
                        matchedImages.add(selectedImage);
                        ((ImagesAdapter) gridView.getAdapter()).setDisabledImages(matchedImages);
                        ((ImagesAdapter) gridView.getAdapter()).notifyDataSetChanged();
                    }

                    int score = playerATurn ? playerAScore : playerBScore;
                    Log.d(LOG_TAG, player + " score: " + score);

                    // It's turn for the other player
                    numFlips = 0;
                    playerATurn = !playerATurn;

                    player = playerATurn ? "A" : "B";
                    Log.d(LOG_TAG, "Switching to " + player);
                }
            }
        });
    }
}
