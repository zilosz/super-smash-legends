package com.github.zilosz.ssl.config;

import com.github.zilosz.ssl.SSL;
import com.github.zilosz.ssl.attribute.AbilityType;
import com.github.zilosz.ssl.kit.KitType;
import com.github.zilosz.ssl.util.file.FileUtility;
import dev.dejvokep.boostedyaml.YamlDocument;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class Resources {
  @Getter private final YamlDocument items;
  @Getter private final YamlDocument lobby;
  @Getter private final YamlDocument config;
  @Getter private final YamlDocument arenas;
  @Getter private final YamlDocument database;

  private final Map<KitType, YamlDocument> kits = new HashMap<>();
  private final Map<AbilityType, YamlDocument> abilities = new HashMap<>();

  public Resources() {
    config = FileUtility.loadYaml(SSL.getInstance(), "config");
    items = FileUtility.loadYaml(SSL.getInstance(), "items");
    lobby = FileUtility.loadYaml(SSL.getInstance(), "lobby");
    arenas = FileUtility.loadYaml(SSL.getInstance(), "arenas");
    database = FileUtility.loadYaml(SSL.getInstance(), "database");

    for (KitType kitType : KitType.values()) {
      String path = FileUtility.buildPath("kits", kitType.getFileName());
      kits.put(kitType, FileUtility.loadYaml(SSL.getInstance(), path));
    }

    for (AbilityType abilityType : AbilityType.values()) {
      String path = FileUtility.buildPath("abilities", abilityType.getFileName());
      abilities.put(abilityType, FileUtility.loadYaml(SSL.getInstance(), path));
    }
  }

  public void reload() {
    FileUtility.reloadYaml(lobby);
    FileUtility.reloadYaml(config);
    FileUtility.reloadYaml(arenas);
    FileUtility.reloadYaml(items);

    kits.values().forEach(FileUtility::reloadYaml);
    abilities.values().forEach(FileUtility::reloadYaml);
  }

  public YamlDocument getKitConfig(KitType type) {
    return kits.get(type);
  }

  public YamlDocument getAbilityConfig(AbilityType type) {
    return abilities.get(type);
  }
}
