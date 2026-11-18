package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.network.OpenWheelPayload;
import com.grahambartley.nightborn.servant.OrderCue;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.structure.NearestStructures;
import com.grahambartley.nightborn.structure.NearestStructures.Found;
import com.grahambartley.nightborn.wheel.CommandWheel;
import com.grahambartley.nightborn.wheel.WheelAction;
import com.grahambartley.nightborn.wheel.WheelEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.Structure;

public final class ScoutOrders<T extends PathAwareEntity & DayWorker> implements WheelAction<T> {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "scout");
  public static final TagKey<Structure> SCOUT_TARGETS =
      TagKey.of(RegistryKeys.STRUCTURE, Identifier.of(Nightborn.MOD_ID, "scout_targets"));
  public static final Identifier ICON = DayJob.SCOUT.icon();
  public static final Identifier PLACE_ICON = Identifier.ofVanilla("filled_map");

  private static final Map<UUID, Offer> OFFERED = new ConcurrentHashMap<>();

  record Offer(UUID worker, List<Found> places) {}

  private final SkillTree theme;

  public ScoutOrders(final SkillTree theme) {
    this.theme = theme;
  }

  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> OFFERED.remove(handler.getPlayer().getUuid()));
  }

  @Override
  public Identifier id() {
    return ID;
  }

  @Override
  public boolean allows(final ServerPlayerEntity player, final T worker) {
    return worker.workingFor(player);
  }

  @Override
  public List<WheelEntry> entries(final ServerPlayerEntity player, final T worker) {
    return List.of(
        WheelEntry.of(
            ID,
            ICON,
            Text.translatable(DayJob.SCOUT.translationKey()),
            worker.doing(DayJob.SCOUT)));
  }

  @Override
  public void perform(final ServerPlayerEntity player, final T worker, final WheelEntry chosen) {
    if (!(worker.getWorld() instanceof ServerWorld world)) {
      return;
    }
    if (chosen.argument() == WheelEntry.NO_ARGUMENT) {
      offer(player, worker, world);
      return;
    }
    final Offer offer = OFFERED.remove(player.getUuid());
    if (offer == null
        || !offer.worker().equals(worker.getUuid())
        || chosen.argument() >= offer.places().size()) {
      return;
    }
    final Found place = offer.places().get(chosen.argument());
    worker.assign(
        JobOrder.scout(worker.getBlockPos(), ScoutTrip.to(place.pos(), place.structure())));
    OrderCue.obey(
        worker,
        player,
        Text.translatable(
            DayJob.SCOUT.orderedKey(),
            worker.getDisplayName(),
            ScoutTrip.placeName(place.structure())));
  }

  private void offer(final ServerPlayerEntity player, final T worker, final ServerWorld world) {
    final BlockPos from = worker.getBlockPos();
    final int range = ServerConfigService.current().servants().scoutRange();
    NearestStructures.eachOf(world, SCOUT_TARGETS, from, range)
        .thenAcceptAsync(
            found -> {
              if (stillAsking(
                  player.isDisconnected(),
                  worker.isRemoved(),
                  player.currentScreenHandler != player.playerScreenHandler,
                  player.canInteractWithEntity(worker, CommandWheel.REACH_SLACK))) {
                show(
                    player,
                    worker,
                    from,
                    ScoutChoices.nearest(from, found, range, OpenWheelPayload.MOST_ENTRIES));
              }
            },
            world.getServer());
  }

  private void show(
      final ServerPlayerEntity player,
      final T worker,
      final BlockPos from,
      final List<Found> places) {
    if (places.isEmpty()) {
      player.sendMessage(
          Text.translatable("nightborn.scout.nowhere", worker.getDisplayName()), true);
      return;
    }
    OFFERED.put(player.getUuid(), new Offer(worker.getUuid(), places));
    final List<WheelEntry> entries = new ArrayList<>();
    for (int index = 0; index < places.size(); index++) {
      entries.add(new WheelEntry(ID, index, PLACE_ICON, label(from, places.get(index)), false));
    }
    CommandWheel.show(
        player,
        worker,
        theme,
        Text.translatable("nightborn.scout.where", worker.getDisplayName()),
        entries);
  }

  static boolean stillAsking(
      final boolean disconnected,
      final boolean workerGone,
      final boolean screenOpen,
      final boolean inReach) {
    return !disconnected && !workerGone && !screenOpen && inReach;
  }

  static Text label(final BlockPos from, final Found place) {
    final double dx = place.pos().getX() - from.getX();
    final double dz = place.pos().getZ() - from.getZ();
    return Text.translatable(
        "nightborn.scout.place",
        ScoutTrip.placeName(place.structure()),
        (int) Math.round(Math.sqrt(dx * dx + dz * dz)),
        Text.translatable(Bearing.of(dx, dz).translationKey()));
  }
}
