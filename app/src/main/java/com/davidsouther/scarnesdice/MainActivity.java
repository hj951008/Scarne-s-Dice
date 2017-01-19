package com.davidsouther.scarnesdice;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView playerScoreText;
    private TextView computerScoreText;
    private TextView turnScoreText;
    private TextView actionText;
    private ImageView dieView;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUserEmail;

    private DatabaseReference mFirebaseDatabase;

    private PlayerState state;
    private ScarnesDiceGame game;

    private Random random;

    public MainActivity() {
        this(new Random());
    }

    public MainActivity(Random random) {
        this.random = random;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser== null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUserEmail = mFirebaseUser.getEmail();
            mUserEmail = mUserEmail.substring(0, mUserEmail.indexOf('@'))
                    .replace('.', '_')
                    .replace('#', '_')
                    .replace('$', '_')
                    .replace('[', '_')
                    .replace(']', '_');
            configureDatabase();
        }

        setContentView(R.layout.activity_main);

        dieView = (ImageView) findViewById(R.id.dieView);
        computerScoreText =(TextView) findViewById(R.id.computerScoreText);
        playerScoreText = (TextView) findViewById(R.id.playerScoreText);
        turnScoreText = (TextView) findViewById(R.id.turnScoreText);
        actionText = (TextView) findViewById(R.id.computerAction);

        actionText.setText(String.format("%d", 5));

        dieView.setImageResource(R.drawable.empty);
        dieView.setContentDescription("Empty die face");
        computerScoreText.setText("0");
        playerScoreText.setText("0");
        turnScoreText.setText("0");
        actionText.setText("");
    }

    private void configureDatabase() {
        mFirebaseDatabase = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabase.child("players").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PlayerState state = dataSnapshot.getValue(PlayerState.class);
                // If both READY, start a game!
                if (state.getStatus() == PlayerStatus.READY && MainActivity.this.state.getStatus() == PlayerStatus.READY) {
                    startGame(state);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        state = new PlayerState(mUserEmail);
        mFirebaseDatabase.child("players").child(state.getId()).setValue(state);
    }

    private void startGame(PlayerState otherState) {
        game = new ScarnesDiceGame(state.getEmail(), otherState.getEmail(), random.nextBoolean() ? MultiPlayers.PLAYER1 : MultiPlayers.PLAYER2);
        mFirebaseDatabase.child("games").push().setValue(game);

        state.setGameId(game.getId());
        state.setStatus(PlayerStatus.IN_GAME);
        otherState.setGameId(game.getId());
        otherState.setStatus(PlayerStatus.IN_GAME);

        mFirebaseDatabase.child("players").child(state.getId()).setValue(state);
        mFirebaseDatabase.child("players").child(otherState.getId()).setValue(otherState);

        mFirebaseDatabase.child("games").child(game.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                game = dataSnapshot.getValue(ScarnesDiceGame.class);
                updateGameView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }


    private void updateGameView() {
        playerScoreText.setText(String.format("%d", game.getPlayer1Score()));
        computerScoreText.setText(String.format("%d", game.getPlayer2Score()));
        turnScoreText.setText(String.format("%d", game.getCurrentTurn()));
        int roll = game.getLastRoll();
        if (roll == 1) {
            actionText.setText(R.string.change_players);
        } else if (currentPlayer()) {
            actionText.setText("It is your turn!");
        } else {
            actionText.setText("Waiting for other player");
        }

        if (roll > 0) {
            showRoll(roll);
            checkForWin();
        }
    }

    private void showRoll(int roll) {
        int die;
        switch (roll) {
            case 1: die = R.drawable.dice1; break;
            case 2: die = R.drawable.dice2; break;
            case 3: die = R.drawable.dice3; break;
            case 4: die = R.drawable.dice4; break;
            case 5: die = R.drawable.dice5; break;
            case 6: die = R.drawable.dice6; break;
            default:
                throw new BadRollException(roll);
        }
        dieView.setImageResource(die);
        dieView.setContentDescription(String.format(getString(R.string.die_face), die));
    }

    public void roll(View view) {
        // if (!currentPlayer()) return;
        int frame = rollDice();
        showRoll(frame);

        if (frame == 1) {
            turnScoreText.setText("");
            game.setCurrentTurn(0);
            switchPlayers();
        } else {
            actionText.setText("");
            game.setCurrentTurn(game.getCurrentTurn() + frame);
            turnScoreText.setText(String.valueOf(game.getCurrentTurn()));
            checkForWin();
        }
        writeGame();
    }

    private void checkForWin() {
        int currentTurn = game.getCurrentTurn();
        switch (game.getCurrentPlayer()) {
            case PLAYER1: if (game.getPlayer1Score() + currentTurn > 25) playerWins(); break;
            case PLAYER2: if (game.getPlayer2Score() + currentTurn > 100) computerWins(); break;
        }
    }

    public static final String USER_SCORE = "com.davidsouther.scarne.USER_SCORE";
    private void playerWins() {
        Intent intent = new Intent(this, WinActivity.class);
        intent.putExtra(USER_SCORE, String.valueOf(game.getPlayer1Score() + game.getCurrentTurn()));
        startActivity(intent);
    }

    private void computerWins() {
        Intent intent = new Intent(this, LoseActivity.class);
        intent.putExtra(USER_SCORE, String.valueOf(game.getPlayer1Score() + game.getCurrentTurn()));
        startActivity(intent);
    }

    public void hold(View view) {
        if (!currentPlayer()) return;
        int currentTurn = game.getCurrentTurn();
        switch (game.getCurrentPlayer()) {
            case PLAYER1: game.setPlayer1Score(game.getPlayer1Score() + currentTurn); break;
            case PLAYER2: game.setPlayer2Score(game.getPlayer2Score() + currentTurn); break;
        }
        game.setCurrentTurn(0);
        switchPlayers();
        writeGame();
    }

    private int rollDice() {
        // Roll a random between 1 and 6, update the image, and return the value.
        int roll = (random.nextInt() % 6) + 1;
        if (roll < 1) { roll += 6; }
        return roll;
    }

    private void switchPlayers() {
        game.setCurrentPlayer(game.getCurrentPlayer() == MultiPlayers.PLAYER1 ? MultiPlayers.PLAYER2 : MultiPlayers.PLAYER1);
    }

    private void writeGame() {
        mFirebaseDatabase.child("games").child(game.getId()).setValue(game);
    }

    private boolean currentPlayer() {
        String currentEmail = "";
        switch (game.getCurrentPlayer()) {
            case PLAYER1:
                currentEmail = game.getPlayer1();
                break;
            case PLAYER2:
                currentEmail = game.getPlayer2();
                break;
        }
        return currentEmail.equals(mUserEmail);
    }
}

class BadRollException extends RuntimeException {
    public BadRollException(int roll) {
        super(String.format("Tried to roll a six-sided dice and got %d", roll));
    }
}
