package com.github.zilosz.ssl.util.inventory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InventoryCoordinate {
  private final int row;
  private final int column;
}
