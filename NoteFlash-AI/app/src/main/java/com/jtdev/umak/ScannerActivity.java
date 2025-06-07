package com.jtdev.umak;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.jtdev.umak.Model.Card;
import com.jtdev.umak.Model.Deck;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ScannerActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageButton btnCaptureImage, btnSelectImage;
    private ImageView imageView;
    private TextView textView;
    private Bitmap imageBitmap;
    private Button btnSaveToDeck;
    private long deckId;
    private String recognizedText;
    private StringAnalyzer stringAnalyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        btnCaptureImage = findViewById(R.id.btnCaptureImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        btnSaveToDeck = findViewById(R.id.btnSaveToDeck);

        btnSaveToDeck.setEnabled(false);

        deckId = getIntent().getLongExtra("deck_id", -1);

        // Initialize StringAnalyzer with context
        stringAnalyzer = new StringAnalyzer(this);

        btnCaptureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });

        // Set up listener for save to deck button
        btnSaveToDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recognizedText != null && !recognizedText.isEmpty()) {
                    saveCardsToDeck(deckId);  // Directly save the recognized text to the specified deck
                } else {
                    Toast.makeText(ScannerActivity.this, "No text recognized to save.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void saveCardsToDeck(long deckId) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Extract nouns from assets to pass to StringAnalyzer
        String[] nouns = stringAnalyzer.getNounsFromAssets();
        Set<String> nounsSet = new HashSet<>(Arrays.asList(nouns));

        // Use the StringAnalyzer to extract terms and definitions
        String termsAndDefinitions = stringAnalyzer.extract_terms_and_definitions(recognizedText, nounsSet);

        // Split the terms and definitions into individual term-definition pairs
        String[] termDefinitionPairs = termsAndDefinitions.split("\n");

        for (String pair : termDefinitionPairs) {
            String[] termAndDef = pair.split(": ");
            if (termAndDef.length == 2) {
                String term = termAndDef[0].trim();
                String definition = termAndDef[1].trim();

                // Interchange term and definition
                String temp = term;
                term = definition;
                definition = temp;

                Card card = new Card();
                card.setFront(term);  // Set the definition as the term
                card.setBack(definition);  // Set the noun as the definition
                card.setDifficulty("Medium");

                dbHelper.saveCard(card, deckId);
            }
        }

        Toast.makeText(this, "Cards saved to deck", Toast.LENGTH_SHORT).show();
        resetUIAfterSaving();  // Reset the UI after saving cards
    }

    private void resetUIAfterSaving() {
        textView.setText("");  // Clear the recognized text
        imageView.setImageDrawable(null);  // Clear the displayed image
        recognizedText = "";  // Clear the accumulated text
        btnSaveToDeck.setEnabled(false);  // Disable save button again
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                imageBitmap = (Bitmap) extras.get("data");
                imageView.setImageBitmap(imageBitmap);
                runTextRecognition();
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                Uri imageUri = data.getData();
                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    imageView.setImageBitmap(imageBitmap);
                    runTextRecognition();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        com.google.mlkit.vision.text.TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        displayRecognizedText(visionText);
                        btnSaveToDeck.setEnabled(true);  // Enable the save button after recognition
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        textView.setText("Failed to recognize text: " + e.getMessage());
                    }
                });
    }

    private void displayRecognizedText(Text visionText) {
        StringBuilder recognizedTextBuilder = new StringBuilder();
        for (Text.TextBlock block : visionText.getTextBlocks()) {
            recognizedTextBuilder.append(block.getText()).append("\n");
        }
        recognizedText = recognizedTextBuilder.toString();  // Store recognized text globally
        textView.setText(recognizedText);
    }

    // Mock methods for extracting term and definition from a sentence
    private String extractTerm(String sentence) {
        // Example: Extract the first word as a "term"
        String[] words = sentence.split(" ");
        return words.length > 0 ? words[0] : "";
    }

    private String extractDefinition(String sentence) {
        // Example: Use the rest of the sentence as a "definition"
        String[] words = sentence.split(" ");
        return words.length > 1 ? sentence.substring(sentence.indexOf(" ") + 1) : "";
    }
}
