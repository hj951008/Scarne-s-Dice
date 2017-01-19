package com.davidsouther.scarnesdice;

/**
 * Created by dsouther on 1/17/17.
 */

public class PlayerState {

    private String id;
    private String email;
    private PlayerStatus status;
    private String gameId;

    public PlayerState() { }

    public PlayerState(String email) {
        this(email, PlayerStatus.READY);
    }

    public PlayerState(String email, PlayerStatus status) {
        this.id = email;
        this.email = email;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}

enum PlayerStatus {
    READY,
    IN_GAME,
}
