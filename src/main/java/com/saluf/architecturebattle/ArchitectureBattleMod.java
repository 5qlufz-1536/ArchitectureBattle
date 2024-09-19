package com.saluf.architecturebattle;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.saluf.architecturebattle.command.ThemeCommand;
import com.saluf.architecturebattle.command.TimerCommand;
import com.saluf.architecturebattle.manager.TimerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ArchitectureBattleMod implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("theme")
                    .then(CommandManager.argument("themes", StringArgumentType.greedyString())
                            .executes(context -> {
                                String themes = StringArgumentType.getString(context, "themes");
                                handleThemeCommand(context.getSource(), themes.split(" "));
                                return 1;
                            })));
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
            TimerCommand.register(dispatcher);
            TimerManager timerManager = new TimerManager();
            timerManager.registerPlayerJoinCallback();
            timerManager.registerPlayerLeaveCallback();
        });
    }

    private void handleThemeCommand(ServerCommandSource source, String[] themes) {
        ThemeCommand.shuffleAndSelectTheme(source.getServer(), themes);
    }
}
