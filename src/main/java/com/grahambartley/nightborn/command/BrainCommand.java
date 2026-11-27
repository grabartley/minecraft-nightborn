package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.mob.combat.MobFighter;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class BrainCommand {

  static final String TARGETS = "targets";

  private BrainCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("brain")
        .then(
            CommandManager.argument(TARGETS, EntityArgumentType.entities())
                .executes(BrainCommand::show));
  }

  private static int show(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    int shown = 0;
    for (final Entity entity : EntityArgumentType.getEntities(context, TARGETS)) {
      if (!(entity instanceof MobFighter fighter)) {
        source.sendError(Text.translatable("nightborn.command.brain.none", entity.getName()));
        continue;
      }
      final var think = fighter.mobFighting().lastThink();
      if (think.isEmpty()) {
        source.sendFeedback(
            () -> Text.translatable("nightborn.command.brain.unthought", entity.getName()), false);
        continue;
      }
      shown++;
      BrainText.lines(entity.getName(), think.get(), entity.getWorld().getTime())
          .forEach(line -> source.sendFeedback(() -> line, false));
    }
    return shown;
  }
}
