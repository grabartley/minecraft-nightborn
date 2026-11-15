package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.config.WardSettings;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
  static final int LINGER_TICKS = SCAN_TICKS + 5;
  static final int TOUCH_TICKS = 60;

  private Wards() {}

  public static void tick(final LivingEntity vampire, final WardSettings settings) {
    if (!(vampire.getWorld() instanceof ServerWorld world)
        || !WardCadence.due(world.getTime(), vampire.getId(), SCAN_TICKS)) {
      return;
    }
    if (holds(vampire, WardTags.GARLIC)) {
      touch(vampire, settings);
    }
    Weakened.set(vampire, STRINGS, strings(world, vampire, settings), LINGER_TICKS);
    Weakened.set(vampire, CRUCIFIX, crucifixes(world, vampire, settings), LINGER_TICKS);
    Weakened.set(
        vampire,
        SHAPE,
        CrucifixShapes.get(world).near(world, vampire.getBlockPos(), settings.shapeRange()).stream()
                .anyMatch(centre -> seesShape(world, vampire, centre))
            ? settings.shapeStrength()
            : 0,
        LINGER_TICKS);
  }

  public static void touch(final Entity entity, final WardSettings settings) {
    if (entity instanceof LivingEntity living && Vampires.isVampire(living)) {
      Weakened.add(living, TOUCH, settings.touchStrength(), TOUCH_TICKS);
    }
  }

  public static boolean holdsCrucifix(final LivingEntity entity) {
    return holds(entity, WardTags.CRUCIFIXES);
  }

  private static boolean holds(final LivingEntity entity, final TagKey<Item> tag) {
    final ItemStack main = entity.getMainHandStack();
    final ItemStack off = entity.getOffHandStack();
    return main.isIn(tag) || off.isIn(tag);
  }

  private static int strings(
      final ServerWorld world, final LivingEntity vampire, final WardSettings settings) {
    final long count =
        world
            .getPointOfInterestStorage()
            .count(
                type -> type.matchesKey(WardPoints.GARLIC_STRING),
                vampire.getBlockPos(),
                settings.stringRange(),
                PointOfInterestStorage.OccupationStatus.ANY);
    return WardStrength.strings(
        (int) Math.min(Integer.MAX_VALUE, count), settings.stringStrength(), settings.stringCap());
  }

  private static int crucifixes(
      final ServerWorld world, final LivingEntity vampire, final WardSettings settings) {
    final double range = settings.crucifixRange();
    final List<LivingEntity> holders =
        world.getEntitiesByClass(
            LivingEntity.class,
            new Box(vampire.getBlockPos()).expand(range),
            holder ->
                holder != vampire
                    && holder.isAlive()
                    && !holder.isSpectator()
                    && holder.squaredDistanceTo(vampire) <= range * range
                    && holdsCrucifix(holder)
                    && vampire.canSee(holder));
    int strongest = 0;
    for (final LivingEntity holder : holders) {
      strongest =
          Math.max(
              strongest,
              WardStrength.crucifix(
                  settings.crucifixStrength(), faith(holder), settings.faithBonus()));
    }
    return strongest;
  }

  private static double faith(final LivingEntity holder) {
    return holder.getAttributes().hasAttribute(HunterAttributes.FAITH)
        ? holder.getAttributeValue(HunterAttributes.FAITH)
        : 0.0;
  }

  private static boolean seesShape(
      final ServerWorld world, final LivingEntity vampire, final BlockPos centre) {
    final BlockHitResult hit =
        world.raycast(
            new RaycastContext(
                vampire.getEyePos(),
                Vec3d.ofCenter(centre),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                vampire));
    return hit.getType() == HitResult.Type.MISS
        || CrucifixShape.partOf(centre, hit.getBlockPos())
            && world.getBlockState(hit.getBlockPos()).isIn(WardTags.CRUCIFIX_MATERIALS);
  }
}
