package com.jtdev.umak.Model;

import java.util.List;

public class Deck {
    private int deckId;
    private String deckName;
    private int cardCount;
    private List<Card> cards;  // Added list to store cards

    public Deck(int deckId, String deckName, int cardCount) {
        this.deckId = deckId;
        this.deckName = deckName;
        this.cardCount = cardCount;
    }

    // Getter and Setter for the card list
    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    // Other getters and setters
    public int getDeckId() {
        return deckId;
    }

    public String getDeckName() {
        return deckName;
    }

    public int getCardCount() {
        return cardCount;
    }
}
