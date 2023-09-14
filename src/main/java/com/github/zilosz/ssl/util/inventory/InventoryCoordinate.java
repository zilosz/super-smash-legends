package com.github.zilosz.ssl.util.inventory;

import lombok.Getter;

@Getter
public class InventoryCoordinate {
    private final int row;
    private final int column;

    public InventoryCoordinate(int row, int column) {
        this.row = row;
        this.column = column;
    }
}
