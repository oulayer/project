package com.grandline;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Utilidades de particulas para as animacoes. */
public final class Fx {
    private Fx() {}

    public static DustParticleOptions dust(int rgb, float size) {
        return new DustParticleOptions(new Vector3f(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f), size);
    }

    public static final DustParticleOptions DARK = dust(0x08080F, 1.7f);
    public static final DustParticleOptions CRIMSON = dust(0xB3101F, 1.9f);
    public static final DustParticleOptions VIOLET = dust(0x6A1FD0, 1.0f);
    public static final DustParticleOptions CYAN = dust(0x6FF2FF, 1.1f);

    public static void p(ServerLevel l, ParticleOptions o, Vec3 v) {
        l.sendParticles(o, v.x, v.y, v.z, 1, 0, 0, 0, 0);
    }

    public static void spread(ServerLevel l, ParticleOptions o, Vec3 v, int count, double spread, double speed) {
        l.sendParticles(o, v.x, v.y, v.z, count, spread, spread, spread, speed);
    }

    public static void ring(ServerLevel l, ParticleOptions o, Vec3 c, double r, int n) {
        for (int i = 0; i < n; i++) {
            double a = Math.PI * 2 * i / n;
            p(l, o, c.add(Math.cos(a) * r, 0, Math.sin(a) * r));
        }
    }

    public static void line(ServerLevel l, ParticleOptions o, Vec3 a, Vec3 b, double step) {
        Vec3 d = b.subtract(a);
        double len = d.length();
        if (len < 1.0E-4) return;
        Vec3 s = d.normalize().scale(step);
        Vec3 pos = a;
        for (double t = 0; t < len; t += step) {
            p(l, o, pos);
            pos = pos.add(s);
        }
    }
}
