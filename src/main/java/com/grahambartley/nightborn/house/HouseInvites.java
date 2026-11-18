package com.grahambartley.nightborn.house;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class HouseInvites {

  static final String SENT = "nightborn.house.invite.sent";
  static final String RECEIVED = "nightborn.house.invite.received";
  static final String ACCEPT = "nightborn.house.invite.accept";
  static final String DECLINE = "nightborn.house.invite.decline";
  static final String NONE = "nightborn.house.invite.none";
  static final String GONE = "nightborn.house.invite.gone";
  static final String WELCOMED = "nightborn.house.invite.welcomed";
  static final String ARRIVED = "nightborn.house.invite.arrived";
  static final String DECLINED = "nightborn.house.invite.declined";
  static final String REFUSED = "nightborn.house.invite.refused";
  static final String LEFT = "nightborn.house.leave.left";
  static final String MEMBER_LEFT = "nightborn.house.leave.member_left";
  static final String LORD_STAYS = "nightborn.house.leave.lord";
  static final String NOT_MEMBER = "nightborn.house.leave.not_member";

  private static final Map<UUID, UUID> PENDING = new ConcurrentHashMap<>();

  private HouseInvites() {}

  static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> PENDING.remove(handler.getPlayer().getUuid()));
  }

  public static void invite(final ServerPlayerEntity lord, final ServerPlayerEntity guest) {
    final HouseRules.Invite verdict = Lordship.judge(lord, guest);
    if (verdict != HouseRules.Invite.WELCOME) {
      lord.sendMessage(Text.translatable(verdict.key(), guest.getDisplayName()), true);
      return;
    }
    PENDING.put(guest.getUuid(), lord.getUuid());
    lord.sendMessage(Text.translatable(SENT, guest.getDisplayName()), true);
    guest.sendMessage(
        Text.translatable(
            RECEIVED,
            lord.getDisplayName(),
            answer(ACCEPT, HouseCommand.ACCEPT, Formatting.DARK_GREEN),
            answer(DECLINE, HouseCommand.DECLINE, Formatting.DARK_RED)),
        false);
    guest.playSoundToPlayer(SoundEvents.BLOCK_BELL_RESONATE, SoundCategory.PLAYERS, 0.6F, 0.6F);
  }

  public static boolean answer(final ServerPlayerEntity guest, final boolean accepts) {
    final UUID lordId = PENDING.remove(guest.getUuid());
    if (lordId == null) {
      guest.sendMessage(Text.translatable(NONE), true);
      return false;
    }
    final MinecraftServer server = guest.getServer();
    final ServerPlayerEntity lord = server.getPlayerManager().getPlayer(lordId);
    if (lord == null) {
      guest.sendMessage(Text.translatable(GONE), true);
      return false;
    }
    if (!accepts) {
      lord.sendMessage(Text.translatable(DECLINED, guest.getDisplayName()), true);
      guest.sendMessage(Text.translatable(REFUSED, lord.getDisplayName()), true);
      return true;
    }
    final HouseRules.Invite verdict = Lordship.judge(lord, guest);
    if (verdict != HouseRules.Invite.WELCOME) {
      guest.sendMessage(Text.translatable(GONE), true);
      lord.sendMessage(Text.translatable(verdict.key(), guest.getDisplayName()), true);
      return false;
    }
    final Houses houses = Houses.get(server);
    leaveAny(houses, guest.getUuid());
    houses.ledBy(lordId).ifPresent(house -> houses.put(house.withPlayer(guest.getUuid())));
    guest.sendMessage(Text.translatable(WELCOMED, lord.getDisplayName()), false);
    lord.sendMessage(Text.translatable(ARRIVED, guest.getDisplayName()), false);
    guest.playSoundToPlayer(
        SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.PLAYERS, 0.8F, 0.7F);
    return true;
  }

  public static boolean leave(final ServerPlayerEntity member) {
    final Houses houses = Houses.get(member.getServer());
    final Optional<House> house = houses.of(member.getUuid());
    if (house.isEmpty()) {
      member.sendMessage(Text.translatable(NOT_MEMBER), true);
      return false;
    }
    if (house.get().lord().equals(member.getUuid())) {
      member.sendMessage(Text.translatable(LORD_STAYS), true);
      return false;
    }
    houses.put(house.get().without(member.getUuid()));
    member.sendMessage(Text.translatable(LEFT), false);
    final ServerPlayerEntity lord =
        member.getServer().getPlayerManager().getPlayer(house.get().lord());
    if (lord != null) {
      lord.sendMessage(Text.translatable(MEMBER_LEFT, member.getDisplayName()), false);
    }
    return true;
  }

  private static void leaveAny(final Houses houses, final UUID player) {
    houses
        .of(player)
        .filter(house -> !house.lord().equals(player))
        .ifPresent(house -> houses.put(house.without(player)));
  }

  private static Text answer(final String key, final String command, final Formatting colour) {
    return Text.translatable(key)
        .setStyle(
            Style.EMPTY
                .withColor(colour)
                .withUnderline(true)
                .withClickEvent(
                    new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND, "/" + HouseCommand.ROOT + " " + command)));
  }
}
