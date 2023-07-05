package com.github.zilosz.ssl.attribute;

import org.bukkit.event.player.PlayerInteractEvent;

public abstract class ChargedRightClickAbility extends RightClickAbility {
    protected boolean showExpBar;
    protected boolean expIncreases;
    protected int minChargeTicks;
    protected int maxChargeTicks;
    protected boolean endChargeInstantly;
    protected int ticksCharging = 0;
    protected boolean startCooldownAfterCharge;

    @Override
    public void activate() {
        super.activate();

        this.showExpBar = this.config.getOptionalBoolean("ShowExp").orElse(true);
        this.expIncreases = this.config.getOptionalBoolean("ExpIncreases").orElse(true);
        this.minChargeTicks = this.config.getInt("MinChargeTicks");
        this.maxChargeTicks = this.config.getOptionalInt("MaxChargeTicks").orElse(Integer.MAX_VALUE);
        this.endChargeInstantly = this.config.getOptionalBoolean("EndChargeInstantly").orElse(true);
        this.startCooldownAfterCharge = this.config.getOptionalBoolean("StartCooldownAfterCharge").orElse(true);

        this.autoStartCooldown = false;
    }

    @Override
    public void onClick(PlayerInteractEvent event) {
        this.ticksCharging = 1;
        this.onInitialClick(event);

        if (this.energyCost == 0 && this.showExpBar) {
            this.player.setExp(this.expIncreases ? 0 : 1);
        }
    }

    public void onInitialClick(PlayerInteractEvent event) {}

    @Override
    public void run() {
        super.run();

        if (this.ticksCharging == 0) return;

        if (!this.player.isBlocking() || this.endChargeInstantly && this.ticksCharging >= this.maxChargeTicks) {
            this.onChargeEnd();
            return;
        }

        if (this.player.getExp() >= this.energyCost) {
            this.onChargeTick();

            if (this.showExpBar) {
                this.player.setExp(this.player.getExp() - this.energyCost);

                if (this.energyCost == 0 && this.maxChargeTicks < Integer.MAX_VALUE) {
                    float percent = this.expIncreases ? this.ticksCharging : this.maxChargeTicks - this.ticksCharging;
                    this.player.setExp(percent / this.maxChargeTicks);
                }
            }
        }

        this.ticksCharging++;
    }

    public void onChargeEnd() {
        this.onGeneralCharge();

        if (this.ticksCharging < this.minChargeTicks) {
            this.onFailedCharge();

        } else {
            this.onSuccessfulCharge();
        }

        this.ticksCharging = 0;

        if (this.energyCost == 0 && this.showExpBar) {
            this.player.setExp(0);
        }

        if (this.startCooldownAfterCharge) {
            this.startCooldown();
        }
    }

    public void onChargeTick() {}

    public void onGeneralCharge() {}

    public void onFailedCharge() {}

    public void onSuccessfulCharge() {}

    @Override
    public void deactivate() {
        if (this.ticksCharging > 0) {
            this.onChargeEnd();
        }
        super.deactivate();
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
