package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class ChargedRightClickAbility extends RightClickAbility {
  protected int ticksCharging;

  @Override
  public void onClick(PlayerInteractEvent event) {
    ticksCharging = 1;
    onInitialClick(event);

    if (getEnergyCost() == 0 && showsExpBar()) {
      player.setExp(doesProgressIncrease() ? 0 : 1);
    }
  }

  @Override
  public boolean startsCooldownInstantly() {
    return false;
  }

  @Override
  public void run() {
    super.run();

    if (ticksCharging == 0) return;

    if (!player.isBlocking() || endsChargeInstantly() && ticksCharging >= getMaxChargeTicks()) {
      onChargeEnd();
      return;
    }

    if (player.getExp() >= getEnergyCost()) {
      onChargeTick();

      if (showsExpBar()) {
        player.setExp(player.getExp() - getEnergyCost());

        if (getEnergyCost() == 0 && getMaxChargeTicks() < Integer.MAX_VALUE) {
          float percent;

          if (doesProgressIncrease()) {
            percent = ticksCharging;

          }
          else {
            percent = getMaxChargeTicks() - ticksCharging;
          }

          player.setExp(percent / getMaxChargeTicks());
        }
      }
    }

    ticksCharging++;
  }

  public boolean endsChargeInstantly() {
    return config.getOptionalBoolean("EndChargeInstantly").orElse(true);
  }

  public int getMaxChargeTicks() {
    return config.getOptionalInt("MaxChargeTicks").orElse(Integer.MAX_VALUE);
  }

  public void onChargeEnd() {

    if (ticksCharging < getMinChargeTicks()) {
      onFailedCharge();
    }
    else {
      onSuccessfulCharge();
    }

    ticksCharging = 0;

    if (getEnergyCost() == 0 && showsExpBar()) {
      player.setExp(0);
    }

    startCooldown();
  }

  public int getMinChargeTicks() {
    return config.getInt("MinChargeTicks");
  }

  public void onFailedCharge() {}

  public void onSuccessfulCharge() {}

  public void onChargeTick() {}

  public void onInitialClick(PlayerInteractEvent event) {}

  public boolean showsExpBar() {
    return config.getOptionalBoolean("ShowExp").orElse(true);
  }

  public boolean doesProgressIncrease() {
    return config.getOptionalBoolean("ExpIncreases").orElse(true);
  }

  @Override
  public void deactivate() {
    super.deactivate();

    if (ticksCharging > 0) {
      onChargeEnd();
    }
  }

  @Override
  public String getUseType() {
    return "Hold Right Click";
  }

  @Override
  public boolean invalidate(PlayerInteractEvent event) {
    return super.invalidate(event) || ticksCharging > 0;
  }
}
