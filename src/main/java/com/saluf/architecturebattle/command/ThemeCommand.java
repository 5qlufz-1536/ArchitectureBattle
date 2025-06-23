package com.saluf.architecturebattle.command;

import com.saluf.architecturebattle.util.VersionUtil;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;

public class ThemeCommand {

    public static void shuffleAndSelectTheme(MinecraftServer server, String[] themes) {
        Thread shuffleThread = new Thread(() -> {
            try {
                Random random = new Random();
                for (int i = 0; i < 15; i++) { // 0.25秒 x 15 = 3.75秒
                    String currentTheme = themes[random.nextInt(themes.length)];
                    
                    // バージョン特有のロジックに対応
                    RegistryEntry<SoundEvent> soundevent_shuffle;
                    try {
                        soundevent_shuffle = Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
                    } catch (Exception e) {
                        soundevent_shuffle = Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME);
                        System.err.println("サウンド取得中にエラーが発生しました: " + e.getMessage());
                    }
                    
                    final RegistryEntry<SoundEvent> finalSoundEvent = soundevent_shuffle;
                    
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        try {
                            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(currentTheme)));
                            player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("お題をシャッフル中...")));
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                    finalSoundEvent,
                                    SoundCategory.PLAYERS,
                                    player.getPos().x,
                                    player.getPos().y,
                                    player.getPos().z,
                                    1.0F,
                                    1.0F,
                                    5));
                        } catch (Exception e) {
                            System.err.println("プレイヤーへのパケット送信中にエラーが発生しました: " + e.getMessage());
                        }
                    }

                    Thread.sleep(250); // 0.25秒ごとにシャッフル
                }

                // 最終的に1つのテーマを選択
                String selectedTheme = themes[random.nextInt(themes.length)];
                RegistryEntry<SoundEvent> soundevent_theme = null;
                
                try {
                    soundevent_theme = Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_PLAYER_LEVELUP);
                } catch (Exception e) {
                    System.err.println("最終サウンド取得中にエラーが発生しました: " + e.getMessage());
                }
                
                final RegistryEntry<SoundEvent> finalThemeSound = soundevent_theme;

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    try {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(selectedTheme)));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("- お題 -").formatted(Formatting.GOLD)));
                        
                        if (finalThemeSound != null) {
                            player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                    finalThemeSound,
                                    SoundCategory.PLAYERS,
                                    player.getPos().x,
                                    player.getPos().y,
                                    player.getPos().z,
                                    1.0F,
                                    1.0F,
                                    5));
                        }
                    } catch (Exception e) {
                        System.err.println("結果通知中にエラーが発生しました: " + e.getMessage());
                    }
                }
                
                Thread.sleep(250);
                
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    try {
                        sendMessageSafely(player, Text.literal("§aお題: §f" + selectedTheme));
                    } catch (Exception e) {
                        System.err.println("お題表示中にエラーが発生しました: " + e.getMessage());
                    }
                }

            } catch (InterruptedException e) {
                System.err.println("シャッフル処理が中断されました: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("予期せぬエラーが発生しました: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        // スレッドをデーモン化して、サーバー停止時に強制終了されるようにする
        shuffleThread.setDaemon(true);
        shuffleThread.start();
    }

    private static void sendMessageSafely(ServerPlayerEntity player, Text message) {
        try {
            if (VersionUtil.isMinecraft1215() || VersionUtil.isMinecraft1216()) {
                // 1.21.5/1.21.6ではリフレクションを使用してメソッドを呼び出す
                try {
                    // 新しいメソッドのシグネチャでメッセージを送信する
                    player.getClass().getMethod("sendMessage", Text.class, boolean.class)
                        .invoke(player, message, false);
                } catch (Exception e1) {
                    try {
                        // 別のメソッド名やシグネチャの可能性を試す
                        player.getClass().getMethod("sendSystemMessage", Text.class)
                            .invoke(player, message);
                    } catch (Exception e2) {
                        // すべての方法が失敗した場合、ログに記録
                        System.err.println("メッセージ送信に失敗しました: " + e1.getMessage() + ", " + e2.getMessage());
                    }
                }
            } else {
                // 1.21.4以前のバージョンではシンプルなsendMessageを使用
                player.sendMessage(message);
            }
        } catch (Exception e) {
            System.err.println("プレイヤーへのメッセージ送信中にエラーが発生しました: " + e.getMessage());
        }
    }
}