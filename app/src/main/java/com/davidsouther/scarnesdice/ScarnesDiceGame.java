package com.davidsouther.scarnesdice;

/**
 * Created by dsouther on 1/17/17.
 */

public class ScarnesDiceGame {
    private String id;
    private String player1;
    private String player2;

    private int lastRoll;
    private int currentTurn;
    private int player1Score;
    private int player2Score;
    private MultiPlayers currentPlayer;

    public ScarnesDiceGame() {}

    public ScarnesDiceGame(String player1, String player2, MultiPlayers currentPlayer) {
        this.id = String.format("%s-%s", player1, player2);

        this.player1 = player1;
        this.player2 = player2;
        this.currentPlayer = currentPlayer;

        currentTurn = 0;
        player1Score = 0;
        player2Score = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScarnesDiceGame that = (ScarnesDiceGame) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public int getLastRoll() {
        return lastRoll;
    }

    public void setLastRoll(int lastRoll) {
        this.lastRoll = lastRoll;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public MultiPlayers getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(MultiPlayers currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    @Override
    public String toString() {
        return "ScarnesDiceGame{" +
                "id='" + id + '\'' +
                ", player1='" + player1 + '\'' +
                ", player2='" + player2 + '\'' +
                ", lastRoll=" + lastRoll +
                ", currentTurn=" + currentTurn +
                ", player1Score=" + player1Score +
                ", player2Score=" + player2Score +
                ", currentPlayer=" + currentPlayer +
                '}';
    }
}

enum MultiPlayers {
    PLAYER1,
    PLAYER2,
}