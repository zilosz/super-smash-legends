package com.github.zilosz.ssl.util.inventory;

public interface AutoUpdatesSoft {

    default int getSoftUpdateTicks() {
        return 10;
    }
}
