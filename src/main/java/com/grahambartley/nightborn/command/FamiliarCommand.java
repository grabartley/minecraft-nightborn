package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.familiar.FamiliarRelease;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class FamiliarCommand {

  static final String TARGETS = "targets";

  private FamiliarCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("familiar")
        .then(
            CommandManager.argument(TARGETS, EntityArgumentType.entities())
                .then(CommandManager.literal("release").executes(FamiliarCommand::release)));
  }

  private static int release(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    int released = 0;
    for (final Entity entity : EntityArgumentType.getEntities(context, TARGETS)) {
      if (entity instanceof HumanFamiliarEntity familiar
          && FamiliarRelease.release(familiar) != null) {
        released++;
        Nightborn.LOGGER.info(
            "{} used /nightborn familiar {} release", source.getName(), entity.getUuidAsString());
        source.sendFeedback(
            () -> Text.translatable("nightborn.command.familiar.release.done", entity.getName()),
            true);
      } else {
        source.sendError(
            Text.translatable("nightborn.command.familiar.release.unchanged", entity.getName()));
      }
    }
    return released;
  }
}
