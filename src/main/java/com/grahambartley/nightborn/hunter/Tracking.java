package com.grahambartley.nightborn.hunter;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.skill.TrackPower;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

public final class Tracking {

  public static final AttachmentType<TrackingState> TYPE =
      AttachmentRegistry.create(Identifier.of(Nightborn.MOD_ID, "tracking"));

  static final int UPDATE_TICKS = 5;

  private Tracking() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(Tracking::tick);
  }

  public static void start(final ServerPlayerEntity hunter, final TrackPower power) {
    hunter.setAttached(
        TYPE,
        new TrackingState(
            hunter.getServerWorld().getTime() + power.durationTicks(), power.range()));
    show(hunter, power.range());
  }

  @Nullable
  public static TrackingState state(final ServerPlayerEntity hunter) {
    return hunter.getAttached(TYPE);
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
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final TrackingState state = state(player);
      if (state == null) {
        continue;
      }
      if (state.over(player.getServerWorld().getTime()) || !player.isAlive()) {
        player.removeAttached(TYPE);
      } else if (player.age % UPDATE_TICKS == 0) {
        show(player, state.range());
      }
    }
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
