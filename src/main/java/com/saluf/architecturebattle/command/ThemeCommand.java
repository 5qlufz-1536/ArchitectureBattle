package com.saluf.architecturebattle.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class ThemeCommand {

    private static final int SHUFFLE_ITERATIONS = 15;
    private static final long SHUFFLE_DELAY_MILLIS = 250L;
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private ThemeCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("theme")
                .then(CommandManager.argument("themes", StringArgumentType.greedyString())
                        .executes(context -> {
                            String rawInput = StringArgumentType.getString(context, "themes").trim();
                            if (rawInput.isEmpty()) {
                                context.getSource().sendError(Text.literal("テーマを1つ以上入力してください。"));
                                return 0;
                            }

                            String[] themes = WHITESPACE.split(rawInput);
                            if (themes.length == 0) {
                                context.getSource().sendError(Text.literal("テーマを1つ以上入力してください。"));
                                return 0;
                            }

                            shuffleAndSelectTheme(context.getSource().getServer(), Arrays.asList(themes));
                            return themes.length;
                        })));
    }

    private static void shuffleAndSelectTheme(MinecraftServer server, List<String> themes) {
        if (themes.size() == 1) {
            announceSelection(server, themes.getFirst());
            return;
        }

        CompletableFuture.runAsync(() -> runShuffle(server, themes));
    }

    private static void runShuffle(MinecraftServer server, List<String> themes) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < SHUFFLE_ITERATIONS; i++) {
            String currentTheme = themes.get(random.nextInt(themes.size()));
            server.execute(() -> broadcastShuffle(server, currentTheme));
            sleepQuietly();
        }

        String selectedTheme = themes.get(random.nextInt(themes.size()));
        server.execute(() -> announceSelection(server, selectedTheme));
    }

    private static void broadcastShuffle(MinecraftServer server, String theme) {
        broadcastToPlayers(server, player -> {
            sendTitle(player, Text.literal(theme), Text.literal("お題をシャッフル中...").formatted(Formatting.GRAY));
            playSound(player, SoundEvents.BLOCK_BUBBLE_COLUMN_BUBBLE_POP);
        });
    }

    private static void announceSelection(MinecraftServer server, String theme) {
        Text subtitle = Text.literal("- お題 -").formatted(Formatting.GOLD);
        Text chatMessage = Text.literal("お題: ").formatted(Formatting.GREEN)
                .append(Text.literal(theme).formatted(Formatting.WHITE));

        broadcastToPlayers(server, player -> {
            sendTitle(player, Text.literal(theme).formatted(Formatting.WHITE), subtitle);
            playSound(player, SoundEvents.ENTITY_PLAYER_LEVELUP);
            player.sendMessage(chatMessage, false);
        });
    }

    private static void broadcastToPlayers(MinecraftServer server, Consumer<ServerPlayerEntity> consumer) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            consumer.accept(player);
        }
    }

    private static void sendTitle(ServerPlayerEntity player, Text title, Text subtitle) {
        player.networkHandler.sendPacket(new TitleS2CPacket(title));
        player.networkHandler.sendPacket(new SubtitleS2CPacket(subtitle));
    }

    private static void playSound(ServerPlayerEntity player, SoundEvent soundEvent) {
        player.playSound(soundEvent, 1.0F, 1.0F);
    }

    private static void sleepQuietly() {
        try {
            Thread.sleep(SHUFFLE_DELAY_MILLIS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
