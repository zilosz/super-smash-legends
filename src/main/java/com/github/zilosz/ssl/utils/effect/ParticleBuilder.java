package com.github.zilosz.ssl.utils.effect;

import com.github.zilosz.ssl.utils.NmsUtils;
import com.github.zilosz.ssl.utils.math.MathUtils;
import com.github.zilosz.ssl.utils.math.VectorUtils;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ParticleBuilder {
    private final EnumParticle particle;
    private float spreadX = 0;
    private float spreadY = 0;
    private float spreadZ = 0;
    private int r = 0;
    private int g = 0;
    private int b = 0;
    private BlockFace face;

    public ParticleBuilder(EnumParticle particle) {
        this.particle = particle;
    }

    public ParticleBuilder setSpread(float x, float y, float z) {
        this.spreadX = x;
        this.spreadY = y;
        this.spreadZ = z;
        return this;
    }

    public ParticleBuilder setRgb(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        return this;
    }

    public ParticleBuilder setFace(BlockFace face) {
        this.face = face;
        return this;
    }

    public void solidSphere(Location center, double radius, int particleCount, double radiusStep) {
        for (double r = 0; r <= radius; r += radiusStep) {
            this.hollowSphere(center, r, particleCount);
        }
    }

    public void hollowSphere(Location center, double radius, int particleCount) {
        for (int i = 0; i < particleCount; i++) {
            this.show(center.clone().add(VectorUtils.getRandomVector(this.face).multiply(radius)));
        }
    }

    public void show(Location location) {
        float x = (float) MathUtils.randSpread(location.getX(), this.spreadX);
        float y = (float) MathUtils.randSpread(location.getY(), this.spreadY);
        float z = (float) MathUtils.randSpread(location.getZ(), this.spreadZ);

        float red = this.r == 0 ? 0.001f : this.r / 255f;
        float green = this.g / 255f;
        float blue = this.b / 255f;

        PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(
                this.particle,
                true,
                x,
                y,
                z,
                red,
                green,
                blue,
                1,
                0,
                1
        );

        Bukkit.getOnlinePlayers().forEach(player -> NmsUtils.sendPacket(player, packet));
    }

    public void ring(Location center, double radius, double gap) {
        this.ring(center, center.getPitch(), center.getYaw(), radius, gap);
    }

    public void ring(Location center, float pitch, float yaw, double radius, double gap) {
        this.ring(center, pitch, yaw, radius, gap, 0);
    }

    public void ring(Location center, float pitch, float yaw, double radius, double gap, double startRadians) {
        for (double radians = startRadians; radians < 2 * Math.PI + startRadians; radians += 2 * Math.PI / gap) {
            this.show(MathUtils.ringPoint(center, pitch, yaw, radius, radians));
        }
    }

    public void verticalRing(Location center, double radius, double gap) {
        for (double radians = 0; radians < 2 * Math.PI; radians += 2 * Math.PI / gap) {
            Location loc = center.clone();
            loc.setPitch((float) ((Math.sin(radians) + 1) * 180));
            loc.setYaw(radians > Math.PI ? center.getYaw() : center.getYaw() - 180);
            this.show(loc.add(loc.getDirection().multiply(radius)));
        }
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

                    ParticleBuilder.this.show(curr);
                    curr.add(step);
                    this.stepped += radiusStep;
                }

            }.runTaskTimer(plugin, 0, 0);
        }
    }
}
