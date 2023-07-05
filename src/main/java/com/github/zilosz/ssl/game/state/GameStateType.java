package com.github.zilosz.ssl.game.state;

import com.github.zilosz.ssl.game.state.implementation.EndState;
import com.github.zilosz.ssl.game.state.implementation.InGameState;
import com.github.zilosz.ssl.game.state.implementation.LobbyState;
import com.github.zilosz.ssl.game.state.implementation.PreGameState;
import com.github.zilosz.ssl.game.state.implementation.TutorialState;

import java.util.function.Supplier;

public enum GameStateType implements Supplier<GameState> {
    LOBBY(LobbyState::new),
    TUTORIAL(TutorialState::new),
    PREGAME(PreGameState::new),
    IN_GAME(InGameState::new),
    END(EndState::new);

    private final Supplier<GameState> supplier;

    GameStateType(Supplier<GameState> supplier) {
        this.supplier = supplier;
    }

    @Override
    public GameState get() {
        return this.supplier.get();
    }
}
