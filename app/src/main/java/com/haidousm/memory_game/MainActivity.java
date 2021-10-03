package com.haidousm.memory_game;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<Integer> flippedCardsIDs = new ArrayList<>();
    private final ArrayList<String> possibleCardTags = new ArrayList<>(List.of("square", "square", "circle", "circle", "triangle", "triangle", "star", "star"));

    private TextView scoreCounter;
    private Button playAgainButton;

    private MediaPlayer mediaPlayer;

    private final int numberOfCards = 8;
    private final String cardIDPrefix = "card_";
    private final String cardBackName = "logo";
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scoreCounter = findViewById(R.id.score_counter);
        playAgainButton = findViewById(R.id.play_again_btn);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0);


        shuffleCards();

    }

    public void shuffleCardsWrapper(View v){
        shuffleCards();
    }

    private void shuffleCards() {

        toggleInteraction(false);
        score = 0;
        scoreCounter.setText(String.valueOf(score));
        playAgainButton.setVisibility(View.INVISIBLE);

        flippedCardsIDs.clear();

        Collections.shuffle(possibleCardTags);
        for (int i = 0; i < numberOfCards; i++) {

            int imageResourceID = getResources().getIdentifier(cardIDPrefix + i, "id", getPackageName());

            ImageButton card = findViewById(imageResourceID);
            card.setTag(possibleCardTags.get(i));
            card.setBackgroundResource(R.drawable.logo);
            animateShuffling(card);

        }
        toggleInteraction(true);


    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void cardClicked(View v) {


        ImageButton firstCard = (ImageButton) v;
        String firstCardTag = firstCard.getTag().toString();

        int firstCardID = firstCard.getId();

        int secondCardIndex = (2 * score) + 1;

        if (flippedCardsIDs.contains(firstCardID)) {
            return;
        }

        if (flippedCardsIDs.size() < secondCardIndex + 1) {
            flippedCardsIDs.add(firstCardID);
            animateFlip(firstCard, firstCardTag);
        }

        if (flippedCardsIDs.size() == secondCardIndex + 1) {

            int secondCardID = flippedCardsIDs.get(secondCardIndex - 1);
            compareCards(firstCardID, secondCardID);

        }


    }

    private void compareCards(int firstCardID, int secondCardID) {

        toggleInteraction(false);

        ImageButton firstCard = (ImageButton) findViewById(firstCardID);
        ImageButton secondCard = (ImageButton) findViewById(secondCardID);

        String firstCardTag = firstCard.getTag().toString();
        String secondCardTag = secondCard.getTag().toString();

        new CountDownTimer(1000, 100) {

            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {

                if (firstCardTag.equals(secondCardTag)) {
                    playSoundFX(R.raw.correct_match);
                    score += 1;
                    scoreCounter.setText(String.valueOf(score));
                    if(score == numberOfCards / 2){
                        playAgainButton.setVisibility(View.VISIBLE);
                        playSoundFX(R.raw.win_game_fx);
                    }

                } else {
                    animateFlip(firstCard, cardBackName);
                    animateFlip(secondCard, cardBackName);
                    flippedCardsIDs.remove((Integer) firstCardID);
                    flippedCardsIDs.remove((Integer) secondCardID);
                }

                toggleInteraction(true);

            }

        }.start();

    }

    private void animateShuffling(ImageButton card){

        new CountDownTimer(3000, 1000) {
            final Random rand = new Random();
            @Override
            public void onTick(long millisUntilFinished) {

                animateFlip(card, possibleCardTags.get(rand.nextInt(numberOfCards)));
            }

            @Override
            public void onFinish() {
                animateFlip(card, cardBackName);
            }
        }.start();
    }

    private void animateFlip(ImageButton card, String image) {

        playSoundFX(R.raw.card_flip_fx);
        int resourceID = getResources().getIdentifier(image, "drawable", getPackageName());
        card.animate().withLayer()
                .rotationY(90)
                .setDuration(200)
                .withEndAction(
                        () -> {
                            card.setBackgroundResource(resourceID);
                            card.setRotationY(-90);
                            card.animate().withLayer()
                                    .rotationY(0)
                                    .setDuration(200)
                                    .start();
                        }
                ).start();
    }

    private void toggleInteraction(boolean isEnabled) {
        if (isEnabled) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }


    private void playSoundFX(int resourceID) {
        if (mediaPlayer != null) mediaPlayer.release();
        mediaPlayer = MediaPlayer.create(getApplicationContext(), resourceID);
        mediaPlayer.start();
    }


}