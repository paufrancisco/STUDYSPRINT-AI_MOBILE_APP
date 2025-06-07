package com.jtdev.umak;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.jtdev.umak.Fragments.Decks;
import com.jtdev.umak.Model.Card;
public class AddCardActivity extends AppCompatActivity {

    private EditText frontCardEditText, backCardEditText;
    private TextView deckNameTextView, cardCountTextView;
    private Button btnSaveCard, btnBack;
    private int deckId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        frontCardEditText = findViewById(R.id.front_card);
        backCardEditText = findViewById(R.id.back_card);
        deckNameTextView = findViewById(R.id.deck_name);
        cardCountTextView = findViewById(R.id.card_count);
        btnSaveCard = findViewById(R.id.btn_save_card);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(view -> finish());

        deckId = getIntent().getIntExtra("deck_id", -1);
        if (deckId == -1) {
            Toast.makeText(this, "Invalid deck", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String deckName = dbHelper.getDeckNameById(deckId);
        deckNameTextView.setText(deckName);

        int cardCount = dbHelper.getCardCountByDeckId(deckId);
        cardCountTextView.setText(String.valueOf(cardCount));

        btnSaveCard.setOnClickListener(v -> saveCard());
    }

    private void saveCard() {
        String front = frontCardEditText.getText().toString().trim();
        String back = backCardEditText.getText().toString().trim();

        if (front.isEmpty() || back.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Card card = new Card(front, back, deckId);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.saveCard(card);
        Toast.makeText(this, "Card saved", Toast.LENGTH_SHORT).show();

        int newCardCount = dbHelper.getCardCountByDeckId(deckId);
        cardCountTextView.setText(String.valueOf(newCardCount));

        frontCardEditText.setText("");
        backCardEditText.setText("");
    }
}

