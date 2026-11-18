package com.grahambartley.nightborn.servant.job.goal;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodBottleItem;
import com.grahambartley.nightborn.blood.BloodGifts;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.BloodQuality;
import com.grahambartley.nightborn.feeding.Lancet;
import com.grahambartley.nightborn.feeding.LancetRules;
import com.grahambartley.nightborn.servant.job.BloodRounds;
import com.grahambartley.nightborn.servant.job.CarryRoom;
import com.grahambartley.nightborn.servant.job.DayJob;
import com.grahambartley.nightborn.servant.job.DayWorker;
import com.grahambartley.nightborn.servant.job.JobSupplies;
import com.grahambartley.nightborn.servant.job.RoundRules;
import com.grahambartley.nightborn.servant.job.RoundRules.Step;
import com.grahambartley.nightborn.servant.job.ToolWear;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class FetchBloodGoal<T extends PathAwareEntity & DayWorker> extends ChestRoundGoal<T> {

  static final int LANCETS = 1;
  public static final TagKey<EntityType<?>> SPARED =
      TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Nightborn.MOD_ID, "servants_spare"));

  private final Set<UUID> skipped = new HashSet<>();
  @Nullable private LivingEntity target;
  private int sinceDraw = BloodRounds.DRAW_TICKS;

  public FetchBloodGoal(final T worker) {
    super(worker, DayJob.FETCH_BLOOD);
  }

  @Override
  public void start() {
    skipped.clear();
    super.start();
  }

  @Override
  protected void stock(final Inventory chest) {
    JobSupplies.take(chest, worker.getInventory(), stack -> stack.isOf(BloodItems.LANCET), LANCETS);
    JobSupplies.take(
        chest,
        worker.getInventory(),
        stack -> stack.isOf(Items.GLASS_BOTTLE),
        Items.GLASS_BOTTLE.getMaxCount());
  }

  @Override
  protected boolean readyToWork() {
    return JobSupplies.holds(worker.getInventory(), stack -> stack.isOf(BloodItems.LANCET))
        && hasEmptyBottle();
  }

  @Override
  protected void forgetTarget() {
    target = null;
  }

  @Override
  protected Step work(final ServerWorld world) {
    sinceDraw++;
    if (!readyToWork()) {
      return Step.BACK;
    }
    if (target == null || !target.isAlive()) {
      target = nextTarget(world);
      restartLegClock();
      if (target == null) {
        return Step.NOTHING_LEFT;
      }
    }
    if (RoundRules.stuck(ticksOnLeg())) {
      skip();
      return Step.WORKING;
    }
    if (!walkTo(target.getPos())) {
      return Step.WORKING;
    }
    worker.getLookControl().lookAt(target);
    if (!BloodRounds.drawsNow(sinceDraw)) {
      return Step.WORKING;
    }
    return bleed(world, target);
  }

  private Step bleed(final ServerWorld world, final LivingEntity creature) {
    final Optional<BloodQuality> quality = BloodQuality.of(creature.getType());
    if (quality.isEmpty() || Lancet.ruleFor(creature, hasEmptyBottle()) != LancetRules.BLEEDS) {
      skip();
      return Step.WORKING;
    }
    final Optional<Identifier> gift =
        BloodGifts.forPrey(world.getRegistryManager(), creature.getType());
    if (!CarryRoom.fits(worker.getInventory(), List.of(BloodBottleItem.of(quality.get(), gift)))) {
      return Step.BACK;
    }
    sinceDraw = 0;
    worker.swingHand(Hand.MAIN_HAND);
    final Optional<ItemStack> bottle =
        Lancet.drawBottle(world, worker, creature, quality.get(), gift);
    if (bottle.isEmpty()) {
      skip();
      return Step.WORKING;
    }
    worker.getInventory().removeItem(Items.GLASS_BOTTLE, 1);
    worker.getInventory().addStack(bottle.get());
    ToolWear.wear(
        world,
        worker,
        JobSupplies.first(worker.getInventory(), stack -> stack.isOf(BloodItems.LANCET)));
    restartLegClock();
    return Step.WORKING;
  }

  private boolean hasEmptyBottle() {
    return worker.getInventory().count(Items.GLASS_BOTTLE) > 0;
  }

  private void skip() {
    if (target != null) {
      skipped.add(target.getUuid());
    }
    target = null;
  }

  @Nullable
  private LivingEntity nextTarget(final ServerWorld world) {
    final int range = ServerConfigService.current().servants().gatherRange();
    final Vec3d centre = Vec3d.ofBottomCenter(place());
    return world
        .getEntitiesByClass(
            LivingEntity.class,
            new Box(centre, centre).expand(range),
            creature ->
                creature != worker
                    && !skipped.contains(creature.getUuid())
                    && BloodRounds.prey(
                        BloodQuality.of(creature.getType())
                            .filter(BloodQuality.ANIMAL::equals)
                            .isPresent(),
                        creature instanceof Tameable tame && tame.getOwnerUuid() != null
                            || creature instanceof AbstractHorseEntity horse && horse.isTame(),
                        creature.getType().isIn(SPARED))
                    && Lancet.ruleFor(creature, hasEmptyBottle()) == LancetRules.BLEEDS)
        .stream()
        .min(Comparator.comparingDouble(worker::squaredDistanceTo))
        .orElse(null);
  }
}
