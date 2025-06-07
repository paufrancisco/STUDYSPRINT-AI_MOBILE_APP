package com.jtdev.umak;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AddDeckActivity extends AppCompatActivity {

    private Spinner spinnerDeckName;
    private EditText editTextFront, editTextBack;
    private TextView textViewCardCount;
    private int cardCount = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_deck);

         spinnerDeckName = findViewById(R.id.spinnerDeckName);
        editTextFront = findViewById(R.id.editTextFront);
        editTextBack = findViewById(R.id.editTextBack);
        textViewCardCount = findViewById(R.id.textViewCardCount);
        Button buttonAddCard = findViewById(R.id.buttonAddCard);

         List<String> deckNames = new ArrayList<>();
        deckNames.add("Deck 1");
        deckNames.add("Deck 2");
        deckNames.add("Deck 3");

         ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, deckNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDeckName.setAdapter(adapter);

         buttonAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDeck = spinnerDeckName.getSelectedItem().toString();
                String frontText = editTextFront.getText().toString().trim();
                String backText = editTextBack.getText().toString().trim();

                if (frontText.isEmpty() || backText.isEmpty()) {
                    Toast.makeText(AddDeckActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                     cardCount++;
                    textViewCardCount.setText("Cards: " + cardCount);


                    editTextFront.setText("");
                    editTextBack.setText("");
                }
            }
        });
    }


}
