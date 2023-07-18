package com.github.zilosz.ssl.utils.inventory;

public interface AutoUpdatesSoft {

    default int getSoftUpdateTicks() {
        return 10;
    }
}
