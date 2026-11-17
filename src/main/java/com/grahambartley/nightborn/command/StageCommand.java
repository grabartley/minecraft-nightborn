package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.age.AgeAttachment;
import com.grahambartley.nightborn.age.AgeLadder;
import com.grahambartley.nightborn.age.AgeProgress;
import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.vampire.Vampires;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

final class StageCommand {

  private static final String STAGE = "stage";

  private static final DynamicCommandExceptionType UNKNOWN =
      new DynamicCommandExceptionType(
          id -> Text.translatable("nightborn.command.player.stage.unknown", id));

  private StageCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal(STAGE)
        .executes(StageCommand::show)
        .then(
            CommandManager.argument(STAGE, IdentifierArgumentType.identifier())
                .suggests(
                    (context, builder) ->
                        CommandSource.suggestIdentifiers(
                            ladder(context).rungs().stream().map(AgeLadder.Rung::id), builder))
                .executes(StageCommand::set));
  }

  private static int show(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final AgeLadder ladder = ladder(context);
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final AgeProgress progress = AgeAttachment.get(player);
      context
          .getSource()
          .sendFeedback(
              () ->
                  Vampires.isVampire(player)
                      ? Text.translatable(
                          "nightborn.command.player.stage",
                          player.getName(),
                          ladder
                              .rung(progress.rank())
                              .map(rung -> rung.age().name())
                              .orElse(Text.literal(Integer.toString(progress.rank()))),
                          progress.nights(),
                          progress.blood(),
                          deeds(ladder, progress))
                      : Text.translatable("nightborn.command.player.stage.human", player.getName()),
              false);
    }
    return 1;
  }

  static String deeds(final AgeLadder ladder, final AgeProgress progress) {
    int done = 0;
    int all = 0;
    for (int rank = 0; rank < ladder.size(); rank++) {
      if (ladder.rungs().get(rank).age().deed().isPresent()) {
        all++;
        done += progress.deedDone(rank) ? 1 : 0;
      }
    }
    return done + "/" + all;
  }

  private static int set(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final Identifier id = IdentifierArgumentType.getIdentifier(context, STAGE);
    final int rank = ladder(context).rankOf(id).orElseThrow(() -> UNKNOWN.create(id));
    return PlayerCommand.forEach(
        context, STAGE, id.toString(), player -> Ageing.setRank(player, rank));
  }

  private static AgeLadder ladder(final CommandContext<ServerCommandSource> context) {
    return AgeLadder.of(context.getSource().getRegistryManager());
  }
}
