package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class Tracking {

  static final int UPDATE_TICKS = 5;

  private static final Map<UUID, TrackingState> ACTIVE = new HashMap<>();

  private Tracking() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Tracking::tick);
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE.clear());
  }

  public static void start(final ServerPlayerEntity hunter, final TrackPower power) {
    ACTIVE.put(
        hunter.getUuid(),
        new TrackingState(
            hunter.getServerWorld().getTime() + power.durationTicks(), power.range()));
    show(hunter, power.range());
  }

  @Nullable
  public static TrackingState state(final ServerPlayerEntity hunter) {
    return ACTIVE.get(hunter.getUuid());
  }

  public static Optional<LivingEntity> nearest(final ServerPlayerEntity hunter, final int range) {
    final List<LivingEntity> vampires =
        hunter
            .getServerWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                new Box(hunter.getBlockPos()).expand(range),
                entity ->
                    entity != hunter
                        && entity.isAlive()
                        && !entity.isSpectator()
                        && Vampires.isVampire(entity));
    return TrackingSearch.nearest(hunter.getPos(), vampires, LivingEntity::getPos, range);
  }

  private static void tick(final MinecraftServer server) {
    if (ACTIVE.isEmpty()) {
      return;
    }
    ACTIVE
        .entrySet()
        .removeIf(
            entry -> {
              final ServerPlayerEntity player = server.getPlayerManager().getPlayer(entry.getKey());
              if (player == null
                  || !player.isAlive()
                  || Vampires.isVampire(player)
                  || entry.getValue().over(player.getServerWorld().getTime())) {
                return true;
              }
              if (player.age % UPDATE_TICKS == 0) {
                show(player, entry.getValue().range());
              }
              return false;
            });
  }

  private static void show(final ServerPlayerEntity hunter, final int range) {
    final Text message =
        nearest(hunter, range)
            .<Text>map(
                vampire ->
                    Text.translatable(
                            "nightborn.tracking.found",
                            TrackingDirection.arrow(
                                vampire.getX() - hunter.getX(),
                                vampire.getZ() - hunter.getZ(),
                                hunter.getYaw()))
                        .formatted(Formatting.DARK_RED))
            .orElseGet(
                () -> Text.translatable("nightborn.tracking.none").formatted(Formatting.GRAY));
    hunter.sendMessage(message, true);
  }
}
