package com.grahambartley.nightborn.boss;

import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ZoneMarkEntity extends Entity implements GeoEntity {

  static final RawAnimation TELL = RawAnimation.begin().thenLoop("tell");
  static final RawAnimation LINGER = RawAnimation.begin().thenLoop("linger");

  private static final TrackedData<Boolean> LINE =
      DataTracker.registerData(ZoneMarkEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Float> RADIUS =
      DataTracker.registerData(ZoneMarkEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<Float> LENGTH =
      DataTracker.registerData(ZoneMarkEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<String> LOOK =
      DataTracker.registerData(ZoneMarkEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Boolean> LINGERING =
      DataTracker.registerData(ZoneMarkEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private int life = Integer.MAX_VALUE;

  public ZoneMarkEntity(final EntityType<? extends ZoneMarkEntity> type, final World world) {
    super(type, world);
    this.noClip = true;
  }

  public static ZoneMarkEntity place(
      final World world, final ZoneShape shape, final double y, final Optional<Identifier> look) {
    final ZoneMarkEntity mark = new ZoneMarkEntity(BossEntities.ZONE_MARK, world);
    mark.dataTracker.set(LOOK, look.map(Identifier::toString).orElse(""));
    mark.dataTracker.set(LINE, shape.line());
    mark.dataTracker.set(RADIUS, (float) shape.radius());
    mark.dataTracker.set(LENGTH, (float) shape.length());
    mark.refreshPositionAndAngles(shape.midX(), y, shape.midZ(), shape.yaw(), 0.0F);
    world.spawnEntity(mark);
    return mark;
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    builder.add(LINE, false);
    builder.add(RADIUS, 1.0F);
    builder.add(LENGTH, 0.0F);
    builder.add(LOOK, "");
    builder.add(LINGERING, false);
  }

  public boolean line() {
    return dataTracker.get(LINE);
  }

  public float radius() {
    return dataTracker.get(RADIUS);
  }

  public float length() {
    return dataTracker.get(LENGTH);
  }

  public Optional<Identifier> look() {
    return Optional.ofNullable(Identifier.tryParse(dataTracker.get(LOOK)))
        .filter(id -> !dataTracker.get(LOOK).isEmpty());
  }

  public boolean lingering() {
    return dataTracker.get(LINGERING);
  }

  public void linger(final int ticks) {
    dataTracker.set(LINGERING, true);
    life = age + ticks;
  }

  @Override
  public void tick() {
    super.tick();
    if (!getWorld().isClient() && age >= life) {
      discard();
    }
  }

  @Override
  public boolean shouldSave() {
    return false;
  }

  @Override
  protected void readCustomDataFromNbt(final NbtCompound nbt) {}

  @Override
  protected void writeCustomDataToNbt(final NbtCompound nbt) {}

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "mark",
            0,
            state -> {
              state.setAnimation(lingering() ? LINGER : TELL);
              return PlayState.CONTINUE;
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
