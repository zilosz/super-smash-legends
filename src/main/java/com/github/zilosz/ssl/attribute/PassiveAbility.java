package com.github.zilosz.ssl.attribute;

public abstract class PassiveAbility extends Ability {

  @Override
  public void activate() {
    super.activate();
    hotbarItem.destroy();
  }
}
