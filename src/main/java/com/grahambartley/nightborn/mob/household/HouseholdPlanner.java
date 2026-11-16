package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.decor.SeatEntity;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class HouseholdPlanner {

  static final int GRIEF_PAUSE_TICKS = 40;
  static final double GATHER_REACH = 5.0;

  private HouseholdPlanner() {}

  public static boolean calm(final VampireEntity vampire) {
    return vampire.homeLife().home().isPresent()
        && HouseholdCalm.calm(
            vampire.isSleeping(),
            vampire.getTarget() != null,
            Downed.state(vampire) != null,
            BatForm.isBat(vampire),
            vampire.homeLife().out(vampire, vampire.hungry()),
            vampire.homeLife().bedtime(vampire));
  }

  public static boolean settled(final VampireEntity vampire) {
    final long now = vampire.getWorld().getTime();
    return vampire.reading()
        || vampire.getVehicle() instanceof SeatEntity
        || vampire.householdLife().talking(now)
        || vampire.householdLife().engagement(now).isPresent();
  }

  public static boolean free(final VampireEntity mate, final long now) {
    return HouseholdCalm.free(
        mate.isAlive() && calm(mate),
        mate.householdLife().engagement(now).isPresent() || mate.householdLife().talking(now),
        mate.householdLife().grieving(now));
  }

  public static HouseholdTask next(
      final VampireEntity vampire, final ServerWorld world, final long now) {
    if (vampire.householdLife().grieving(now)) {
      return new StandTask(vampire, GRIEF_PAUSE_TICKS);
    }
    final BlockPos home = vampire.homeLife().home().orElseThrow();
    final NightPhase phase =
        NightPhase.of(world.getDimension().hasFixedTime(), world.getTimeOfDay());
    final Temperament temperament =
        vampire
            .householdLife()
            .temperament(world.getRegistryManager(), vampire.getRandom()::nextDouble);
    final int ticks = temperament.pastimeSeconds().get(vampire.getRandom()) * 20;
    final Map<FurnitureKind, List<BlockPos>> furniture =
        HomeFurniture.around(world, home, ServerConfigService.current().mobs().vampireHomeRadius());
    final Optional<HouseholdTask> chore = chore(vampire, world, phase, furniture);
    if (chore.isPresent()) {
      return chore.get();
    }
    if (phase == NightPhase.GATHER
        && vampire.getPos().squaredDistanceTo(Vec3d.ofBottomCenter(home))
            > GATHER_REACH * GATHER_REACH) {
      return StrollTask.to(vampire, Vec3d.ofBottomCenter(home), ticks);
    }
    final Optional<VampireEntity> partner = freeHousemate(vampire, world, now);
    final Optional<BlockPos> seat =
        FurniturePick.any(
            furniture.getOrDefault(FurnitureKind.SEAT, List.of()),
            spot -> FurnitureUse.seatFree(world, spot),
            vampire.getRandom().nextInt(Integer.MAX_VALUE));
    final Optional<BlockPos> books =
        FurniturePick.any(
            furniture.getOrDefault(FurnitureKind.BOOKS, List.of()),
            spot -> FurnitureUse.is(world, spot, FurnitureKind.BOOKS),
            vampire.getRandom().nextInt(Integer.MAX_VALUE));
    final Set<Hobby> available = EnumSet.of(Hobby.STROLL);
    seat.ifPresent(spot -> available.add(Hobby.SIT));
    books.ifPresent(spot -> available.add(Hobby.READ));
    partner.ifPresent(mate -> available.add(Hobby.CONVERSE));
    return switch (HobbyChoice.pick(temperament, phase, available, vampire.getRandom().nextDouble())
        .orElse(Hobby.STROLL)) {
      case SIT -> new SitTask(vampire, world, seat.orElseThrow(), ticks);
      case READ -> new ReadTask(vampire, world, books.orElseThrow(), ticks);
      case CONVERSE -> converse(vampire, world, now, temperament, partner.orElseThrow());
      case STROLL -> StrollTask.around(vampire, ticks);
    };
  }

  public static Optional<Chore> choreFor(
      final ServerWorld world, final NightPhase phase, final BlockPos pos) {
    final BlockState state = world.getBlockState(pos);
    return FurnitureKind.of(state)
        .flatMap(
            kind ->
                switch (kind) {
                  case LIGHT ->
                      Chore.forLight(phase, FurnitureUse.lit(state))
                          .filter(chore -> chore != Chore.LIGHT || FurnitureUse.lightable(state));
                  case DRAPE -> Chore.forDrape(phase, FurnitureUse.open(state));
                  case SEAT, BOOKS -> Optional.empty();
                });
  }

  private static Optional<HouseholdTask> chore(
      final VampireEntity vampire,
      final ServerWorld world,
      final NightPhase phase,
      final Map<FurnitureKind, List<BlockPos>> furniture) {
    if (!phase.choresDue()) {
      return Optional.empty();
    }
    for (final FurnitureKind kind : List.of(FurnitureKind.LIGHT, FurnitureKind.DRAPE)) {
      final Optional<BlockPos> spot =
          FurniturePick.nearest(
              furniture.getOrDefault(kind, List.of()),
              vampire.getPos(),
              pos ->
                  !vampire.householdLife().skips(pos, world.getTime())
                      && choreFor(world, phase, pos).isPresent());
      if (spot.isPresent()) {
        return Optional.of(
            new ChoreTask(
                vampire, world, spot.get(), choreFor(world, phase, spot.get()).orElseThrow()));
      }
    }
    return Optional.empty();
  }

  private static HouseholdTask converse(
      final VampireEntity vampire,
      final ServerWorld world,
      final long now,
      final Temperament temperament,
      final VampireEntity partner) {
    final Temperament theirs =
        partner
            .householdLife()
            .temperament(world.getRegistryManager(), partner.getRandom()::nextDouble);
    final List<Conversation.Line> lines =
        Conversation.lines(
            vampire.getRandom().nextInt(Integer.MAX_VALUE),
            vampire.getRandom().nextInt(100),
            temperament.temper(),
            theirs.temper());
    final long until = now + ConverseTask.length(lines.size());
    partner.householdLife().engage(vampire.getId(), until);
    vampire.householdLife().startTalking(until);
    return new ConverseTask(vampire, partner, lines);
  }

  private static Optional<VampireEntity> freeHousemate(
      final VampireEntity vampire, final ServerWorld world, final long now) {
    final int radius = ServerConfigService.current().mobs().vampireHomeRadius();
    return world
        .getEntitiesByClass(
            VampireEntity.class,
            vampire.getBoundingBox().expand(radius),
            mate ->
                mate != vampire
                    && Housemates.share(vampire.homeLife().home(), mate.homeLife().home())
                    && free(mate, now))
        .stream()
        .findAny();
  }
}
