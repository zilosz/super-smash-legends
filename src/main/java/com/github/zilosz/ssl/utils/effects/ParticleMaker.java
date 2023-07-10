package com.github.zilosz.ssl.utils.effects;

import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import xyz.xenondevs.particle.ParticleBuilder;

public class ParticleMaker {
    private final ParticleBuilder particle;
    private BlockFace face;
    private double spreadX;
    private double spreadY;
    private double spreadZ;

    public ParticleMaker(ParticleBuilder particle) {
        this.particle = particle;
    }

    public ParticleMaker setFace(BlockFace face) {
        this.face = face;
        return this;
    }

    public ParticleMaker setSpread(double spread) {
        return this.setSpread(spread, spread, spread);
    }

    public ParticleMaker setSpread(double spreadX, double spreadY, double spreadZ) {
        this.spreadX = spreadX;
        this.spreadY = spreadY;
        this.spreadZ = spreadZ;
        return this;
    }

    public void boom(Plugin plugin, Location center, double radius, double radiusStep, int streaks) {

        for (int i = 0; i < streaks; i++) {
            Vector step = VectorUtils.getRandomVector(this.face).multiply(radiusStep);
            Location curr = center.clone();

            new BukkitRunnable() {
                double stepped = 0;

                @Override
                public void run() {

                    if (this.stepped > radius) {
                        this.cancel();
                        return;
                    }

                    ParticleMaker.this.show(curr);
                    curr.add(step);
                    this.stepped += radiusStep;
                }

            }.runTaskTimer(plugin, 0, 0);
        }
    }

    public void show(Location location) {
        double x = MathUtils.randSpread(location.getX(), this.spreadX);
        double y = MathUtils.randSpread(location.getY(), this.spreadY);
        double z = MathUtils.randSpread(location.getZ(), this.spreadZ);
        this.particle.setLocation(new Location(location.getWorld(), x, y, z)).display();
    }

    public void ring(Location center, double radius, double gap) {
        this.ring(center, center.getPitch(), center.getYaw(), radius, gap);
    }

    public void ring(Location center, float pitch, float yaw, double radius, double degreeStep) {
        double radianStep = MathUtils.degToRad(degreeStep);

        for (double radians = 0; radians < 2 * Math.PI; radians += radianStep) {
            this.show(MathUtils.ringPoint(center, pitch, yaw, radius, radians));
        }
    }

    public void solidSphere(Location center, double radius, int particleCount, double radiusStep) {
        for (double currRadius = 0; currRadius <= radius; currRadius += radiusStep) {
            this.hollowSphere(center, currRadius, particleCount);
        }
    }

    public void hollowSphere(Location center, double radius, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            this.show(center.clone().add(VectorUtils.getRandomVector(this.face).multiply(radius)));
        }
    }
}
