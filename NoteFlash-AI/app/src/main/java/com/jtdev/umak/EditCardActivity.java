package com.jtdev.umak;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jtdev.umak.Model.Card;

public class EditCardActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private int cardId;
    private Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_card);

        dbHelper = new DatabaseHelper(this);
        back = findViewById(R.id.btn_back);
        cardId = getIntent().getIntExtra("card_id", -1);

        Card card = dbHelper.getCardById(cardId);

        EditText frontCardEditText = findViewById(R.id.front_card);
        EditText backCardEditText = findViewById(R.id.back_card);

        frontCardEditText.setText(card.getFront());
        backCardEditText.setText(card.getBack());

        Button saveButton = findViewById(R.id.btn_save_card);
        saveButton.setOnClickListener(v -> {
            String front = frontCardEditText.getText().toString();
            String back = backCardEditText.getText().toString();
            String difficulty = "Medium";

            card.setFront(front);
            card.setBack(back);
            card.setDifficulty(difficulty);

             dbHelper.updateCard(card);

             Toast.makeText(this, "Card updated successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
