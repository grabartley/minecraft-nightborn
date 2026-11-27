package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.dismember.BodyPart;
import com.grahambartley.nightborn.dismember.Dismemberment;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class BodyCommand {

  static final String TARGETS = "targets";
  static final String PART = "part";

  private BodyCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("body")
        .then(
            CommandManager.literal("sever")
                .then(
                    CommandManager.argument(TARGETS, EntityArgumentType.entities())
                        .then(
                            CommandManager.argument(PART, StringArgumentType.word())
                                .suggests(
                                    (context, builder) ->
                                        CommandSource.suggestMatching(
                                            BodyPart.limbs().stream().map(BodyPart::asString),
                                            builder))
                                .executes(BodyCommand::sever))))
        .then(
            CommandManager.literal("restore")
                .then(
                    CommandManager.argument(TARGETS, EntityArgumentType.entities())
                        .executes(BodyCommand::restore)));
  }

  private static int sever(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final String name = StringArgumentType.getString(context, PART);
    final Optional<BodyPart> part = BodyPart.named(name).filter(BodyPart::limb);
    if (part.isEmpty()) {
      source.sendError(Text.translatable("nightborn.command.body.unknown_part", name));
      return 0;
    }
    int count = 0;
    for (final Entity entity : EntityArgumentType.getEntities(context, TARGETS)) {
      if (entity instanceof LivingEntity living && !Dismemberment.missing(living, part.get())) {
        Dismemberment.sever(living, part.get());
        count++;
        Nightborn.LOGGER.info(
            "{} used /nightborn body sever {} on {}",
            source.getName(),
            name,
            living.getUuidAsString());
      }
    }
    return report(source, count, "nightborn.command.body.severed");
  }

  private static int restore(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    int count = 0;
    for (final Entity entity : EntityArgumentType.getEntities(context, TARGETS)) {
      if (entity instanceof LivingEntity living && !Dismemberment.state(living).whole()) {
        Dismemberment.restore(living);
        count++;
        Nightborn.LOGGER.info(
            "{} used /nightborn body restore on {}", source.getName(), living.getUuidAsString());
      }
    }
    return report(source, count, "nightborn.command.body.restored");
  }

  private static int report(final ServerCommandSource source, final int count, final String key) {
    if (count == 0) {
      source.sendError(Text.translatable("nightborn.command.body.none"));
    } else {
      source.sendFeedback(() -> Text.translatable(key, count), true);
    }
    return count;
  }
}
