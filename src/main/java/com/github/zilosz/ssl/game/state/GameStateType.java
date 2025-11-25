package com.github.zilosz.ssl.game.state;

import com.github.zilosz.ssl.game.state.impl.EndState;
import com.github.zilosz.ssl.game.state.impl.InGameState;
import com.github.zilosz.ssl.game.state.impl.LobbyState;
import com.github.zilosz.ssl.game.state.impl.PreGameState;
import com.github.zilosz.ssl.game.state.impl.TutorialState;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public enum GameStateType implements Supplier<GameState> {
  LOBBY(LobbyState::new),
  TUTORIAL(TutorialState::new),
  PREGAME(PreGameState::new),
  IN_GAME(InGameState::new),
  END(EndState::new);

  private final Supplier<GameState> supplier;

  @Override
  public GameState get() {
    return supplier.get();
  }
}
