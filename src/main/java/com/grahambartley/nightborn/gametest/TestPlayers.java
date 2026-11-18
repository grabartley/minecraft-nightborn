package com.grahambartley.nightborn.gametest;

import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.TreeProgress;
import com.grahambartley.nightborn.vampire.VampireTransitions;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.ladysnake.cca.internal.entity.CardinalComponentsEntity;

final class TestPlayers {

  static final int JOIN_PROTECTION_TICKS = 60;
  static final int READY = JOIN_PROTECTION_TICKS + 2;

  static final String NAME_PREFIX = "nb-";

  private static final AtomicInteger NAMES = new AtomicInteger();

  static {
    ServerPlayConnectionEvents.INIT.register(
        (handler, server) -> {
          if (handler.player.getGameProfile().getName().startsWith(NAME_PREFIX)) {
            handler.onCustomPayload(
                new CustomPayloadC2SPacket(
                    new RegistrationPayload(
                        RegistrationPayload.REGISTER,
                        List.of(CardinalComponentsEntity.PACKET_ID.id()))));
          }
        });
  }

  private TestPlayers() {}

  static ServerPlayerEntity human(final TestContext context, final BlockPos relative) {
    final MinecraftServer server = context.getWorld().getServer();
    final ConnectedClientData data =
        ConnectedClientData.createDefault(
            new GameProfile(UUID.randomUUID(), NAME_PREFIX + NAMES.incrementAndGet()), false);
    final ServerPlayerEntity player =
        new ServerPlayerEntity(
            server, context.getWorld(), data.gameProfile(), data.syncedOptions());
    final ClientConnection connection = new ClientConnection(NetworkSide.SERVERBOUND);
    new EmbeddedChannel(connection);
    server.getPlayerManager().onPlayerConnect(connection, player, data);
    player.changeGameMode(GameMode.SURVIVAL);
    final Vec3d at = context.getAbsolute(Vec3d.ofBottomCenter(relative));
    player.refreshPositionAndAngles(at.x, at.y, at.z, 0.0F, 0.0F);
    context.runAtEveryTick(
        () -> {
          if (!player.isRemoved()) {
            player.playerTick();
          }
        });
    return player;
  }

  static ServerPlayerEntity vampire(final TestContext context, final BlockPos relative) {
    final ServerPlayerEntity player = human(context, relative);
    VampireTransitions.turn(player);
    return player;
  }

  static void remove(final TestContext context, final ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
  }

  static void disconnectEveryone(final MinecraftServer server) {
    List.copyOf(server.getPlayerManager().getPlayerList())
        .forEach(server.getPlayerManager()::remove);
  }

  static int pointsFor(final int experience) {
    final SkillSettings settings = ServerConfigService.current().skills();
    return TreeProgress.EMPTY
        .addExperience(experience, settings.firstPointExperience(), settings.pointCostRise())
        .earned();
  }

  static void finish(final TestContext context, final ServerPlayerEntity... players) {
    remove(context, players);
    context.complete();
  }
}
