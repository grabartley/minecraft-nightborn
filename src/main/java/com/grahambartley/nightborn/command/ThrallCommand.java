package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.thrall.ThrallEntity;
import com.grahambartley.nightborn.thrall.Thralls;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class ThrallCommand {

  static final String THRALLS = "thralls";

  private ThrallCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("thrall")
        .then(
            CommandManager.literal("release")
                .then(
                    CommandManager.argument(THRALLS, EntityArgumentType.entities())
                        .executes(ThrallCommand::release)));
  }

  private static int release(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    int released = 0;
    for (final Entity entity : EntityArgumentType.getEntities(context, THRALLS)) {
      if (entity instanceof ThrallEntity thrall && Thralls.free(thrall)) {
        released++;
        Nightborn.LOGGER.info(
            "{} used /nightborn thrall release on {}", source.getName(), thrall.getUuidAsString());
      }
    }
    final int count = released;
    if (count == 0) {
      source.sendError(Text.translatable("nightborn.command.thrall.release.none"));
    } else {
      source.sendFeedback(
          () -> Text.translatable("nightborn.command.thrall.release.done", count), true);
    }
    return count;
  }
}
