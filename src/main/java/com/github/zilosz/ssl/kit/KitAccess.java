package com.github.zilosz.ssl.kit;

import com.github.zilosz.ssl.util.message.MessageUtils;

public enum KitAccess {
  ACCESSIBLE {
    @Override
    public String getLore() {
      return "&7You have access to this kit.";
    }

    @Override
    public String getHologram() {
      return "";
    }
  },

  SELECTED {
    @Override
    public String getLore() {
      return "&7This is your &dcurrent &7kit.";
    }

    @Override
    public String getHologram() {
      return MessageUtils.color("&dSelected");
    }
  };

  public abstract String getLore();

  public abstract String getHologram();
}
