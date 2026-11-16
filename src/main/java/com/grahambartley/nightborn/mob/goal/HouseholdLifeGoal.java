package com.grahambartley.nightborn.mob.goal;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.mob.household.Chore;
import com.grahambartley.nightborn.mob.household.ChoreTask;
import com.grahambartley.nightborn.mob.household.Conversation;
import com.grahambartley.nightborn.mob.household.ConverseTask;
import com.grahambartley.nightborn.mob.household.FurnitureKind;
import com.grahambartley.nightborn.mob.household.FurniturePick;
import com.grahambartley.nightborn.mob.household.FurnitureUse;
import com.grahambartley.nightborn.mob.household.Hobby;
import com.grahambartley.nightborn.mob.household.HobbyChoice;
import com.grahambartley.nightborn.mob.household.HouseholdCalm;
import com.grahambartley.nightborn.mob.household.HouseholdTask;
import com.grahambartley.nightborn.mob.household.NightPhase;
import com.grahambartley.nightborn.mob.household.ReadTask;
import com.grahambartley.nightborn.mob.household.SitTask;
import com.grahambartley.nightborn.mob.household.StandTask;
import com.grahambartley.nightborn.mob.household.StrollTask;
import com.grahambartley.nightborn.mob.household.Temperament;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class HouseholdLifeGoal extends Goal {

  static final int LISTEN_TICKS = 20;
  static final int GRIEF_PAUSE_TICKS = 40;
  static final double GATHER_REACH = 5.0;

  private final VampireEntity vampire;
  @Nullable private HouseholdTask task;

  public HouseholdLifeGoal(final VampireEntity vampire) {
    this.vampire = vampire;
    setControls(EnumSet.of(Control.MOVE, Control.LOOK));
  }

  @Override
  public boolean canStart() {
    return calm();
  }

  @Override
  public boolean shouldContinue() {
    return calm();
  }

  @Override
  public void start() {
    task = null;
  }

  @Override
  public void tick() {
    if (!(vampire.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final long now = world.getTime();
    final Optional<VampireEntity> speaker = engagedBy(world, now);
    if (speaker.isPresent()) {
      vampire.getNavigation().stop();
      vampire.getLookControl().lookAt(speaker.get());
      return;
    }
    if (task == null || !task.tick()) {
      endTask();
      task = next(world, now);
    }
  }

  @Override
  public void stop() {
    endTask();
  }

  private void endTask() {
    if (task != null) {
      task.stop();
      task = null;
    }
  }

  private Optional<VampireEntity> engagedBy(final ServerWorld world, final long now) {
    return vampire
        .householdLife()
        .engagement(now)
        .map(engagement -> world.getEntityById(engagement.partnerId()))
        .filter(VampireEntity.class::isInstance)
        .map(VampireEntity.class::cast);
  }

  private HouseholdTask next(final ServerWorld world, final long now) {
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
        vampire
            .householdLife()
            .furniture(world, home, ServerConfigService.current().mobs().vampireHomeRadius());
    final Optional<HouseholdTask> chore = chore(world, phase, furniture);
    if (chore.isPresent()) {
      return chore.get();
    }
    if (phase == NightPhase.GATHER
        && vampire.getPos().squaredDistanceTo(Vec3d.ofBottomCenter(home))
            > GATHER_REACH * GATHER_REACH) {
      return StrollTask.to(vampire, Vec3d.ofBottomCenter(home), ticks);
    }
    final Optional<VampireEntity> partner = freeHousemate(world, now);
    final Optional<BlockPos> seat =
        FurniturePick.any(
            furniture.getOrDefault(FurnitureKind.SEAT, List.of()),
            spot -> FurnitureUse.seatFree(world, spot),
            vampire.getRandom().nextInt(Integer.MAX_VALUE));
    final Optional<BlockPos> books =
        FurniturePick.any(
            furniture.getOrDefault(FurnitureKind.BOOKS, List.of()),
            spot -> true,
            vampire.getRandom().nextInt(Integer.MAX_VALUE));
    final Set<Hobby> available = EnumSet.of(Hobby.STROLL);
    seat.ifPresent(spot -> available.add(Hobby.SIT));
    books.ifPresent(spot -> available.add(Hobby.READ));
    partner.ifPresent(mate -> available.add(Hobby.CONVERSE));
    return switch (HobbyChoice.pick(temperament, phase, available, vampire.getRandom().nextDouble())
        .orElse(Hobby.STROLL)) {
      case SIT -> new SitTask(vampire, world, seat.orElseThrow(), ticks);
      case READ -> new ReadTask(vampire, books.orElseThrow(), ticks);
      case CONVERSE -> converse(world, now, temperament, partner.orElseThrow());
      case STROLL -> StrollTask.around(vampire, ticks);
    };
  }

  private Optional<HouseholdTask> chore(
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
              pos -> choreFor(world, phase, kind, pos).isPresent());
      if (spot.isPresent()) {
        return Optional.of(
            new ChoreTask(
                vampire,
                world,
                spot.get(),
                choreFor(world, phase, kind, spot.get()).orElseThrow()));
      }
    }
    return Optional.empty();
  }

  private static Optional<Chore> choreFor(
      final ServerWorld world,
      final NightPhase phase,
      final FurnitureKind kind,
      final BlockPos pos) {
    final var state = world.getBlockState(pos);
    if (FurnitureKind.of(state).filter(kind::equals).isEmpty()) {
      return Optional.empty();
    }
    return kind == FurnitureKind.LIGHT
        ? Chore.forLight(phase, FurnitureUse.lit(state))
        : Chore.forDrape(phase, FurnitureUse.open(state));
  }

  private HouseholdTask converse(
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
    partner.householdLife().engage(vampire.getId(), now + ConverseTask.length(lines.size()));
    return new ConverseTask(vampire, partner, lines);
  }

  private Optional<VampireEntity> freeHousemate(final ServerWorld world, final long now) {
    final int radius = ServerConfigService.current().mobs().vampireHomeRadius();
    return world
        .getEntitiesByClass(
            VampireEntity.class,
            vampire.getBoundingBox().expand(radius),
            mate ->
                mate != vampire
                    && Housemates.share(vampire.homeLife().home(), mate.homeLife().home())
                    && HouseholdCalm.free(
                        mate.isAlive(),
                        mate.isSleeping(),
                        mate.getTarget() != null,
                        mate.householdLife().engagement(now).isPresent(),
                        mate.householdLife().grieving(now)))
        .stream()
        .findAny();
  }

  private boolean calm() {
    return vampire.homeLife().home().isPresent()
        && HouseholdCalm.calm(
            vampire.isSleeping(),
            vampire.getTarget() != null,
            Downed.state(vampire) != null,
            BatForm.isBat(vampire),
            vampire.homeLife().out(vampire, vampire.hungry()),
            vampire.homeLife().bedtime(vampire));
  }
}
