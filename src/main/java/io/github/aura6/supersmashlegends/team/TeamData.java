package io.github.aura6.supersmashlegends.team;

import lombok.Getter;

public class TeamData {
    @Getter private final String name;
    @Getter private final String textColor;
    @Getter private final int woolData;

    public TeamData(String name, String textColor, int woolData) {
        this.name = name;
        this.textColor = textColor;
        this.woolData = woolData;
    }
}
