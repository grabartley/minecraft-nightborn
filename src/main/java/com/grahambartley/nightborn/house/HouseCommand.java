package com.grahambartley.nightborn.house;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.GlobalPos;

public final class HouseCommand {

  static final String ROOT = "house";
  static final String ACCEPT = "accept";
  static final String DECLINE = "decline";
  static final String LEAVE = "leave";
  static final String NAME = "name";
  static final String DISBAND = "disband";
  static final String NAMED = "nightborn.house.info.named";
  static final String CHALLENGED = "nightborn.house.info.challenged";
  static final String CHALLENGED_DISBAND = "nightborn.house.disband.challenged";
  static final String NONE = "nightborn.house.info.none";
  static final String LED = "nightborn.house.info.led";
  static final String MEMBER = "nightborn.house.info.member";
  static final String SEATED = "nightborn.house.info.seated";
  static final String ANCHORED = "nightborn.house.info.anchored";
  static final String STORE = "nightborn.house.info.store";

  private HouseCommand() {}

  static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> register(dispatcher));
  }

  static void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
    dispatcher.register(
        CommandManager.literal(ROOT)
            .executes(HouseCommand::info)
            .then(
                CommandManager.literal(ACCEPT)
                    .executes(context -> ok(HouseInvites.answer(player(context), true))))
            .then(
                CommandManager.literal(DECLINE)
                    .executes(context -> ok(HouseInvites.answer(player(context), false))))
            .then(
                CommandManager.literal(LEAVE)
                    .executes(context -> ok(HouseInvites.leave(player(context)))))
            .then(
                CommandManager.literal(NAME)
                    .executes(context -> ok(HouseNaming.offer(player(context)))))
            .then(CommandManager.literal(DISBAND).executes(HouseCommand::disband)));
  }

  private static int info(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerPlayerEntity player = player(context);
    final MinecraftServer server = player.getServer();
    final Optional<House> house = Houses.get(server).of(player.getUuid());
    if (house.isEmpty()) {
      player.sendMessage(Text.translatable(NONE), false);
      return 0;
    }
    final House found = house.get();
    final boolean leads = found.lord().equals(player.getUuid());
    player.sendMessage(
        leads
            ? Text.translatable(LED, found.size(), Lordship.capacity(player))
            : Text.translatable(MEMBER, name(server, found.lord())),
        false);
    found
        .identity()
        .name()
        .ifPresent(name -> player.sendMessage(Text.translatable(NAMED, name), false));
    found
        .challenges()
        .pending()
        .ifPresent(
            challenge ->
                player.sendMessage(
                    Text.translatable(CHALLENGED, name(server, challenge.challenger())), false));
    final Optional<GlobalPos> place = HousePlace.of(server, found);
    place.ifPresent(
        at ->
            player.sendMessage(
                Text.translatable(
                    found.seat().isPresent() ? SEATED : ANCHORED,
                    at.pos().getX(),
                    at.pos().getY(),
                    at.pos().getZ()),
                false));
    found
        .store()
        .ifPresent(
            at ->
                player.sendMessage(
                    Text.translatable(STORE, at.pos().getX(), at.pos().getY(), at.pos().getZ()),
                    false));
    return 1;
  }

  private static int disband(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerPlayerEntity lord = player(context);
    final Optional<House> led = Houses.get(lord.getServer()).ledBy(lord.getUuid());
    if (led.isEmpty()) {
      lord.sendMessage(Text.translatable(HouseNaming.NOT_LORD), true);
      return 0;
    }
    if (!HouseSuccession.disband(lord.getServer(), lord.getUuid(), false)) {
      lord.sendMessage(Text.translatable(CHALLENGED_DISBAND), true);
      return 0;
    }
    return 1;
  }

  static Text name(final MinecraftServer server, final UUID player) {
    final ServerPlayerEntity online = server.getPlayerManager().getPlayer(player);
    if (online != null) {
      return online.getDisplayName();
    }
    return Text.literal(
        Optional.ofNullable(server.getUserCache())
            .flatMap(cache -> cache.getByUuid(player))
            .map(profile -> profile.getName())
            .orElse("?"));
  }

  private static ServerPlayerEntity player(final CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return context.getSource().getPlayerOrThrow();
  }

  private static int ok(final boolean done) {
    return done ? 1 : 0;
  }
}
