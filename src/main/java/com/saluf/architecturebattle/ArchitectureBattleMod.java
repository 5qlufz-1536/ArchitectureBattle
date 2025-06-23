package com.saluf.architecturebattle;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.saluf.architecturebattle.command.ThemeCommand;
import com.saluf.architecturebattle.command.TimerCommand;
import com.saluf.architecturebattle.manager.TimerManager;
import com.saluf.architecturebattle.util.VersionUtil;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.SharedConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArchitectureBattleMod implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("architecturebattle");

    @Override
    public void onInitialize() {
        // バージョン互換性チェック
        String version = SharedConstants.getGameVersion().getName();
        LOGGER.info("Minecraft version: " + version);

        if (VersionUtil.isMinecraft1215()) {
            LOGGER.info("Minecraft 1.21.5を検出しました。特別な処理を適用します。");
        } else if (VersionUtil.isMinecraft1216()) {
            LOGGER.info("Minecraft 1.21.6を検出しました。特別な処理を適用します。");
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("theme")
                    .then(CommandManager.argument("themes", StringArgumentType.greedyString())
                            .executes(context -> {
                                try {
                                    String themes = StringArgumentType.getString(context, "themes");
                                    handleThemeCommand(context.getSource(), themes.split(" "));
                                } catch (Exception e) {
                                    LOGGER.error("テーマコマンド実行中にエラーが発生しました: " + e.getMessage());
                                    e.printStackTrace();
                                }
                                return 1;
                            })));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TimerCommand.register(dispatcher);
            TimerManager timerManager = new TimerManager();
            timerManager.registerPlayerJoinCallback();
            timerManager.registerPlayerLeaveCallback();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LOGGER.info("サーバーシャットダウン中、リソースをクリーンアップします...");
        });
    }

    private void handleThemeCommand(ServerCommandSource source, String[] themes) {
        ThemeCommand.shuffleAndSelectTheme(source.getServer(), themes);
    }
}
