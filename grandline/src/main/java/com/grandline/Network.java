package com.grandline;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Network {
    private static final String VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GrandLine.MODID, "main"), () -> VERSION, VERSION::equals, VERSION::equals);

    private Network() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, ActionPacket.class, ActionPacket::encode, ActionPacket::decode, ActionPacket::handle);
        CHANNEL.registerMessage(id++, SyncPacket.class, SyncPacket::encode, SyncPacket::decode, SyncPacket::handle);
        CHANNEL.registerMessage(id++, ShakePacket.class, ShakePacket::encode, ShakePacket::decode, ShakePacket::handle);
    }

    public static void sendSync(ServerPlayer p) {
        Fruit f = PlayerData.getFruit(p);
        int[] cd = new int[4];
        for (int i = 0; i < 4; i++) cd[i] = PlayerData.remaining(p, "cd" + i);
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p),
                new SyncPacket(f == null ? "" : f.id, PlayerData.isArmament(p), PlayerData.isObservation(p), cd));
    }

    public static void shake(ServerLevel l, Vec3 c, float intensity, int ticks, int color, float flash, float range) {
        CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(c.x, c.y, c.z, range, l.dimension())),
                new ShakePacket(c.x, c.y, c.z, intensity, ticks, color, flash, range));
    }
}
