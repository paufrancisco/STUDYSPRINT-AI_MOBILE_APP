package com.jtdev.umak.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.jtdev.umak.CardViewerActivity;
import com.jtdev.umak.Model.Deck;
import com.jtdev.umak.R;
import com.jtdev.umak.AddCardActivity;
import com.jtdev.umak.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.DeckViewHolder> {

    private List<Deck> deckList;
    private List<Deck> deckListFull;
    private Context context;
    private DatabaseHelper dbHelper;

    public DeckAdapter(List<Deck> deckList, Context context) {
        this.deckList = deckList;
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.deckListFull = new ArrayList<>(deckList);
    }

    @NonNull
    @Override
    public DeckViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deck, parent, false);
        return new DeckViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeckViewHolder holder, int position) {
        Deck currentDeck = deckList.get(position);
        holder.deckTitle.setText(currentDeck.getDeckName());

        int cardCount = dbHelper.getCardCountByDeckId(currentDeck.getDeckId());
        holder.cardCount.setText(cardCount + " cards");

        int completionPercentage = dbHelper.getCompletedCardPercentage(currentDeck.getDeckId());

        if (completionPercentage > 0) {
            holder.deckProgress.setVisibility(View.VISIBLE);
            holder.deckProgress.setProgress(completionPercentage);

        } else {
            holder.deckProgress.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (cardCount == 0) {
                Snackbar snackbar = Snackbar.make(v, "This deck is empty. Add cards now.", Snackbar.LENGTH_LONG)
                        .setAction("Add", view -> {
                            Intent intent = new Intent(context, AddCardActivity.class);
                            intent.putExtra("deck_id", currentDeck.getDeckId());
                            intent.putExtra("deck_name", currentDeck.getDeckName());
                            context.startActivity(intent);
                        });

                View snackbarView = snackbar.getView();
                Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbarView;
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
                params.setMargins(20, 0, 20, 250);
                snackbarView.setLayoutParams(params);

                snackbar.show();
            } else {
                Intent intent = new Intent(context, CardViewerActivity.class);
                intent.putExtra("deck_id", currentDeck.getDeckId());
                intent.putExtra("deck_name", currentDeck.getDeckName());
                context.startActivity(intent);
            }
        });
    }


        @Override
    public int getItemCount() {
        return deckList.size();
    }

    // Method to update the deck list with the filtered results
    public void updateDeckList(List<Deck> newDeckList) {
        deckList = newDeckList;
        notifyDataSetChanged();
    }

     public void filterDeckList(String query) {
        List<Deck> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(deckListFull);
        } else {
            query = query.toLowerCase().trim();
            for (Deck deck : deckListFull) {
                if (deck.getDeckName().toLowerCase().contains(query)) {
                    filteredList.add(deck);
                }
            }
        }

        updateDeckList(filteredList);
    }

    public static class DeckViewHolder extends RecyclerView.ViewHolder {
        TextView deckTitle;
        TextView cardCount;
        ProgressBar deckProgress;

        public DeckViewHolder(View itemView) {
            super(itemView);
            deckTitle = itemView.findViewById(R.id.deck_title);
            cardCount = itemView.findViewById(R.id.deck_item_count);
            deckProgress = itemView.findViewById(R.id.deck_progress);
        }
    }
}
