package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class ChargedRightClickAbility extends RightClickAbility {
    protected int ticksCharging = 0;

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.ticksCharging = 1;
        this.onInitialClick(event);

        if (this.getEnergyCost() == 0 && this.showsExpBar()) {
            this.player.setExp(this.doesProgressIncrease() ? 0 : 1);
        }
    }

    @Override
    public boolean startsCooldownInstantly() {
        return false;
    }

    @Override
    public void run() {
        super.run();

        if (this.ticksCharging == 0) return;

        if (!this.player.isBlocking() || this.endsChargeInstantly() && this.ticksCharging >= this.getMaxChargeTicks()) {
            this.onChargeEnd();
            return;
        }

        if (this.player.getExp() >= this.getEnergyCost()) {
            this.onChargeTick();

            if (this.showsExpBar()) {
                this.player.setExp(this.player.getExp() - this.getEnergyCost());

                if (this.getEnergyCost() == 0 && this.getMaxChargeTicks() < Integer.MAX_VALUE) {
                    float percent;

                    if (this.doesProgressIncrease()) {
                        percent = this.ticksCharging;

                    } else {
                        percent = this.getMaxChargeTicks() - this.ticksCharging;
                    }

                    this.player.setExp(percent / this.getMaxChargeTicks());
                }
            }
        }

        this.ticksCharging++;
    }

    public boolean endsChargeInstantly() {
        return this.config.getOptionalBoolean("EndChargeInstantly").orElse(true);
    }

    public int getMaxChargeTicks() {
        return this.config.getOptionalInt("MaxChargeTicks").orElse(Integer.MAX_VALUE);
    }

    public void onChargeEnd() {

        if (this.ticksCharging < this.getMinChargeTicks()) {
            this.onFailedCharge();

        } else {
            this.onSuccessfulCharge();
        }

        this.ticksCharging = 0;

        if (this.getEnergyCost() == 0 && this.showsExpBar()) {
            this.player.setExp(0);
        }

        this.startCooldown();
    }

    public void onChargeTick() {}

    public int getMinChargeTicks() {
        return this.config.getInt("MinChargeTicks");
    }

    public void onFailedCharge() {}

    public void onSuccessfulCharge() {}

    public void onInitialClick(PlayerInteractEvent event) {}

    public boolean showsExpBar() {
        return this.config.getOptionalBoolean("ShowExp").orElse(true);
    }

    public boolean doesProgressIncrease() {
        return this.config.getOptionalBoolean("ExpIncreases").orElse(true);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (this.ticksCharging > 0) {
            this.onChargeEnd();
        }
    }

    @Override
    public String getUseType() {
        return "Hold Right Click";
    }

    @Override
    public boolean invalidate(PlayerInteractEvent event) {
        return super.invalidate(event) || this.ticksCharging > 0;
    }
}
