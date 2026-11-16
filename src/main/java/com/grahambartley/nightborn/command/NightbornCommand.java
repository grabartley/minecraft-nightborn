package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.server.ConfigEditPermission;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class NightbornCommand {

  private NightbornCommand() {}

  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> register(dispatcher));
  }

  static void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal("nightborn")
            .requires(source -> source.hasPermissionLevel(ConfigEditPermission.OPERATOR_LEVEL))
            .executes(NightbornCommand::help)
            .then(CommandManager.literal("status").executes(NightbornCommand::status))
            .then(ConfigCommand.node())
            .then(PlayerCommand.node())
            .then(FamiliarCommand.node())
            .then(ThrallCommand.node()));
  }

  static List<Text> helpLines() {
    return List.of(
        Text.translatable("nightborn.command.help.header"),
        Text.translatable("nightborn.command.help.status"),
        Text.translatable("nightborn.command.help.config"),
        Text.translatable("nightborn.command.help.reset"),
        Text.translatable("nightborn.command.help.turn"),
        Text.translatable("nightborn.command.help.cure"),
        Text.translatable("nightborn.command.help.info"),
        Text.translatable("nightborn.command.help.skills"),
        Text.translatable("nightborn.command.help.familiar"),
        Text.translatable("nightborn.command.help.thrall"),
        Text.translatable("nightborn.command.help.thrall_free"));
  }

  private static int help(final CommandContext<ServerCommandSource> context) {
    helpLines().forEach(line -> context.getSource().sendFeedback(() -> line, false));
    return 1;
  }

  private static int status(final CommandContext<ServerCommandSource> context) {
    SettingText.status(ServerConfigService.current())
        .forEach(line -> context.getSource().sendFeedback(() -> line, false));
    return 1;
  }
}
