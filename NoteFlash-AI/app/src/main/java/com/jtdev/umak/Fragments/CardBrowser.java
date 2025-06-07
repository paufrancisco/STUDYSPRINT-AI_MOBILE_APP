package com.jtdev.umak.Fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.jtdev.umak.AddCardActivity;
import com.jtdev.umak.DatabaseHelper;
import com.jtdev.umak.Model.Card;
import com.jtdev.umak.Model.Deck;
import com.jtdev.umak.R;
import com.jtdev.umak.Adapter.CardAdapter;
import java.util.ArrayList;
import java.util.List;

public class CardBrowser extends Fragment {

    private DatabaseHelper dbHelper;
    private Spinner deckSpinner;
    private RecyclerView cardRecyclerView;
    private CardAdapter cardAdapter;
    private List<String> deckNames = new ArrayList<>();
    private int selectedDeckId;
    private int selectedDeckPosition = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        dbHelper = new DatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_browser, container, false);

        if (getActivity() != null) {
            getActivity().setTitle("");
        }

        cardRecyclerView = view.findViewById(R.id.cardRecyclerView);
        cardRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        getActivity().invalidateOptionsMenu();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.cardbrowser_menu, menu);

         MenuItem spinnerItem = menu.findItem(R.id.menu_spinner);
        deckSpinner = (Spinner) spinnerItem.getActionView();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, deckNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deckSpinner.setAdapter(adapter);
        deckSpinner.setPopupBackgroundResource(R.drawable.dialog_spinner_background);

         loadDecks();

         deckSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!deckNames.isEmpty()) {
                    String selectedDeck = deckNames.get(position);
                    selectedDeckId = dbHelper.getDeckIdByName(selectedDeck);
                    loadCardsForDeck(selectedDeckId);
                    selectedDeckPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Setup the search view
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterCards(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCards(newText);
                return true;
            }
        });
    }
    private void filterCards(String query) {
        List<Card> allCards = dbHelper.getCardsByDeckId(selectedDeckId);
        List<Card> filteredCards = new ArrayList<>();

        for (Card card : allCards) {
            if (card.getFront().toLowerCase().contains(query.toLowerCase()) ||
                    card.getBack().toLowerCase().contains(query.toLowerCase())) {
                filteredCards.add(card);
            }
        }

        if (cardAdapter != null) {
            cardAdapter.updateCardList(filteredCards);
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_add_card) {
            Intent intent = new Intent(getContext(), AddCardActivity.class);
            intent.putExtra("deck_id", selectedDeckId);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_search) {




             return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

         if (deckSpinner != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, deckNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deckSpinner.setAdapter(adapter);

            if (deckNames.size() > 0 && selectedDeckPosition != -1) {
                deckSpinner.setSelection(selectedDeckPosition);
            }
        } else {
            Log.e("CardBrowser", "Spinner is null");
        }
    }

    private void loadDecks() {
        List<Deck> updatedDeckList = dbHelper.getAllDecks();
        deckNames.clear();

        if (updatedDeckList.isEmpty()) {
            cardRecyclerView.setVisibility(View.GONE);
            Toast.makeText(getContext(), "No decks available", Toast.LENGTH_SHORT).show();
        } else {
            cardRecyclerView.setVisibility(View.VISIBLE);

            for (Deck deck : updatedDeckList) {
                deckNames.add(deck.getDeckName());
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, deckNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            deckSpinner.setAdapter(adapter);

            if (!deckNames.isEmpty()) {
                String initialDeck = deckNames.get(0);
                selectedDeckId = dbHelper.getDeckIdByName(initialDeck);
                loadCardsForDeck(selectedDeckId);
            }
        }
    }




    private void loadCardsForDeck(int deckId) {
        List<Card> cards = dbHelper.getCardsByDeckId(deckId);
        if (cardAdapter == null) {
            cardAdapter = new CardAdapter(cards, getContext());
            cardRecyclerView.setAdapter(cardAdapter);
        } else {
            cardAdapter.updateCardList(cards);
        }
    }






}
