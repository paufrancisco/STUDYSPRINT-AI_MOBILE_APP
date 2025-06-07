package com.jtdev.umak.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jtdev.umak.EditCardActivity;
import com.jtdev.umak.Model.Card;
import com.jtdev.umak.R;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<Card> cardList;
    private Context context;

    public CardAdapter(List<Card> cardList, Context context) {
        this.cardList = cardList;
        this.context = context;
    }

    /**
     * Updates the card list and refreshes the RecyclerView.
     * @param newCards The updated list of cards to display.
     */
    public void updateCardList(List<Card> newCards) {
        this.cardList.clear();
        this.cardList.addAll(newCards);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        Card card = cardList.get(position);
        holder.frontTextView.setText(card.getFront());
        holder.backTextView.setText(card.getBack());

         holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditCardActivity.class);
            intent.putExtra("card_id", card.getCardID());
            intent.putExtra("card_front", card.getFront());
            intent.putExtra("card_back", card.getBack());
            intent.putExtra("card_difficulty", card.getDifficulty());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }


    public static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView frontTextView;
        TextView backTextView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            frontTextView = itemView.findViewById(R.id.cardFrontTextView);
            backTextView = itemView.findViewById(R.id.cardBackTextView);
        }
    }
}
