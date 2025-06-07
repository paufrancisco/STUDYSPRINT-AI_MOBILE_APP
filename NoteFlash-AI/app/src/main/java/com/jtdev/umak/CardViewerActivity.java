package com.jtdev.umak;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.jtdev.umak.Model.Card;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CardViewerActivity extends AppCompatActivity {
    private Queue<Card> cardQueue = new LinkedList<>();
    private int correctAnswers = 0;
    private DatabaseHelper dbHelper;
    private List<Card> cardList;
    private ProgressBar progressBar;
    private Button showAnswerButton, hardButton, mediumButton, goodButton;
    private TextView cardFrontTextView, cardBackTextView;
    private TextView cardlist, correct;
    private int remaining;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_viewer);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(0);
        progressBar.setIndeterminate(false);

        cardlist = findViewById(R.id.cardlist);
        correct = findViewById(R.id.correctanswer);

        showAnswerButton = findViewById(R.id.showAnswerButton);
        hardButton = findViewById(R.id.hardButton);
        mediumButton = findViewById(R.id.mediumButton);
        goodButton = findViewById(R.id.goodButton);
        cardFrontTextView = findViewById(R.id.cardFrontTextView);
        cardBackTextView = findViewById(R.id.cardBackTextView);

        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        int deckId = intent.getIntExtra("deck_id", -1);

        if (deckId == -1) {
            Toast.makeText(this, "Invalid deck", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cardList = dbHelper.getCardsByDeckId(deckId);
        int cardCount = dbHelper.getCardCountByDeckId(deckId);
        remaining = cardCount - correctAnswers;
        cardlist.setText(String.valueOf(remaining));

        cardQueue.addAll(cardList);
        loadNextCard();

        showAnswerButton.setOnClickListener(v -> {
            cardBackTextView.setVisibility(View.VISIBLE);
            hardButton.setVisibility(View.VISIBLE);
            mediumButton.setVisibility(View.VISIBLE);
            goodButton.setVisibility(View.VISIBLE);
            showAnswerButton.setVisibility(View.GONE);
        });

        hardButton.setOnClickListener(v -> handleDifficulty("Hard"));
        mediumButton.setOnClickListener(v -> handleDifficulty("Medium"));
        goodButton.setOnClickListener(v -> handleDifficulty("Easy"));


    }

    private void handleDifficulty(String difficulty) {
        Card currentCard = cardQueue.poll();

        if (currentCard != null) {
            dbHelper.updateCardDifficulty(currentCard.getCardID(), difficulty, System.currentTimeMillis());

            switch (difficulty) {
                case "Hard":
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        cardQueue.add(currentCard);
                        if (cardQueue.size() == 1) {
                            loadNextCard();
                        }
                    }, 5000);
                    break;
                case "Medium":
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        cardQueue.add(currentCard);
                        if (cardQueue.size() == 1) {
                            loadNextCard();
                        }
                    }, 10000);
                    break;
                case "Easy":
                    correctAnswers++;
                    remaining--;
                    correct.setText(String.valueOf(correctAnswers));
                    cardlist.setText(String.valueOf(remaining));
                    updateProgressBar();
                    break;
            }

            loadNextCard();
        }
    }



    private void loadNextCard() {
        if (cardQueue.isEmpty()) {
            goBackToDeckFragment();
            return;
        }

        Card currentCard = cardQueue.peek();
        if (currentCard != null) {
            cardFrontTextView.setText(currentCard.getFront());
            cardBackTextView.setText(currentCard.getBack());
            cardBackTextView.setVisibility(View.GONE);

            hardButton.setVisibility(View.GONE);
            mediumButton.setVisibility(View.GONE);
            goodButton.setVisibility(View.GONE);

            showAnswerButton.setVisibility(View.VISIBLE);
        }
    }

    private void updateProgressBar() {
        if (cardList.size() > 0) {
            int progress = (int) ((correctAnswers / (float) cardList.size()) * 100);
            progressBar.setProgress(progress);
        }
    }

    private void goBackToDeckFragment() {
        Toast.makeText(this, "All cards answered!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(CardViewerActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
