package com.jtdev.umak.Model;

public class Card {
    private int cardID;
    private String front;
    private String back;
    private String difficulty;
    private int deckId;

    // Constructor for new card creation (without card ID)
    public Card(String front, String back, int deckId) {
        this.front = front;
        this.back = back;
        this.deckId = deckId;
        this.difficulty = "Medium";  // Default difficulty
    }

    // Constructor with all fields (for loading from DB)
    public Card(int cardId, String front, String back, String difficulty, int deckId) {
        this.cardID = cardId;
        this.front = front;
        this.back = back;
        this.difficulty = difficulty;
        this.deckId = deckId;
    }

    // Default constructor for future use if needed
    public Card() {
        this.difficulty = "Medium";  // Default difficulty for new cards
    }

    // Getters and setters for each field

    public String getFront() {
        return front;
    }

    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    public void setBack(String back) {
        this.back = back;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getDeckId() {
        return deckId;
    }

    public void setDeckId(int deckId) {
        this.deckId = deckId;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }
}
