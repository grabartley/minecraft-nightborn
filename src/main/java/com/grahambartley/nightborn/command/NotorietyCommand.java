package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.age.AgeNight;
import com.grahambartley.nightborn.hunt.HuntAttachment;
import com.grahambartley.nightborn.hunt.HuntRecord;
import com.grahambartley.nightborn.hunt.HuntRules;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class NotorietyCommand {

  private static final String NOTORIETY = "notoriety";

  private NotorietyCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal(NOTORIETY)
        .executes(NotorietyCommand::show)
        .then(
            CommandManager.argument(NOTORIETY, DoubleArgumentType.doubleArg(0.0))
                .executes(NotorietyCommand::set));
  }

  static String shown(final double notoriety) {
    return BigDecimal.valueOf(notoriety)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString();
  }

  private static int show(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final HuntRecord record = HuntAttachment.get(player);
      final HuntRules.Level level =
          HuntRules.level(record.notoriety(), ServerConfigService.current().hunt());
      final long night = AgeNight.index(Hunts.time(player));
      context
          .getSource()
          .sendFeedback(
              () ->
                  Vampires.isVampire(player)
                      ? Text.translatable(
                          "nightborn.command.player.notoriety",
                          player.getName(),
                          shown(record.notoriety()),
                          Text.translatable(
                              "nightborn.hunt.level." + level.name().toLowerCase(Locale.ROOT)),
                          Text.translatable("nightborn.hunt.phase." + record.phase().asString()),
                          record.quietNightsLeft(night))
                      : Text.translatable(
                          "nightborn.command.player.notoriety.human", player.getName()),
              false);
    }
    return 1;
  }

  private static int set(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final double notoriety = DoubleArgumentType.getDouble(context, NOTORIETY);
    return PlayerCommand.forEach(
        context,
        NOTORIETY,
        shown(notoriety),
        player -> Vampires.isVampire(player) && Hunts.set(player, notoriety));
  }
}
