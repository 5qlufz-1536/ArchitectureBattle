package com.saluf.architecturebattle.manager;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.event.GameEvent;

import java.util.function.Consumer;

public class TimerManager {

    private static final int TICKS_PER_SECOND = 20;
    private static final Identifier GOAT_HORN_PLAY_ID = Identifier.of("minecraft", "item.goat_horn.play");
    private static final Identifier[] GOAT_HORN_SOUND_IDS = {
        Identifier.of("minecraft", "item.goat_horn.sound.0"),
        Identifier.of("minecraft", "item.goat_horn.sound.1"),
        Identifier.of("minecraft", "item.goat_horn.sound.2"),
        Identifier.of("minecraft", "item.goat_horn.sound.3"),
        Identifier.of("minecraft", "item.goat_horn.sound.4"),
        Identifier.of("minecraft", "item.goat_horn.sound.5"),
        Identifier.of("minecraft", "item.goat_horn.sound.6"),
        Identifier.of("minecraft", "item.goat_horn.sound.7"),
        GOAT_HORN_PLAY_ID
    };

    private ServerBossBar bossBar;
    private int totalTicks;
    private int remainingTicks;
    private boolean timerRunning;
    private SoundEvent cachedGoatHornSound;

    public TimerManager() {
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);
    }

    public void registerPlayerJoinCallback() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (bossBar != null) {
                bossBar.addPlayer(handler.getPlayer());
            }
        });
    }

    public void registerPlayerLeaveCallback() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (bossBar != null) {
                bossBar.removePlayer(handler.getPlayer());
            }
        });
    }

    public void startTimer(MinecraftServer server, int minutes) {
        remainingTicks = Math.max(minutes, 1) * TICKS_PER_SECOND * 60;
        totalTicks = remainingTicks;
        timerRunning = true;

        if (bossBar == null) {
            bossBar = new ServerBossBar(
                    Text.literal("Remaining Time"),
                    BossBar.Color.GREEN,
                    BossBar.Style.PROGRESS
            );
        }

        bossBar.setPercent(1.0F);
        updateBossBarLabel();
        broadcast(server, player -> {
            bossBar.addPlayer(player);
            playGoatHorn(player);
        });
    }

    public void stopTimer(MinecraftServer server) {
        if (!timerRunning) {
            return;
        }
        timerRunning = false;
        sendChat(server, Text.literal("タイマーを停止しました。"));
    }

    public void resumeTimer(MinecraftServer server) {
        if (timerRunning || remainingTicks <= 0) {
            return;
        }
        timerRunning = true;
        sendChat(server, Text.literal("タイマーを再開しました。"));
        broadcast(server, this::playGoatHorn);
    }

    public void resetTimer(MinecraftServer server) {
        if (bossBar == null) {
            return;
        }
        timerRunning = false;
        remainingTicks = 0;
        totalTicks = 0;
        broadcast(server, player -> {
            bossBar.removePlayer(player);
            player.sendMessage(Text.literal("タイマーがリセットされました。").formatted(Formatting.GRAY), false);
        });
        bossBar = null;
    }

    private void onServerTick(MinecraftServer server) {
        if (!timerRunning) {
            return;
        }

        if (remainingTicks <= 0) {
            finish(server);
            return;
        }

        remainingTicks--;
        float progress = totalTicks == 0 ? 0.0F : (float) remainingTicks / totalTicks;
        bossBar.setPercent(progress);
        updateBossBarLabel();
    }

    private void finish(MinecraftServer server) {
        timerRunning = false;
        remainingTicks = 0;
        totalTicks = 0;

        sendChat(server, Text.literal("建築終了！").formatted(Formatting.GOLD));
        broadcast(server, player -> {
            player.playSound(SoundEvents.ITEM_TOTEM_USE, 1.0F, 1.0F);
            if (bossBar != null) {
                bossBar.removePlayer(player);
            }
        });
        bossBar = null;
    }

    private void updateBossBarLabel() {
        if (bossBar == null) {
            return;
        }

        int secondsRemaining = remainingTicks / TICKS_PER_SECOND;
        int minutesRemaining = secondsRemaining / 60;
        int hoursRemaining = minutesRemaining / 60;
        int secondsOnly = secondsRemaining % 60;
        minutesRemaining %= 60;

        String label = hoursRemaining > 0
                ? String.format("残り時間: %02d:%02d:%02d", hoursRemaining, minutesRemaining, secondsOnly)
                : String.format("残り時間: %02d:%02d", minutesRemaining, secondsOnly);

        bossBar.setName(Text.literal(label).formatted(Formatting.BOLD));
    }

    private void sendChat(MinecraftServer server, Text message) {
        broadcast(server, player -> player.sendMessage(message, false));
    }

    private void broadcast(MinecraftServer server, Consumer<ServerPlayerEntity> consumer) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            consumer.accept(player);
        }
    }

    private void playGoatHorn(ServerPlayerEntity player) {
    ServerWorld world = player.getServerWorld();
    SoundEvent soundEvent = goatHornSound();
    player.playSound(soundEvent, 4.0F, 1.0F);
    world.playSound(null, player.getX(), player.getY(), player.getZ(), soundEvent, SoundCategory.RECORDS, 4.0F, 1.0F);
    world.emitGameEvent(GameEvent.INSTRUMENT_PLAY, player.getPos(), GameEvent.Emitter.of(player));
    }

    private SoundEvent goatHornSound() {
        if (cachedGoatHornSound != null) {
            return cachedGoatHornSound;
        }

        for (Identifier id : GOAT_HORN_SOUND_IDS) {
            SoundEvent fallback = Registries.SOUND_EVENT.getOrEmpty(id).orElse(null);
            if (fallback != null) {
                cachedGoatHornSound = fallback;
                return cachedGoatHornSound;
            }
        }

        cachedGoatHornSound = SoundEvents.ENTITY_PLAYER_LEVELUP;
        return cachedGoatHornSound;
    }
}
