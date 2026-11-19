package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.clothing.WornAttributes;
import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import com.grahambartley.nightborn.weakened.WeakenedSources;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.poi.PointOfInterestStorage;

public final class Wards {

  public static final String STRINGS = "garlic_strings";
  public static final String TOUCH = "garlic_touch";
  public static final String CRUCIFIX = "crucifix";
  public static final String SHAPE = "crucifix_shape";

  static final int SCAN_TICKS = 10;
  static final int TICKS_PER_SECOND = 20;
  static final int LINGER_TICKS = SCAN_TICKS + 5;

  private Wards() {}

  public static void tick(final LivingEntity vampire, final WardSettings settings) {
    if (!(vampire.getWorld() instanceof ServerWorld world)
        || !WardCadence.due(world.getTime(), vampire.getId(), SCAN_TICKS)) {
      return;
    }
    if (holds(vampire, WardTags.GARLIC)) {
      touch(vampire, settings);
    }
    final int strings = stringsAround(world, vampire, settings);
    final int stringStrength =
        dulled(
            vampire,
            WardStrength.strings(strings, settings.stringStrength(), settings.stringCap()));
    if (stringStrength > 0) {
      MilestoneCriterion.reach(vampire, Milestone.GARLIC_STRINGS, strings);
    }
    final WeakenedSources before = Weakened.sources(vampire);
    final int crucifix = dulled(vampire, crucifixes(world, vampire, settings));
    final int shape = dulled(vampire, shapes(world, vampire, settings));
    Weakened.set(vampire, STRINGS, stringStrength, LINGER_TICKS);
    Weakened.set(vampire, CRUCIFIX, crucifix, LINGER_TICKS);
    Weakened.set(vampire, SHAPE, shape, LINGER_TICKS);
    Flinch.show(
        vampire,
        Flinch.starts(Flinch.inReach(before.strength(STRINGS)), Flinch.inReach(stringStrength)),
        Flinch.starts(
            Flinch.inReach(before.strength(CRUCIFIX), before.strength(SHAPE)),
            Flinch.inReach(crucifix, shape)));
  }

  public static void touch(final Entity entity, final WardSettings settings) {
    if (entity instanceof LivingEntity living && Vampires.isVampire(living)) {
      Weakened.add(
          living,
          TOUCH,
          dulled(living, settings.touchStrength()),
          settings.touchSeconds() * TICKS_PER_SECOND);
    }
  }

  public static int dulled(final LivingEntity vampire, final int strength) {
    return WardStrength.dulled(
        strength, WornAttributes.value(vampire, WornAttributes.WARD_RESISTANCE));
  }

  public static boolean wards(final LivingEntity entity) {
    return holds(entity, WardTags.CRUCIFIXES) && !Vampires.isVampire(entity);
  }

  private static boolean holds(final LivingEntity entity, final TagKey<Item> tag) {
    return entity.isHolding(stack -> stack.isIn(tag));
  }

  private static int stringsAround(
      final ServerWorld world, final LivingEntity vampire, final WardSettings settings) {
    if (settings.stringStrength() == 0 || settings.stringCap() == 0) {
      return 0;
    }
    final long count =
        world
            .getPointOfInterestStorage()
            .count(
                type -> type.matchesKey(WardPoints.GARLIC_STRING),
                vampire.getBlockPos(),
                settings.stringRange(),
                PointOfInterestStorage.OccupationStatus.ANY);
    return (int) Math.min(Integer.MAX_VALUE, count);
  }

  private static int crucifixes(
      final ServerWorld world, final LivingEntity vampire, final WardSettings settings) {
    if (settings.crucifixStrength() == 0) {
      return 0;
    }
    final double range = settings.crucifixRange();
    final List<LivingEntity> holders =
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(vampire.getBlockPos()).expand(range),
            holder ->
                holder.isAlive()
                    && !holder.isSpectator()
                    && within(holder, vampire, crucifixReach(holder, range))
                    && vampire.canSee(holder));
    int strongest = 0;
    for (final LivingEntity holder : holders) {
      strongest =
          Math.max(
              strongest,
              WardStrength.crucifix(
                  settings.crucifixStrength(),
                  HunterAttributes.faith(holder),
                  settings.faithBonus()));
    }
    return strongest;
  }

  private static double crucifixReach(final LivingEntity holder, final double range) {
    return WornWard.reach(
        holds(holder, WardTags.CRUCIFIXES),
        Vampires.isVampire(holder),
        GiftAttributes.value(holder, GiftAttributes.WORN_WARD),
        range);
  }

  private static boolean within(
      final LivingEntity holder, final LivingEntity vampire, final double reach) {
    return reach > 0.0 && holder.squaredDistanceTo(vampire) <= reach * reach;
  }

  private static int shapes(
      final ServerWorld world, final LivingEntity vampire, final WardSettings settings) {
    if (settings.shapeStrength() == 0) {
      return 0;
    }
    final boolean seesShape =
        CrucifixShapes.get(world).near(world, vampire.getBlockPos(), settings.shapeRange()).stream()
            .anyMatch(centre -> seesShape(world, vampire, centre));
    return seesShape || seesHungCrucifix(world, vampire, settings.shapeRange())
        ? settings.shapeStrength()
        : 0;
  }

  private static boolean seesHungCrucifix(
      final ServerWorld world, final LivingEntity vampire, final int range) {
    return world
        .getPointOfInterestStorage()
        .getPositions(
            type -> type.matchesKey(WardPoints.HUNG_CRUCIFIX),
            pos -> true,
            vampire.getBlockPos(),
            range,
            PointOfInterestStorage.OccupationStatus.ANY)
        .anyMatch(pos -> seesBlock(world, vampire, pos));
  }

  private static boolean seesShape(
      final ServerWorld world, final LivingEntity vampire, final BlockPos centre) {
    final BlockHitResult hit = sightLine(world, vampire, centre);
    return hit.getType() == HitResult.Type.MISS
        || CrucifixShape.blocks(
                centre, pos -> world.getBlockState(pos).isIn(WardTags.CRUCIFIX_MATERIALS))
            .contains(hit.getBlockPos());
  }

  private static boolean seesBlock(
      final ServerWorld world, final LivingEntity vampire, final BlockPos target) {
    final BlockHitResult hit = sightLine(world, vampire, target);
    return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(target);
  }

  private static BlockHitResult sightLine(
      final ServerWorld world, final LivingEntity vampire, final BlockPos target) {
    return world.raycast(
        new RaycastContext(
            vampire.getEyePos(),
            Vec3d.ofCenter(target),
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.NONE,
            vampire));
  }
}
