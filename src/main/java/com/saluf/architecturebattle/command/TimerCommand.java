package com.saluf.architecturebattle.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saluf.architecturebattle.manager.TimerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class TimerCommand {

    private TimerCommand() {
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, TimerManager timerManager) {
        dispatcher.register(CommandManager.literal("timer")
                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int minutes = IntegerArgumentType.getInteger(context, "minutes");
                            timerManager.startTimer(context.getSource().getServer(), minutes);
                            context.getSource().sendMessage(Text.translatable("architecturebattle.command.timer.set_and_started", minutes));
                            return 1;
                        }))
                .then(CommandManager.literal("start").executes(context -> {
                    timerManager.resumeTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.translatable("architecturebattle.command.timer.resumed"));
                    return 1;
                }))
                .then(CommandManager.literal("stop").executes(context -> {
                    timerManager.stopTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.translatable("architecturebattle.command.timer.stopped"));
                    return 1;
                }))
                .then(CommandManager.literal("reset").executes(context -> {
                    timerManager.resetTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.translatable("architecturebattle.command.timer.reset"));
                    return 1;
                })));
    }
}
