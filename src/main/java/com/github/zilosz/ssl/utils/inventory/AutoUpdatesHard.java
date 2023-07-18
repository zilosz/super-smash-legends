package com.github.zilosz.ssl.utils.inventory;

public interface AutoUpdatesHard {

    default int getHardResetTicks() {
        return 30;
    }
}
