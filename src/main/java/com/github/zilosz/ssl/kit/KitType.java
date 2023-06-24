package com.github.zilosz.ssl.kit;

import lombok.Getter;

public enum KitType {
    BARBARIAN("barbarian"),
    CRYOMANCER("cryomancer"),
    DEADMORTAL("deadmortal"),
    DRAKULA("drakula"),
    GLOBBY("globby"),
    MASTER_BLADE("master-blade"),
    MECHON("mechon"),
    MERMAID("mermaid"),
    MINER("miner"),
    MOREO("moreo"),
    MUSICIAN("musician"),
    PIKACHEW("pikachew"),
    SHINOBY("shinoby"),
    SKELLINGTON("skellington"),
    TANK("tank"),
    WEBMAN("webman");

    @Getter private final String fileName;

    KitType(String fileName) {
        this.fileName = fileName;
    }
}
