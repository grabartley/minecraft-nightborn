package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.server.ServerConfigService;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VampireBatEntity extends BatEntity implements GeoEntity {

  static final double HUNT_RANGE = 12.0;
  static final double BITE_REACH = 1.4;
  static final int BITE_COOLDOWN_TICKS = 50;
  static final int SEARCH_INTERVAL_TICKS = 20;
  static final int ANIMATION_BLEND_TICKS = 3;
  static final byte BITE_STATUS = 61;
  static final int BITE_SHOWN_TICKS = 10;

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  @Nullable private LivingEntity prey;
  private int biteCooldown;
  private int biteShownTicks;

  public VampireBatEntity(final EntityType<? extends BatEntity> type, final World world) {
    super(type, world);
  }

  public static DefaultAttributeContainer.Builder createVampireBatAttributes() {
    return BatEntity.createBatAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, HUNT_RANGE);
  }

  @Override
  protected void mobTick() {
    if (biteCooldown > 0) {
      biteCooldown--;
    }
    if (age % SEARCH_INTERVAL_TICKS == 0 || !stillHunting(prey)) {
      prey = findPrey();
    }
    if (prey == null || biteCooldown > 0) {
      super.mobTick();
      return;
    }
    if (isRoosting()) {
      setRoosting(false);
    }
    final Vec3d velocity =
        BatSteering.toward(
            getVelocity(), prey.getEyePos().subtract(0.0, 0.3, 0.0).subtract(getPos()));
    setVelocity(velocity);
    setYaw(BatSteering.yaw(velocity));
    forwardSpeed = 0.5F;
    if (squaredDistanceTo(prey) < BITE_REACH * BITE_REACH) {
      bite(prey);
    }
  }

  private void bite(final LivingEntity victim) {
    final NightbornConfig config = ServerConfigService.current();
    if (MobBite.bite(
        this,
        victim,
        (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
        config.turning().vampireBatTurnChance(),
        config.turning())) {
      getWorld().sendEntityStatus(this, BITE_STATUS);
    }
    biteCooldown = BITE_COOLDOWN_TICKS;
    setVelocity(getVelocity().multiply(-0.6, 0.5, -0.6).add(0.0, 0.3, 0.0));
  }

  @Override
  public void handleStatus(final byte status) {
    if (status == BITE_STATUS) {
      biteShownTicks = BITE_SHOWN_TICKS;
      return;
    }
    super.handleStatus(status);
  }

  @Override
  public void tick() {
    super.tick();
    if (biteShownTicks > 0) {
      biteShownTicks--;
    }
  }

  private boolean stillHunting(@Nullable final LivingEntity target) {
    return target != null
        && target.isAlive()
        && huntingNow()
        && Prey.isPrey(target)
        && squaredDistanceTo(target) < HUNT_RANGE * HUNT_RANGE;
  }

  private boolean huntingNow() {
    final World world = getWorld();
    return world.getDifficulty() != Difficulty.PEACEFUL
        && Prey.batHunts(
            world.getDimension().hasFixedTime(),
            world.getTimeOfDay(),
            world.isSkyVisible(BlockPos.ofFloored(getX(), getEyeY(), getZ())));
  }

  @Nullable
  private LivingEntity findPrey() {
    if (!huntingNow()) {
      return null;
    }
    LivingEntity closest = null;
    double best = HUNT_RANGE * HUNT_RANGE;
    for (final LivingEntity candidate :
        getWorld()
            .getEntitiesByClass(
                LivingEntity.class,
                Box.of(getPos(), HUNT_RANGE * 2, HUNT_RANGE * 2, HUNT_RANGE * 2),
                entity -> entity != this && entity.isAlive() && Prey.isPrey(entity))) {
      final double distance = squaredDistanceTo(candidate);
      if (distance < best && canSee(candidate)) {
        best = distance;
        closest = candidate;
      }
    }
    return closest;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state ->
                state.setAndContinue(VampireBatPose.of(isRoosting(), biteShownTicks > 0).clip())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
