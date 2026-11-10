package com.baza.auto;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class BazaModClient implements ClientModInitializer {

    public static boolean active = false;
    public static int bindKey = GLFW.GLFW_KEY_G;

    private boolean keyWasDown = false;
    private BlockPos breakTarget = null;
    private long breakStartMs = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            keyWasDown = false;
            return;
        }

        boolean keyDown = client.currentScreen == null
                && InputUtil.isKeyPressed(client.getWindow().getHandle(), bindKey);
        if (keyDown && !keyWasDown) {
            active = !active;
            msg(client, active
                    ? "Мод ВКЛЮЧЁН. Иди вперёд — я копаю тоннель."
                    : "Мод ВЫКЛЮЧЕН.");
        }
        keyWasDown = keyDown;

        if (!active || client.currentScreen != null || client.interactionManager == null) return;
        tunnel(client);
    }

    private void tunnel(MinecraftClient client) {
        var player = client.player;
        var world = player.getWorld();
        Direction dir = player.getHorizontalFacing();
        BlockPos playerPos = player.getBlockPos();
        BlockPos feet = playerPos.offset(dir);
        BlockPos head = feet.up();
        BlockPos top = head.up();

        for (int d = 2; d <= 3; d++) {
            BlockPos column = playerPos.offset(dir, d);
            for (int dy = -2; dy <= 2; dy++) {
                if (world.getBlockState(column.up(dy)).getFluidState().isIn(FluidTags.LAVA)) {
                    stop(client, "ЛАВА! Мод остановился для твоей безопасности.");
                    return;
                }
            }
        }

        BlockPos[] targets = {feet, head, top};
        for (BlockPos pos : targets) {
            BlockState state = world.getBlockState(pos);
            if (state.isAir() || state.getFluidState().isIn(FluidTags.WATER)) continue;

            if (state.getHardness(world, pos) < 0) {
                stop(client, "БАЗА! Блок неразрушим (бедрок или приват) — стоп.");
                overlay(client, "БАЗА");
                return;
            }

            if (!pos.equals(breakTarget)) {
                breakTarget = pos;
                breakStartMs = System.currentTimeMillis();
            }
            boolean done = client.interactionManager.updateBlockBreakingProgress(pos, dir);
            if (System.currentTimeMillis() - breakStartMs > 2000) {
                stop(client, "БАЗА! Блок не ломается — скорее всего это чужой приват.");
                overlay(client, "БАЗА");
                return;
            }
            if (done) breakTarget = null;
            return;
        }
        breakTarget = null;
    }

    private void stop(MinecraftClient client, String reason) {
        active = false;
        msg(client, reason);
    }

    private void overlay(MinecraftClient client, String text) {
        client.inGameHud.setOverlayMessage(Text.literal(text), false);
    }

    public static void msg(MinecraftClient client, String text) {
        client.inGameHud.getChatHud().addMessage(Text.literal("[BazaMod] " + text));
    }

    public static void handleCommand(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        String[] parts = message.trim().split("\\s+");

        if (parts.length == 1) {
            active = !active;
            msg(client, active ? "Мод ВКЛЮЧЁН." : "Мод ВЫКЛЮЧЕН.");
        } else if (parts[1].equalsIgnoreCase("key") && parts.length >= 3) {
            int code = parseKey(parts[2].toUpperCase());
            if (code != -1) {
                bindKey = code;
                msg(client, "Клавиша: " + parts[2].toUpperCase());
            } else {
                msg(client, "Не понял клавишу. Пример: .baza key G");
            }
        } else if (parts[1].equalsIgnoreCase("menu")) {
            client.setScreen(new BazaScreen());
        }
    }

    private static int parseKey(String s) {
        if (s.length() == 1) {
            char c = s.charAt(0);
            if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
            if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }
        return -1;
    }
}
