package com.saluf.architecturebattle.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.saluf.architecturebattle.manager.TimerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TimerCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("timer")
                .then(CommandManager.argument("minutes", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            int minutes = IntegerArgumentType.getInteger(context, "minutes");
                            TimerManager.startTimer(context.getSource().getServer(), minutes);
                            context.getSource().sendMessage(Text.literal("タイマーを" + minutes + "分に設定して開始しました。"));
                            return 1;
                        }))
                .then(CommandManager.literal("start").executes(context -> {
                    TimerManager.resumeTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.literal("タイマーを再開しました。"));
                    return 1;
                }))
                .then(CommandManager.literal("stop").executes(context -> {
                    TimerManager.stopTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.literal("タイマーを一時停止しました。"));
                    return 1;
                }))
                .then(CommandManager.literal("reset").executes(context -> {
                    TimerManager.resetTimer(context.getSource().getServer());
                    context.getSource().sendMessage(Text.literal("タイマーがリセットされました。").formatted(Formatting.GRAY));
                    return 1;
                })));
    }
}
