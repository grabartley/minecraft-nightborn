package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.servant.Bindable;
import com.grahambartley.nightborn.servant.goal.KeepCloseGoal;
import com.grahambartley.nightborn.servant.goal.MastersFoeGoal;
import com.grahambartley.nightborn.smoke.SmokePoof;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RestlessDeadEntity extends HostileEntity implements Bindable, GeoEntity {

  static final String MASTER = "Master";
  static final String TICKS_LEFT = "TicksLeft";
  static final String LOOK_KEY = "Look";
  static final String RAISED_KEY = "RaisedAt";
  static final int ANIMATION_BLEND_TICKS = 4;
  static final double KEEP_CLOSE = 12.0;
  static final double CLOSE_ENOUGH = 4.0;

  private static final TrackedData<String> LOOK =
      DataTracker.registerData(RestlessDeadEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Long> RAISED =
      DataTracker.registerData(RestlessDeadEntity.class, TrackedDataHandlerRegistry.LONG);
  private static final TrackedData<Boolean> SINKING =
      DataTracker.registerData(RestlessDeadEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Long> STRUCK =
      DataTracker.registerData(RestlessDeadEntity.class, TrackedDataHandlerRegistry.LONG);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final RestlessLookCache worn = new RestlessLookCache();

  private Optional<UUID> master = Optional.empty();
  private int ticksLeft;
  private int sinkLeft = RestlessPose.SINK_TICKS;

  public RestlessDeadEntity(final EntityType<? extends HostileEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 0;
    if (!world.isClient()) {
      dataTracker.set(
          LOOK,
          RestlessLook.pick(RestlessDead.looks(world.getRegistryManager()), random.nextDouble())
              .map(Identifier::toString)
              .orElse(""));
      dataTracker.set(RAISED, world.getTime());
    }
  }

  public static DefaultAttributeContainer.Builder createRestlessAttributes() {
    return HostileEntity.createHostileAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(LOOK, "");
    builder.add(RAISED, RestlessPose.NEVER);
    builder.add(SINKING, false);
    builder.add(STRUCK, RestlessPose.NEVER);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new MeleeAttackGoal(this, 1.1, false));
    goalSelector.add(2, new KeepCloseGoal(this, KEEP_CLOSE, CLOSE_ENOUGH));
    goalSelector.add(3, new WanderAroundFarGoal(this, 0.8));
    goalSelector.add(4, new LookAroundGoal(this));
    targetSelector.add(1, new MastersFoeGoal(this));
  }

  @Override
  public Optional<UUID> owner() {
    return master;
  }

  @Override
  public void bind(final UUID owner, final int lifetimeTicks) {
    master = Optional.of(owner);
    ticksLeft = lifetimeTicks;
  }

  public static boolean expires(final boolean bound, final int ticksLeft) {
    return !bound || ticksLeft <= 0;
  }

  @Override
  public void tick() {
    super.tick();
    if (!(getWorld() instanceof ServerWorld)) {
      return;
    }
    if (!sinking() && expires(master.isPresent(), --ticksLeft)) {
      dataTracker.set(SINKING, true);
    }
    if (sinking() && --sinkLeft <= 0) {
      SmokePoof.burst(this, SoundEvents.ENTITY_ZOMBIE_DEATH);
      discard();
    }
  }

  public String look() {
    return dataTracker.get(LOOK);
  }

  public boolean sinking() {
    return dataTracker.get(SINKING);
  }

  public Optional<RestlessLook> wornLook() {
    return worn.resolve(
        look(), id -> RestlessLook.worn(RestlessDead.looks(getRegistryManager()), id));
  }

  public long sinceRaised() {
    return RestlessPose.since(dataTracker.get(RAISED), getWorld().getTime());
  }

  public long sinceStruck() {
    return RestlessPose.since(dataTracker.get(STRUCK), getWorld().getTime());
  }

  public boolean held() {
    return RestlessPose.held(sinking(), sinceRaised());
  }

  @Override
  protected boolean isImmobile() {
    return super.isImmobile() || held();
  }

  @Override
  public boolean tryAttack(final Entity target) {
    if (held() || !super.tryAttack(target)) {
      return false;
    }
    dataTracker.set(STRUCK, getWorld().getTime());
    return true;
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return false;
  }

  @Override
  protected boolean shouldDropLoot() {
    return false;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return SoundEvents.ENTITY_ZOMBIE_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_ZOMBIE_DEATH;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    master.ifPresent(id -> nbt.putUuid(MASTER, id));
    nbt.putInt(TICKS_LEFT, ticksLeft);
    nbt.putString(LOOK_KEY, look());
    nbt.putLong(RAISED_KEY, dataTracker.get(RAISED));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    master = nbt.containsUuid(MASTER) ? Optional.of(nbt.getUuid(MASTER)) : Optional.empty();
    ticksLeft = nbt.getInt(TICKS_LEFT);
    if (!nbt.getString(LOOK_KEY).isEmpty()) {
      dataTracker.set(LOOK, nbt.getString(LOOK_KEY));
    }
    dataTracker.set(
        RAISED, nbt.contains(RAISED_KEY) ? nbt.getLong(RAISED_KEY) : RestlessPose.NEVER);
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state -> {
              final RestlessPose pose =
                  RestlessPose.of(
                      sinking(), sinceRaised(), hurtTime > 0, sinceStruck(), state.isMoving());
              state.getController().transitionLength(pose.blendTicks(ANIMATION_BLEND_TICKS));
              return state.setAndContinue(pose.clip());
            }));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
