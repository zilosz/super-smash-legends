package com.github.zilosz.ssl.kit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KitType {
  BARBARIAN("barbarian", "Barbarian"),
  CRYOMANCER("cryomancer", "Cryomancer"),
  DEADMORTAL("deadmortal", "Deadmortal"),
  DRAKULA("drakula", "Drakula"),
  GLOBBY("globby", "Globby"),
  SKAREKROW("skarekrow", "Skarekrow"),
  MECHON("mechon", "Mechon"),
  MERMAID("mermaid", "Mermaid"),
  MINER("miner", "Miner"),
  MOREO("moreo", "Moreo"),
  MUSICIAN("musician", "Musician"),
  PIKACHEW("pikachew", "Pikachew"),
  SHINOBY("shinoby", "Shinoby"),
  SKELLINGTON("skellington", "Skellington"),
  TANK("tank", "Tank"),
  WEBMAN("webman", "Webman");

  private final String fileName;
  private final String configName;
}
