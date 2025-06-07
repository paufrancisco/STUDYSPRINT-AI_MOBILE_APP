package com.jtdev.umak.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.jtdev.umak.Adapter.DeckAdapter;
import com.jtdev.umak.DatabaseHelper;
import com.jtdev.umak.Model.Card;
import com.jtdev.umak.Model.Deck;
import com.jtdev.umak.R;
import com.jtdev.umak.ScannerActivity;

import java.util.ArrayList;
import java.util.List;

public class Decks extends Fragment {

    private RecyclerView recyclerView;
    private DeckAdapter deckAdapter;
    private List<Deck> deckList = new ArrayList<>();
    private DatabaseHelper dbHelper;

    public Decks() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, android.view.ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deck, container, false);

        setHasOptionsMenu(true);
        if (getActivity() != null) {
            getActivity().setTitle("Decks");
        }

        recyclerView = view.findViewById(R.id.recycler_view_decks);
        dbHelper = new DatabaseHelper(getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        deckAdapter = new DeckAdapter(deckList, getContext());
        recyclerView.setAdapter(deckAdapter);

        loadDecks();

        // FAB menu
        View fabAdd = view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.fab_menu, popupMenu.getMenu());

            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                MenuItem item = popupMenu.getMenu().getItem(i);
                SpannableString spannableString = new SpannableString(item.getTitle());
                spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), 0, spannableString.length(), 0);
                item.setTitle(spannableString);
            }

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_add_manual) {
                    showCreateDeckDialog();
                    return true;
                } else if (item.getItemId() == R.id.menu_add_ai) {
                    askForDeckTitleAndGoToScanner();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show();
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.decks_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDecks(newText);
                return true;
            }
        });

        searchView.setIconified(false);
        searchView.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            syncData();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDecks();
    }

    private void loadDecks() {
        List<Deck> updatedDeckList = dbHelper.getAllDecks();
        if (updatedDeckList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }

        deckList.clear();
        deckList.addAll(updatedDeckList);
        deckAdapter.notifyDataSetChanged();
    }

    private void syncData() {
        // Show loading indicator
        showLoading(true);

        // Step 1: Retrieve local data from the database
        List<Deck> localDecks = dbHelper.getAllDecks();

        // Step 2: Upload local data to Firebase
        uploadDecksToFirebase(localDecks);

        // Hide loading indicator once sync is done
        showLoading(false);
    }

    private void uploadDecksToFirebase(List<Deck> decks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Loop through each deck in the list and upload it to Firestore
        for (Deck deck : decks) {
            // Assuming the deck object has a method getDeckId() that returns a unique identifier for the deck
            String deckId = String.valueOf(deck.getDeckId());  // Replace with the appropriate method if necessary

            // Set the deck in the Firestore collection "decks" using the deck's ID as the document ID
            db.collection("decks")
                    .document(deckId)  // Use the deck ID for the document ID in Firestore
                    .set(deck)  // Set the deck data to Firestore
                    .addOnSuccessListener(aVoid -> {
                        // Log success
                        Log.d("Sync", "Deck uploaded successfully: " + deck.getDeckName());

                        // Now upload the cards associated with the deck
                        uploadCardsForDeck(deckId, deck.getCards());
                    })
                    .addOnFailureListener(e -> {
                        // Log failure
                        Log.d("Sync", "Failed to upload deck: " + e.getMessage());
                    });
        }
    }

    private void uploadCardsForDeck(String deckId, List<Card> cards) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Loop through each card in the list and upload it to Firestore
        for (Card card : cards) {
            // Assuming the card object has a method getCardId() or a similar identifier for each card
            String cardId = String.valueOf(card.getCardID());  // Replace with the appropriate method if necessary

            // Set the card in the Firestore sub-collection "cards" under the corresponding deck document
            db.collection("decks")
                    .document(deckId)  // Reference the deck document by its deckId
                    .collection("cards")  // Use the sub-collection "cards" for each deck's cards
                    .document(cardId)  // Use the card ID for the document ID in the sub-collection
                    .set(card)  // Set the card data to Firestore
                    .addOnSuccessListener(aVoid -> {
                        // Log success
                        Log.d("Sync", "Card uploaded successfully: " + card.getFront());
                    })
                    .addOnFailureListener(e -> {
                        // Log failure
                        Log.d("Sync", "Failed to upload card: " + e.getMessage());
                    });
        }
    }










    private void showLoading(boolean show) {
        // Show or hide a loading spinner
        if (show) {
            // Show loading
        } else {
            // Hide loading
        }
    }

    private void filterDecks(String query) {
        List<Deck> filteredDecks = new ArrayList<>();
        for (Deck deck : deckList) {
            if (deck.getDeckName().toLowerCase().contains(query.toLowerCase())) {
                filteredDecks.add(deck);
            }
        }
        deckAdapter.updateDeckList(filteredDecks);
    }

    private void showCreateDeckDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        TextView title = new TextView(getContext());
        title.setText("Enter Deck Name");
        title.setTextColor(Color.BLACK);
        title.setPadding(20, 20, 20, 20);
        title.setTextSize(20);
        builder.setCustomTitle(title);

        final EditText input = new EditText(getContext());
        input.setHint("Deck Name");
        input.setPadding(20,20,20,20);
        input.setTextColor(Color.BLACK);

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deckName = input.getText().toString().trim();
                if (!deckName.isEmpty()) {
                    saveDeckToDatabase(deckName);
                } else {
                    Toast.makeText(getContext(), "Deck name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void saveDeckToDatabase(String deckName) {
        dbHelper.saveDeck(deckName);
        Toast.makeText(getContext(), "Deck '" + deckName + "' created", Toast.LENGTH_SHORT).show();
        loadDecks();
    }

    private void askForDeckTitleAndGoToScanner() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Deck Title");

        final EditText input = new EditText(getContext());
        builder.setView(input);
        TextView title = new TextView(getContext());
        title.setText("Enter Deck Name");
        title.setTextColor(Color.BLACK);
        title.setPadding(20, 20, 20, 20);
        title.setTextSize(20);
        builder.setCustomTitle(title);

        input.setHint("Deck Name");
        input.setPadding(20,20,20,20);
        input.setTextColor(Color.BLACK);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String deckTitle = input.getText().toString();
                if (!deckTitle.isEmpty()) {
                    // Save deck and get the deckId
                    long deckId = dbHelper.saveDeck(deckTitle);  // Modify your method to return the deckId
                    Intent intent = new Intent(getContext(), ScannerActivity.class);
                    intent.putExtra("deck_id", deckId);  // Pass the deckId to ScannerActivity
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Deck title cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
