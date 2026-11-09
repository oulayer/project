package com.grandline;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/** Servidor -> dono: estado da fruta, haki e cooldowns para o HUD. */
public class SyncPacket {
    public final String fruit;
    public final boolean arm, obs;
    public final int[] cd;

    public SyncPacket(String fruit, boolean arm, boolean obs, int[] cd) {
        this.fruit = fruit; this.arm = arm; this.obs = obs; this.cd = cd;
    }

    public static void encode(SyncPacket m, FriendlyByteBuf b) {
        b.writeUtf(m.fruit); b.writeBoolean(m.arm); b.writeBoolean(m.obs);
        for (int i = 0; i < 4; i++) b.writeVarInt(m.cd[i]);
    }

    public static SyncPacket decode(FriendlyByteBuf b) {
        String f = b.readUtf(); boolean a = b.readBoolean(), o = b.readBoolean();
        int[] cd = new int[4];
        for (int i = 0; i < 4; i++) cd[i] = b.readVarInt();
        return new SyncPacket(f, a, o, cd);
    }

    public static void handle(SyncPacket m, Supplier<NetworkEvent.Context> s) {
        NetworkEvent.Context c = s.get();
        c.enqueueWork(() -> ClientState.applySync(m));
        c.setPacketHandled(true);
    }
}
