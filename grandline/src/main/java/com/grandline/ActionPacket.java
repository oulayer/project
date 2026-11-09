package com.grandline;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Cliente -> servidor: 0..2 habilidades, 3 armamento, 4 observacao, 5 rei. */
public class ActionPacket {
    public final int action;

    public ActionPacket(int action) { this.action = action; }

    public static void encode(ActionPacket m, FriendlyByteBuf b) { b.writeVarInt(m.action); }
    public static ActionPacket decode(FriendlyByteBuf b) { return new ActionPacket(b.readVarInt()); }

    public static void handle(ActionPacket m, Supplier<NetworkEvent.Context> s) {
        NetworkEvent.Context c = s.get();
        c.enqueueWork(() -> {
            ServerPlayer p = c.getSender();
            if (p == null || !p.isAlive() || p.isSpectator()) return;
            switch (m.action) {
                case 0, 1, 2 -> Abilities.use(p, m.action);
                case 3 -> Haki.toggleArmament(p);
                case 4 -> Haki.toggleObservation(p);
                case 5 -> Haki.conqueror(p);
                default -> { }
            }
        });
        c.setPacketHandled(true);
    }
}
