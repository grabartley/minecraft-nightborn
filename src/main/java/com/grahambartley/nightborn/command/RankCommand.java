package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.Oath;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.rank.RankLadder;
import com.grahambartley.nightborn.rank.Ranking;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
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

final class RankCommand {

  private static final String RANK = "rank";

  private static final DynamicCommandExceptionType UNKNOWN =
      new DynamicCommandExceptionType(
          id -> Text.translatable("nightborn.command.player.rank.unknown", id));

  private RankCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal(RANK)
        .executes(RankCommand::show)
        .then(
            CommandManager.argument(RANK, IdentifierArgumentType.identifier())
                .suggests(
                    (context, builder) ->
                        CommandSource.suggestIdentifiers(
                            ladder(context).rungs().stream().map(RankLadder.Rung::id), builder))
                .executes(RankCommand::set));
  }

  private static int show(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final RankLadder ladder = ladder(context);
    for (final ServerPlayerEntity player :
        EntityArgumentType.getPlayers(context, PlayerCommand.TARGETS)) {
      final Oath oath = OathAttachment.get(player);
      context
          .getSource()
          .sendFeedback(
              () ->
                  GuildMembership.isHunter(player)
                      ? Text.translatable(
                          "nightborn.command.player.rank",
                          player.getName(),
                          ladder
                              .rung(oath.rank())
                              .map(rung -> rung.rank().name())
                              .orElse(Text.literal(Integer.toString(oath.rank()))),
                          SkillAttachment.get(player, SkillTree.HUNTER).earned(),
                          deeds(ladder, oath))
                      : Text.translatable("nightborn.command.player.rank.none", player.getName()),
              false);
    }
    return 1;
  }

  static String deeds(final RankLadder ladder, final Oath oath) {
    int done = 0;
    int all = 0;
    for (int rank = 0; rank < ladder.size(); rank++) {
      if (ladder.deed(rank).isPresent()) {
        all++;
        done += oath.deedDone(rank) ? 1 : 0;
      }
    }
    return done + "/" + all;
  }

  private static int set(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final Identifier id = IdentifierArgumentType.getIdentifier(context, RANK);
    final int rank = ladder(context).rankOf(id).orElseThrow(() -> UNKNOWN.create(id));
    return PlayerCommand.forEach(
        context, RANK, id.toString(), player -> Ranking.setRank(player, rank));
  }

  private static RankLadder ladder(final CommandContext<ServerCommandSource> context) {
    return RankLadder.of(context.getSource().getRegistryManager());
  }
}
