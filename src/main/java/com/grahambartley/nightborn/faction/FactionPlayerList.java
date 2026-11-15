package com.grahambartley.nightborn.faction;

import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class FactionPlayerList {

  private FactionPlayerList() {}

  public static void register() {
    ServerConfigService.onChange((server, config) -> refreshAll(server));
  }

  @Nullable
  public static Text listName(final ServerPlayerEntity player, final boolean shown) {
    if (!shown) {
      return null;
    }
    return Text.empty()
        .append(Team.decorateName(player.getScoreboardTeam(), player.getName()))
        .append(" ")
        .append(
            Text.translatable(
                    "nightborn.faction." + FactionAttachment.get(player).asString() + ".tag")
                .formatted(Formatting.DARK_GRAY));
  }

  public static void refresh(final ServerPlayerEntity player) {
    send(player.getServer(), List.of(player));
  }

  public static void refreshAll(final MinecraftServer server) {
    send(server, server.getPlayerManager().getPlayerList());
  }

  private static void send(
      @Nullable final MinecraftServer server, final List<ServerPlayerEntity> players) {
    if (server == null || players.isEmpty()) {
      return;
    }
    server
        .getPlayerManager()
        .sendToAll(
            new PlayerListS2CPacket(
                EnumSet.of(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME), players));
  }

  public static boolean shown() {
    return ServerConfigService.current().factions().showInPlayerList();
  }
}
