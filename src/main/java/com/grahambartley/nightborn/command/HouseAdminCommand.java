package com.grahambartley.nightborn.command;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.house.HouseSuccession;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

final class HouseAdminCommand {

  static final String LORDS = "lords";

  private HouseAdminCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("house")
        .then(
            CommandManager.literal("disband")
                .then(
                    CommandManager.argument(LORDS, GameProfileArgumentType.gameProfile())
                        .executes(HouseAdminCommand::disband)));
  }

  private static int disband(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    int disbanded = 0;
    for (final GameProfile lord : GameProfileArgumentType.getProfileArgument(context, LORDS)) {
      if (HouseSuccession.disband(source.getServer(), lord.getId())) {
        disbanded++;
        Nightborn.LOGGER.info(
            "{} used /nightborn house disband on {}", source.getName(), lord.getName());
      }
    }
    final int total = disbanded;
    if (total == 0) {
      source.sendError(Text.translatable("nightborn.command.house.disband.none"));
    } else {
      source.sendFeedback(
          () -> Text.translatable("nightborn.command.house.disband.done", total), true);
    }
    return total;
  }
}
