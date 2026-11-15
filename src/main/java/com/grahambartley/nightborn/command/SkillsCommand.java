package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class SkillsCommand {

  private static final String POINTS = "points";

  private SkillsCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    final LiteralArgumentBuilder<ServerCommandSource> skills = CommandManager.literal("skills");
    for (final SkillTree tree : SkillTree.values()) {
      skills.then(
          CommandManager.literal(tree.asString())
              .executes(context -> show(context, tree))
              .then(
                  CommandManager.literal(POINTS)
                      .then(
                          CommandManager.argument(
                                  POINTS, IntegerArgumentType.integer(0, TreeProgress.MAX_POINTS))
                              .executes(context -> setPoints(context, tree))))
              .then(CommandManager.literal("reset").executes(context -> reset(context, tree))));
    }
    return skills;
  }

  private static int show(final CommandContext<ServerCommandSource> context, final SkillTree tree)
      throws CommandSyntaxException {
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final TreeProgress progress = SkillAttachment.get(player, tree);
      context
          .getSource()
          .sendFeedback(
              () ->
                  Text.translatable(
                      "nightborn.command.player.skills",
                      player.getName(),
                      Text.translatable(tree.translationKey()),
                      progress.earned(),
                      progress.spent(),
                      progress.free(),
                      progress.ownedInOrder().size(),
                      SkillNames.bloodlines(progress.bloodlines())),
              false);
    }
    return 1;
  }

  private static int setPoints(
      final CommandContext<ServerCommandSource> context, final SkillTree tree)
      throws CommandSyntaxException {
    final int points = IntegerArgumentType.getInteger(context, POINTS);
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final TreeProgress progress = SkillAttachment.get(player, tree);
      if (!progress.canSetEarned(points)) {
        context
            .getSource()
            .sendError(
                Text.translatable(
                    "nightborn.command.player.points.below_spent",
                    player.getName(),
                    progress.spent()));
        return 0;
      }
    }
    return PlayerCommand.forEach(
        context,
        "points",
        tree.asString() + " " + points,
        player -> {
          final TreeProgress progress = SkillAttachment.get(player, tree);
          if (progress.earned() == points) {
            return false;
          }
          SkillAttachment.set(player, tree, progress.withEarned(points));
          return true;
        });
  }

  private static int reset(final CommandContext<ServerCommandSource> context, final SkillTree tree)
      throws CommandSyntaxException {
    return PlayerCommand.forEach(
        context,
        "reset_skills",
        tree.asString(),
        player -> {
          final TreeProgress progress = SkillAttachment.get(player, tree);
          if (progress.bought().isEmpty()) {
            return false;
          }
          SkillAttachment.set(player, tree, progress.refundAll());
          SkillAttributes.apply(player);
          return true;
        });
  }
}
