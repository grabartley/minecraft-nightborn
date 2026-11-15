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
import net.minecraft.text.MutableText;
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
                      bloodlines(progress)),
              false);
    }
    return 1;
  }

  static Text bloodlines(final TreeProgress progress) {
    if (progress.bloodlines().isEmpty()) {
      return Text.translatable("nightborn.command.player.skills.none");
    }
    final MutableText names = Text.empty();
    for (int i = 0; i < progress.bloodlines().size(); i++) {
      if (i > 0) {
        names.append(", ");
      }
      names.append(Text.translatable(SkillNames.branch(progress.bloodlines().get(i))));
    }
    return names;
  }

  private static int setPoints(
      final CommandContext<ServerCommandSource> context, final SkillTree tree)
      throws CommandSyntaxException {
    final int points = IntegerArgumentType.getInteger(context, POINTS);
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final int spent = SkillAttachment.get(player, tree).spent();
      if (points < spent) {
        context
            .getSource()
            .sendError(
                Text.translatable(
                    "nightborn.command.player.points.below_spent", player.getName(), spent));
        return 0;
      }
    }
    return PlayerCommand.forEach(
        context,
        "points",
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
