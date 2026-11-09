package com.grandline;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

public final class ClientEvents {
    public static final KeyMapping[] KEYS = new KeyMapping[6];
    private static final Set<Integer> GLOWING = new HashSet<>();

    private ClientEvents() {}

    @Mod.EventBusSubscriber(modid = GrandLine.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class ModBus {
        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            int[] codes = {GLFW.GLFW_KEY_Z, GLFW.GLFW_KEY_X, GLFW.GLFW_KEY_C, GLFW.GLFW_KEY_G, GLFW.GLFW_KEY_V, GLFW.GLFW_KEY_B};
            String[] names = {"ability1", "ability2", "ability3", "armament", "observation", "conqueror"};
            for (int i = 0; i < 6; i++) {
                KEYS[i] = new KeyMapping("key.grandline." + names[i], codes[i], "key.categories.grandline");
                e.register(KEYS[i]);
            }
        }

        @SubscribeEvent
        public static void overlay(RegisterGuiOverlaysEvent e) {
            e.registerAboveAll("grandline_hud", ClientEvents::renderHud);
        }
    }

    @Mod.EventBusSubscriber(modid = GrandLine.MODID, value = Dist.CLIENT)
    public static final class ForgeBus {
        @SubscribeEvent
        public static void tick(TickEvent.ClientTickEvent e) {
            if (e.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            ClientState.tick();
            if (mc.player == null || mc.level == null) { GLOWING.clear(); return; }

            if (mc.screen == null) {
                for (int i = 0; i < 6; i++) {
                    if (KEYS[i] != null && KEYS[i].consumeClick()) Network.CHANNEL.sendToServer(new ActionPacket(i));
                }
            }

            // Haki da Observacao: contorno brilhante em todos os seres vivos proximos (so no seu cliente)
            for (int id : GLOWING) {
                Entity en = mc.level.getEntity(id);
                if (en != null) en.setGlowingTag(false);
            }
            GLOWING.clear();
            if (ClientState.obs) {
                for (Entity en : mc.level.entitiesForRendering()) {
                    if (en instanceof LivingEntity && en != mc.player && en.distanceToSqr(mc.player) < 28 * 28) {
                        en.setGlowingTag(true);
                        GLOWING.add(en.getId());
                    }
                }
            }
        }

        @SubscribeEvent
        public static void camera(ViewportEvent.ComputeCameraAngles e) {
            if (ClientState.shakeTicks <= 0) return;
            float amp = ClientState.shakeIntensity * ((float) ClientState.shakeTicks / ClientState.shakeMax);
            double t = (ClientState.shakeMax - ClientState.shakeTicks) * 1.9;
            e.setPitch(e.getPitch() + (float) Math.sin(t * 1.3) * amp);
            e.setYaw(e.getYaw() + (float) Math.cos(t * 1.1) * amp * 0.8f);
            e.setRoll(e.getRoll() + (float) Math.sin(t * 0.9) * amp * 1.4f);
        }
    }

    // ---------------------------------------------------------------- HUD

    private static void renderHud(ForgeGui gui, GuiGraphics g, float partial, int w, int h) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null) return;
        float time = (mc.player.tickCount + partial) * 0.15f;

        // Bordas do Haki do Armamento (escuro pulsante) e da Observacao (ciano)
        if (ClientState.arm) {
            int a = 0x50 + (int) (Math.sin(time) * 0x18);
            int c = (a << 24);
            g.fillGradient(0, 0, w, h / 5, c, 0x00000000);
            g.fillGradient(0, h - h / 5, w, h, 0x00000000, c);
        }
        if (ClientState.obs) {
            int a = 0x38 + (int) (Math.sin(time * 1.3) * 0x10);
            int c = (a << 24) | 0x00BFFF;
            g.fillGradient(0, 0, w, h / 7, c, 0x0000BFFF);
            g.fillGradient(0, h - h / 7, w, h, 0x0000BFFF, c);
        }
        // Flash do Haki do Rei / impactos
        if (ClientState.flash > 0) {
            int a = (int) (Math.min(1f, ClientState.flash) * 170);
            g.fill(0, 0, w, h, (a << 24) | (ClientState.flashColor & 0xFFFFFF));
        }

        // Painel de habilidades
        int x = w - 140, y = h - 108;
        Fruit f = Fruit.byId(ClientState.fruit);
        if (f != null) {
            g.drawString(mc.font, f.displayName(), x, y - 12, 0xFF000000 | f.color, true);
            for (int i = 0; i < 3; i++) {
                row(g, mc, x, y + i * 16, KEYS[i], f.abilityName(i), ClientState.cd[i], ClientState.maxCd(i), 0xFF000000 | f.color, false);
            }
        }
        int hy = y + 52;
        row(g, mc, x, hy, KEYS[3], Component.translatable("haki.grandline.armament"), 0, 1, 0xFF8A3CFF, ClientState.arm);
        row(g, mc, x, hy + 16, KEYS[4], Component.translatable("haki.grandline.observation"), 0, 1, 0xFF4FE8FF, ClientState.obs);
        row(g, mc, x, hy + 32, KEYS[5], Component.translatable("haki.grandline.conqueror"), ClientState.cd[3], ClientState.maxCd(3), 0xFFD0182A, false);
    }

    private static void row(GuiGraphics g, Minecraft mc, int x, int y, KeyMapping key, Component name,
                            int remaining, int max, int color, boolean toggled) {
        boolean ready = remaining <= 0;
        String k = key == null ? "?" : key.getTranslatedKeyMessage().getString();
        Component label = Component.literal("[" + k + "] ").append(name);
        if (toggled) label = label.copy().append(" ").append(Component.translatable("hud.grandline.on"));
        g.drawString(mc.font, label, x, y, ready ? 0xFFFFFFFF : 0xFF999999, true);
        g.fill(x, y + 10, x + 120, y + 12, 0x66000000);
        float frac = toggled ? 1f : (max <= 0 ? 1f : 1f - (float) remaining / max);
        g.fill(x, y + 10, x + (int) (120 * frac), y + 12, color);
    }
}
