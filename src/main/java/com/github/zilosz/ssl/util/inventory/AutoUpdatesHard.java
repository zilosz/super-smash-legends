package com.github.zilosz.ssl.util.inventory;

public interface AutoUpdatesHard {

  default int getHardResetTicks() {
    return 30;
  }
}
