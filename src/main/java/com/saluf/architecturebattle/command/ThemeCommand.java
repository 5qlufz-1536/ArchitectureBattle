package com.saluf.architecturebattle.command;

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
        new Thread(() -> {
            try {
                Random random = new Random();
                for (int i = 0; i < 15; i++) { // 0.25秒 x 15 = 3.75秒
                    String currentTheme = themes[random.nextInt(themes.length)];

                    RegistryEntry<SoundEvent> soundevent_shuffle = Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
                    for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(currentTheme)));
                        player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("お題をシャッフル中...")));
                        player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                                soundevent_shuffle,
                                SoundCategory.PLAYERS,
                                player.getPos().x,
                                player.getPos().y,
                                player.getPos().z,
                                1.0F,
                                1.0F,
                                5));
                    }

                    Thread.sleep(250); // 0.25秒ごとにシャッフル
                }

                // 最終的に1つのテーマを選択
                String selectedTheme = themes[random.nextInt(themes.length)];

                RegistryEntry<SoundEvent> soundevent_theme = Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_PLAYER_LEVELUP);

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(selectedTheme)));
                    player.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal("- お題 -").formatted(Formatting.GOLD)));
                    player.networkHandler.sendPacket(new PlaySoundS2CPacket(
                            soundevent_theme,
                            SoundCategory.PLAYERS,
                            player.getPos().x,
                            player.getPos().y,
                            player.getPos().z,
                            1.0F,
                            1.0F,
                            5));
                }
                Thread.sleep(250); // 0.25秒ごとにシャッフル
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    player.sendMessage(Text.literal("§aお題: §f" + selectedTheme));
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

}
