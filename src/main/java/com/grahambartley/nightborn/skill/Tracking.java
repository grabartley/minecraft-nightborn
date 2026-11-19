package com.grahambartley.nightborn.skill;

import com.grahambartley.nightborn.raid.Trails;
import com.grahambartley.nightborn.seeker.HeldSeekGifts;
import com.grahambartley.nightborn.seeker.SeekGift;
import com.grahambartley.nightborn.seeker.SeekGifts;
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
import net.minecraft.util.math.Vec3d;
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
    final List<SeekGift> gifts = HeldSeekGifts.of(hunter);
    final TrackingState state =
        new TrackingState(
            hunter.getServerWorld().getTime() + SeekGifts.trackTicks(power.durationTicks(), gifts),
            SeekGifts.trackRange(power.range(), gifts),
            SeekGifts.bloodTrails(gifts));
    ACTIVE.put(hunter.getUuid(), state);
    show(hunter, state);
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
                show(player, entry.getValue());
              }
              return false;
            });
  }

  private static void show(final ServerPlayerEntity hunter, final TrackingState state) {
    final Optional<Vec3d> trail = Trails.lead(hunter);
    trail.ifPresent(lead -> Trails.drip(hunter));
    final Optional<LivingEntity> nearest =
        trail.isPresent() ? Optional.empty() : nearest(hunter, state.range());
    if (state.bloodTrails()) {
      nearest.ifPresent(vampire -> Trails.drip(hunter, vampire));
    }
    final Text message =
        trail
            .map(lead -> pointer(hunter, lead, "nightborn.tracking.trail"))
            .or(
                () ->
                    nearest.map(
                        vampire -> pointer(hunter, vampire.getPos(), "nightborn.tracking.found")))
            .orElseGet(
                () -> Text.translatable("nightborn.tracking.none").formatted(Formatting.GRAY));
    hunter.sendMessage(message, true);
  }

  private static Text pointer(final ServerPlayerEntity hunter, final Vec3d at, final String key) {
    return Text.translatable(
            key,
            TrackingDirection.arrow(at.x - hunter.getX(), at.z - hunter.getZ(), hunter.getYaw()))
        .formatted(Formatting.GOLD);
  }
}
