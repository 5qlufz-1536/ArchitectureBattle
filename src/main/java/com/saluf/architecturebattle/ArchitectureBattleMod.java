package com.saluf.architecturebattle;

import com.saluf.architecturebattle.command.ThemeCommand;
import com.saluf.architecturebattle.command.TimerCommand;
import com.saluf.architecturebattle.manager.TimerManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class ArchitectureBattleMod implements ModInitializer {

    private final TimerManager timerManager = new TimerManager();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ThemeCommand.register(dispatcher);
            TimerCommand.register(dispatcher, timerManager);
        });

        timerManager.registerPlayerJoinCallback();
        timerManager.registerPlayerLeaveCallback();
    }
}
