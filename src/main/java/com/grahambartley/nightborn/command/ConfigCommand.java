package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.config.BoolSetting;
import com.grahambartley.nightborn.config.IntSetting;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.config.Setting;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class ConfigCommand {

  private static final String VALUE = "value";

  private ConfigCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    final LiteralArgumentBuilder<ServerCommandSource> config = CommandManager.literal("config");
    final Map<String, LiteralArgumentBuilder<ServerCommandSource>> sections = new LinkedHashMap<>();
    for (final Setting<?> setting : NightbornConfig.SETTINGS) {
      sections
          .computeIfAbsent(setting.section(), CommandManager::literal)
          .then(settingNode(setting));
    }
    sections.values().forEach(config::then);
    return config.then(CommandManager.literal("reset").executes(ConfigCommand::reset));
  }

  private static LiteralArgumentBuilder<ServerCommandSource> settingNode(final Setting<?> setting) {
    return CommandManager.literal(setting.key())
        .executes(context -> show(context, setting))
        .then(valueArgument(setting));
  }

  private static ArgumentBuilder<ServerCommandSource, ?> valueArgument(final Setting<?> setting) {
    return switch (setting) {
      case IntSetting number ->
          CommandManager.argument(VALUE, IntegerArgumentType.integer(number.min(), number.max()))
              .executes(
                  context ->
                      apply(context, number, IntegerArgumentType.getInteger(context, VALUE)));
      case BoolSetting flag ->
          CommandManager.argument(VALUE, BoolArgumentType.bool())
              .executes(context -> apply(context, flag, BoolArgumentType.getBool(context, VALUE)));
    };
  }

  private static int show(
      final CommandContext<ServerCommandSource> context, final Setting<?> setting) {
    final ServerCommandSource source = context.getSource();
    source.sendFeedback(() -> SettingText.value(setting, ServerConfigService.current()), false);
    source.sendFeedback(() -> SettingText.range(setting), false);
    return 1;
  }

  private static <T> int apply(
      final CommandContext<ServerCommandSource> context, final Setting<T> setting, final T value) {
    final NightbornConfig updated = ServerConfigService.current().with(setting, value);
    return save(context, updated, () -> SettingText.value(setting, updated));
  }

  private static int reset(final CommandContext<ServerCommandSource> context) {
    return save(
        context,
        NightbornConfig.DEFAULTS,
        () -> Text.translatable("nightborn.command.config.reset"));
  }

  private static int save(
      final CommandContext<ServerCommandSource> context,
      final NightbornConfig updated,
      final Supplier<Text> feedback) {
    final ServerCommandSource source = context.getSource();
    if (!ServerConfigService.update(source.getServer(), updated)) {
      source.sendError(Text.translatable("nightborn.command.config.failed"));
      return 0;
    }
    source.sendFeedback(feedback, true);
    return 1;
  }
}
