package com.saluf.architecturebattle.manager;

import com.saluf.architecturebattle.util.VersionUtil;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TimerManager {
    private static ServerBossBar bossBar;
    private static int totalTicks; // タイマー全体のティック数
    private static int remainingTicks = 0;
    private static boolean timerRunning = false;
    private static boolean eventRegistered = false; // イベントが登録されているかを追跡
    public static void startTimer(MinecraftServer server, int minutes) {
        // 1分 = 20ティック * 60秒
        remainingTicks = minutes * 20 * 60;
        totalTicks = remainingTicks;
        timerRunning = true;

        if (bossBar == null) {
            bossBar = new ServerBossBar(
                    Text.literal("Remaining Time"),
                    BossBar.Color.GREEN,
                    BossBar.Style.PROGRESS
            );
        }

        bossBar.setPercent(1.0f);

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            bossBar.addPlayer(player);
            if (VersionUtil.isMinecraft1215() || VersionUtil.isMinecraft1216()) {
                playStartSound(player);
            } else {
                playStartSound(player);
            }
        }

        if (!eventRegistered) {
            eventRegistered = true;
            ServerTickEvents.START_SERVER_TICK.register(server1 -> {
                if (timerRunning) {
                    updateTimer(server1);
                }
            });
            
            // サーバーシャットダウン時の処理を登録
            ServerLifecycleEvents.SERVER_STOPPING.register(server1 -> {
                try {
                    cleanupResources();
                } catch (Exception e) {
                    System.err.println("タイマーのクリーンアップ中にエラーが発生しました: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private static void playStartSound(ServerPlayerEntity player) {
        try {
            RegistryEntry.Reference<SoundEvent> hornSoundEntry_1 = SoundEvents.GOAT_HORN_SOUNDS.getFirst();
            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                    hornSoundEntry_1,
                    SoundCategory.PLAYERS,
                    player.getPos().x,
                    player.getPos().y,
                    player.getPos().z,
                    1.0F,
                    1.0F,
                    5));
        } catch (Exception e) {
            System.err.println("サウンド再生中にエラーが発生しました: " + e.getMessage());
        }
    }

    private static void cleanupResources() {
        if (bossBar != null) {
            timerRunning = false;
            totalTicks = 0;
            remainingTicks = 0;
            bossBar = null;
        }
    }

    public static void stopTimer(MinecraftServer server) {
        if (timerRunning) {
            timerRunning = false;
        }
    }
    
    public static void resumeTimer(MinecraftServer server) {
        if (!timerRunning && remainingTicks > 0) {
            timerRunning = true;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                try {
                    RegistryEntry.Reference<SoundEvent> hornSoundEntry_2 = SoundEvents.GOAT_HORN_SOUNDS.get(2);
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                            hornSoundEntry_2,
                            SoundCategory.PLAYERS,
                            player.getPos().x,
                            player.getPos().y,
                            player.getPos().z,
                            1.0F,
                            1.0F,
                            5));
                } catch (Exception e) {
                    System.err.println("サウンド再生中にエラーが発生しました: " + e.getMessage());
                }
            }
        }
    }

    public static void resetTimer(MinecraftServer server) {
        if (timerRunning) {
            timerRunning = false;
            remainingTicks = 0;
            totalTicks = 0;

            if (bossBar != null) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    bossBar.removePlayer(player);}
                bossBar = null;
            }
        }
    }

    private static void updateTimer(MinecraftServer server) {
        int secondsRemaining;
        int minutesRemaining;
        int hoursRemaining;
        int secondsOnly;
        try {
            if (timerRunning && remainingTicks > 0) {
                remainingTicks--;

                float progress = (float) remainingTicks / totalTicks;
                bossBar.setPercent(progress);

                secondsRemaining = remainingTicks / 20;
                minutesRemaining = secondsRemaining / 60;
                hoursRemaining = minutesRemaining / 60;
                secondsOnly = secondsRemaining % 60;
                minutesRemaining = minutesRemaining % 60;

                if (hoursRemaining > 0) {
                    bossBar.setName(Text.literal(String.format("残り時間: %02d:%02d:%02d", hoursRemaining, minutesRemaining, secondsOnly)).formatted(Formatting.BOLD));
                }
                else {
                    bossBar.setName(Text.literal(String.format("残り時間: %02d:%02d", minutesRemaining, secondsOnly)).formatted(Formatting.BOLD));
                }

            } else {
                timerRunning = false;
                remainingTicks = 0;
                totalTicks = 0;

                if (bossBar != null) {
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        try {
                            bossBar.removePlayer(player);
                            RegistryEntry<SoundEvent> soundevent_finish = Registries.SOUND_EVENT.getEntry(SoundEvents.ITEM_TOTEM_USE);
                            // バージョン別の処理でメッセージ送信
                            server.getPlayerManager().broadcast(Text.literal("建築終了！").formatted(Formatting.GOLD), false);
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                    soundevent_finish,
                                    SoundCategory.PLAYERS,
                                    player.getPos().x,
                                    player.getPos().y,
                                    player.getPos().z,
                                    0.8F,
                                    1.0F,
                                    5));
                        } catch (Exception e) {
                            System.err.println("プレイヤーへの通知中にエラーが発生しました: " + e.getMessage());
                        }
                    }
                }
                bossBar = null;
            }
        } catch (Exception e) {
            System.err.println("タイマー更新中にエラーが発生しました: " + e.getMessage());
        }
    }

    public void registerPlayerJoinCallback() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // タイマーが動作中なら、プレイヤーにボスバーを再送信
            if (timerRunning && bossBar != null) {
                bossBar.addPlayer(player);
            }
        });
    }

    public void registerPlayerLeaveCallback() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();

            // ボスバーからプレイヤーを削除
            if (bossBar != null) {
                bossBar.removePlayer(player);
            }
        });
    }
}
