package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.config.TurningSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.guild.GuildOath;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.SicknessStage;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class PlayerCommand {

  static final String TARGETS = "targets";

  private PlayerCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("player")
        .then(
            CommandManager.argument(TARGETS, EntityArgumentType.players())
                .then(CommandManager.literal("turn").executes(PlayerCommand::turn))
                .then(CommandManager.literal("cure").executes(PlayerCommand::cure))
                .then(CommandManager.literal("swear").executes(PlayerCommand::swear))
                .then(CommandManager.literal("info").executes(PlayerCommand::info))
                .then(StageCommand.node())
                .then(NotorietyCommand.node())
                .then(SkillsCommand.node()));
  }

  private static int turn(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return forEach(context, "turn", VampireTransitions::turnInCeremony);
  }

  private static int swear(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return forEach(context, "swear", player -> GuildOath.swear(player, Optional.empty()));
  }

  private static int cure(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return forEach(
        context,
        "cure",
        player -> {
          if (Vampires.isVampire(player)) {
            VampireTransitions.makeHuman(player);
            return true;
          }
          return BiteSickness.cure(player);
        });
  }

  private static int info(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final TurningSettings turning = ServerConfigService.current().turning();
    for (final ServerPlayerEntity player : EntityArgumentType.getPlayers(context, TARGETS)) {
      final DownedState downed = Downed.state(player);
      final Text sickness =
          BiteSicknessAttachment.isSick(player)
              ? Text.translatable(
                  "nightborn.command.player.info.sick",
                  SicknessStage.of(BiteSicknessAttachment.ticksRun(player), turning.sicknessDays())
                      .daysLeft())
              : Text.translatable("nightborn.command.player.info.well");
      final Text downedText =
          downed == null
              ? Text.translatable("nightborn.command.player.info.standing")
              : Text.translatable(
                  "nightborn.command.player.info." + downed.phase().asString(),
                  downed.secondsLeft());
      source.sendFeedback(
          () ->
              Text.translatable(
                  "nightborn.command.player.info",
                  player.getName(),
                  Text.translatable(
                      "nightborn.faction." + FactionAttachment.get(player).asString()),
                  BloodAttachment.get(player),
                  BloodCapacity.of(player),
                  sickness,
                  downedText),
          false);
    }
    return 1;
  }

  static int forEach(
      final CommandContext<ServerCommandSource> context,
      final String action,
      final Predicate<ServerPlayerEntity> change)
      throws CommandSyntaxException {
    return forEach(context, action, "", change);
  }

  static int forEach(
      final CommandContext<ServerCommandSource> context,
      final String action,
      final String detail,
      final Predicate<ServerPlayerEntity> change)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, TARGETS);
    int changed = 0;
    for (final ServerPlayerEntity player : players) {
      if (change.test(player)) {
        changed++;
        Nightborn.LOGGER.info(
            "{} used /nightborn player {} {} {}",
            source.getName(),
            player.getName().getString(),
            action,
            detail);
        source.sendFeedback(
            () ->
                Text.translatable("nightborn.command.player." + action + ".done", player.getName()),
            true);
      } else {
        source.sendError(
            Text.translatable(
                "nightborn.command.player." + action + ".unchanged", player.getName()));
      }
    }
    return changed;
  }
}
