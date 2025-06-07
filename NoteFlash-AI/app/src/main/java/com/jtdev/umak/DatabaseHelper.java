package com.jtdev.umak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jtdev.umak.Model.Card;
import com.jtdev.umak.Model.Deck;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flashcards.db";
    private static final int DATABASE_VERSION = 1;

    // Deck Table
    public static final String TABLE_DECKS = "decks";
    public static final String COLUMN_DECK_ID = "deck_id";
    public static final String COLUMN_DECK_NAME = "deck_name";
    public static final String COLUMN_CARD_COUNT = "card_count";

    // Card Table
    public static final String TABLE_CARDS = "cards";
    public static final String COLUMN_CARD_ID = "card_id";
    public static final String COLUMN_CARD_FRONT = "card_front";
    public static final String COLUMN_CARD_BACK = "card_back";
    public static final String COLUMN_CARD_DIFFICULTY = "card_difficulty";
    public static final String COLUMN_CARD_DECK_ID = "deck_id";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Decks Table
        String createDecksTable = "CREATE TABLE IF NOT EXISTS " + TABLE_DECKS + " (" +
                COLUMN_DECK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_DECK_NAME + " TEXT NOT NULL, " +
                COLUMN_CARD_COUNT + " INTEGER DEFAULT 0" +
                ")";
        db.execSQL(createDecksTable);

        // Create Cards Table
        String createCardsTable = "CREATE TABLE " + TABLE_CARDS + " (" +
                COLUMN_CARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CARD_FRONT + " TEXT, " +
                COLUMN_CARD_BACK + " TEXT, " +
                COLUMN_CARD_DIFFICULTY + " TEXT, " +
                COLUMN_CARD_DECK_ID + " INTEGER, " +
                "last_marked_timestamp INTEGER, " +
                "FOREIGN KEY(" + COLUMN_CARD_DECK_ID + ") REFERENCES " + TABLE_DECKS + "(" + COLUMN_DECK_ID + "))";
        db.execSQL(createCardsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DECKS);
        onCreate(db);
    }

    // Save a new card
    public void saveCard(Card card) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_FRONT, card.getFront());
        values.put(COLUMN_CARD_BACK, card.getBack());
        values.put(COLUMN_CARD_DIFFICULTY, card.getDifficulty());
        values.put(COLUMN_CARD_DECK_ID, card.getDeckId());

        long id = db.insert(TABLE_CARDS, null, values);
        card.setCardID((int) id);

        db.close();
    }
    // Save a new card with deckId
    public void saveCard(Card card, long deckId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_FRONT, card.getFront());
        values.put(COLUMN_CARD_BACK, card.getBack());
        values.put(COLUMN_CARD_DIFFICULTY, card.getDifficulty());
        values.put(COLUMN_CARD_DECK_ID, deckId);  // Use deckId directly to associate the card with the deck

        long id = db.insert(TABLE_CARDS, null, values);
        card.setCardID((int) id);  // Set the card ID for the newly inserted card

        db.close();
    }




    // Get all decks
    public List<Deck> getAllDecks() {
        List<Deck> deckList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DECKS, null);

        if (cursor.moveToFirst()) {
            do {
                int deckId = cursor.getInt(cursor.getColumnIndex(COLUMN_DECK_ID));
                String deckName = cursor.getString(cursor.getColumnIndex(COLUMN_DECK_NAME));
                int cardCount = cursor.getInt(cursor.getColumnIndex(COLUMN_CARD_COUNT));

                // Create Deck object
                Deck deck = new Deck(deckId, deckName, cardCount);

                // Set cards associated with the deck
                deck.setCards(getCardsByDeckId(deckId));

                deckList.add(deck);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return deckList;
    }
    //get cardcount
    public int getCardCountByDeckId(int deckId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_CARDS + " WHERE " + COLUMN_CARD_DECK_ID + " = ?";
        Cursor cursor = db.rawQuery(countQuery, new String[]{String.valueOf(deckId)});

        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_COUNT, count);

        db = this.getWritableDatabase();
        db.update(TABLE_DECKS, values, COLUMN_DECK_ID + " = ?", new String[]{String.valueOf(deckId)});
        db.close();
        return count;
    }

    public String getDeckNameById(int deckId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_DECK_NAME + " FROM " + TABLE_DECKS + " WHERE " + COLUMN_DECK_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(deckId)});

        String deckName = null;
        if (cursor.moveToFirst()) {
            deckName = cursor.getString(cursor.getColumnIndex(COLUMN_DECK_NAME));
        }

        cursor.close();
        db.close();
        return deckName;
    }

    // Get all cards by deck ID
    public List<Card> getCardsByDeckId(int deckId) {
        List<Card> cardList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CARDS + " WHERE " + COLUMN_CARD_DECK_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(deckId)});

        if (cursor.moveToFirst()) {
            do {
                int cardId = cursor.getInt(cursor.getColumnIndex(COLUMN_CARD_ID));
                String cardFront = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_FRONT));
                String cardBack = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_BACK));
                String cardDifficulty = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_DIFFICULTY));
                cardList.add(new Card(cardId, cardFront, cardBack, cardDifficulty, deckId));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return cardList;
    }

    // Update the difficulty of a card
    public void updateCardDifficulty(int cardId, String difficulty, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_DIFFICULTY, difficulty);
        values.put("last_marked_timestamp", timestamp);
        db.update(TABLE_CARDS, values, COLUMN_CARD_ID + " = ?", new String[]{String.valueOf(cardId)});
        db.close();
    }

    public int getGoodCardCount(int deckId) {
        int goodCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM Cards WHERE deck_id = ? AND card_difficulty = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(deckId), "Easy"});

        if (cursor.moveToFirst()) {
            goodCount = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return goodCount;
    }

    public int getHardCardCount(int deckId) {
        int hardCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_CARDS + " WHERE " + COLUMN_CARD_DECK_ID + " = ? AND " + COLUMN_CARD_DIFFICULTY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] {String.valueOf(deckId), "Hard"});

        if (cursor.moveToFirst()) {
            hardCount = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return hardCount;
    }
    public int getCompletedCardPercentage(int deckId) {
        int totalCards = getCardCountByDeckId(deckId);
        int completedCards = getGoodCardCount(deckId);

        if (totalCards == 0) {
            return 0;
        }

        return (int) ((double) completedCards / totalCards * 100);  // Calculate percentage
    }
    public List<String> getAllDeckNames() {
        List<String> deckNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COLUMN_DECK_NAME + " FROM " + TABLE_DECKS, null);

        if (cursor.moveToFirst()) {
            do {
                deckNames.add(cursor.getString(cursor.getColumnIndex(COLUMN_DECK_NAME)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return deckNames;
    }
    public int getDeckIdByName(String deckName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int deckId = -1;
        String query = "SELECT " + COLUMN_DECK_ID + " FROM " + TABLE_DECKS + " WHERE " + COLUMN_DECK_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{deckName});

        if (cursor.moveToFirst()) {
            deckId = cursor.getInt(cursor.getColumnIndex(COLUMN_DECK_ID));
        }
        cursor.close();
        db.close();

        return deckId;
    }

    public void updateCard(Card card) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_FRONT, card.getFront());
        values.put(COLUMN_CARD_BACK, card.getBack());
        values.put(COLUMN_CARD_DIFFICULTY, card.getDifficulty());
        values.put(COLUMN_CARD_DECK_ID, card.getDeckId());


        db.update(TABLE_CARDS, values, COLUMN_CARD_ID + " = ?", new String[]{String.valueOf(card.getCardID())});

        db.close();
    }
    public Card getCardById(int cardId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CARDS + " WHERE " + COLUMN_CARD_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(cardId)});

        Card card = null;
        if (cursor.moveToFirst()) {
            String front = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_FRONT));
            String back = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_BACK));
            String difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_CARD_DIFFICULTY));
            int deckId = cursor.getInt(cursor.getColumnIndex(COLUMN_CARD_DECK_ID));

            card = new Card(cardId, front, back, difficulty, deckId);
        }

        cursor.close();
        db.close();

        return card;
    }

    public int getMediumCardCount(int deckId) {
        int mediumCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_CARDS + " WHERE " + COLUMN_CARD_DECK_ID + " = ? AND " + COLUMN_CARD_DIFFICULTY + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(deckId), "Medium"});

        if (cursor.moveToFirst()) {
            mediumCount = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return mediumCount;
    }

    public int getTotalCardCount() {
        // SQL query to count all cards across all decks
        String query = "SELECT COUNT(*) FROM cards";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int totalCount = 0;
        if (cursor.moveToFirst()) {
            totalCount = cursor.getInt(0);
        }
        cursor.close();
        return totalCount;
    }

    public int getTotalEasyCardCount() {
        String query = "SELECT COUNT(*) FROM cards WHERE card_difficulty = 'Easy'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int goodCount = 0;
        if (cursor.moveToFirst()) {
            goodCount = cursor.getInt(0);
        }
        cursor.close();
        return goodCount;
    }

    public int getTotalHardCardCount() {
        String query = "SELECT COUNT(*) FROM cards WHERE card_difficulty = 'Hard'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int hardCount = 0;
        if (cursor.moveToFirst()) {
            hardCount = cursor.getInt(0);
        }
        cursor.close();
        return hardCount;
    }

    public int getTotalMediumCardCount() {
        String query = "SELECT COUNT(*) FROM cards WHERE card_difficulty = 'Medium'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int mediumCount = 0;
        if (cursor.moveToFirst()) {
            mediumCount = cursor.getInt(0);
        }
        cursor.close();
        return mediumCount;
    }


    public long createDeck(String deckName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", deckName);
        return db.insert("decks", null, values);
    }
    // Method to check if a deck exists
    public Deck getDeckByName(String deckName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DECKS + " WHERE " + COLUMN_DECK_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{deckName});

        Deck deck = null;

        if (cursor.moveToFirst()) {
            // Retrieve data from cursor
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DECK_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DECK_NAME));
            int cardCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CARD_COUNT));

            // Create the deck object using all necessary fields
            deck = new Deck(id, name, cardCount);
        }

        cursor.close();
        return deck;
    }






    public Deck getDeckById(long deckId) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Use the correct column name for the primary key
        Cursor cursor = db.query(TABLE_DECKS, null, COLUMN_DECK_ID + " = ?", new String[]{String.valueOf(deckId)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            // Retrieve the deck name using the correct column name
            String deckName = cursor.getString(cursor.getColumnIndex(COLUMN_DECK_NAME));
            cursor.close();

            // Return a new Deck object
            return new Deck((int) deckId, deckName, 0);  // Adjust if needed to return other properties
        }
        return null;
    }


    // Save a new deck with initial card count set to 0
    public long saveDeck(String deckName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DECK_NAME, deckName);
        values.put(COLUMN_CARD_COUNT, 0);  // Initialize card count to 0

        // Insert into the database and get the new row ID
        long deckId = db.insert(TABLE_DECKS, null, values);
        db.close();

        // Check if insertion was successful
        if (deckId == -1) {
            Log.e("Database", "Error inserting deck: " + deckName);
            return -1;  // Return -1 if insert fails
        }

        return deckId;  // Return the new deck ID
    }


    // Update the card count for a specific deck
    public void updateDeckCardCount(int deckId) {
        int cardCount = getCardCountByDeckId(deckId); // Fetch updated card count
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CARD_COUNT, cardCount);
        db.update(TABLE_DECKS, values, COLUMN_DECK_ID + " = ?", new String[]{String.valueOf(deckId)});
        db.close();
    }

    // Get a deck by its ID with updated card count
    public Deck getDeckById(int deckId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_DECKS + " WHERE " + COLUMN_DECK_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(deckId)});

        Deck deck = null;
        if (cursor.moveToFirst()) {
            String deckName = cursor.getString(cursor.getColumnIndex(COLUMN_DECK_NAME));
            int cardCount = cursor.getInt(cursor.getColumnIndex(COLUMN_CARD_COUNT));
            deck = new Deck(deckId, deckName, cardCount);
        }

        cursor.close();
        db.close();
        return deck;
    }
























}