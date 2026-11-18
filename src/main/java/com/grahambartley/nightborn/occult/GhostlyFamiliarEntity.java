package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.occult.goal.FamiliarAttackGoal;
import com.grahambartley.nightborn.occult.goal.FamiliarFollowGoal;
import com.grahambartley.nightborn.occult.goal.FamiliarScoutGoal;
import com.grahambartley.nightborn.occult.goal.FamiliarTargetGoal;
import com.grahambartley.nightborn.servant.Servant;
import com.grahambartley.nightborn.servant.ServantDeaths;
import com.grahambartley.nightborn.servant.Servants;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GhostlyFamiliarEntity extends PathAwareEntity
    implements GeoEntity, Servant, NameBearer {

  static final FamiliarPower DEFAULT_POWER =
      new FamiliarPower(1200, 10.0F, 3.0F, 24.0F, 200, 8.0F, 200, 9);
  static final int ANIMATION_BLEND_TICKS = 4;
  static final double BURIED_MARGIN = 1.0E-3;
  static final double FLYING_SPEED = 0.16;
  static final float AIR_DRAG = 0.91F;
  static final float WATER_DRAG = 0.8F;
  static final float WATER_PUSH = 0.02F;

  private static final TrackedData<Optional<UUID>> OWNER_UUID =
      DataTracker.registerData(
          GhostlyFamiliarEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
  private static final TrackedData<Integer> MODE =
      DataTracker.registerData(GhostlyFamiliarEntity.class, TrackedDataHandlerRegistry.INTEGER);
  private static final TrackedData<Long> SUMMONED =
      DataTracker.registerData(GhostlyFamiliarEntity.class, TrackedDataHandlerRegistry.LONG);
  private static final TrackedData<Boolean> FADING =
      DataTracker.registerData(GhostlyFamiliarEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

  private static final String OWNER = "Owner";
  private static final String MODE_KEY = "Mode";
  private static final String TICKS_LEFT = "TicksLeft";
  private static final String POWER = "Power";
  private static final String CARRIED = "Carried";
  private static final String SUMMONED_AT = "SummonedAt";

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private FamiliarPower power = DEFAULT_POWER;
  private SimpleInventory carried = new SimpleInventory(DEFAULT_POWER.carrySlots());
  private int ticksLeft = DEFAULT_POWER.lifetimeTicks();
  @Nullable private Vec3d scoutTarget;
  private int scoutTicks;

  public GhostlyFamiliarEntity(
      final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    moveControl = new FlightMoveControl(this, 20, true);
    setNoGravity(true);
    setCanPickUpLoot(false);
  }

  public static DefaultAttributeContainer.Builder createFamiliarAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, DEFAULT_POWER.health())
        .add(EntityAttributes.GENERIC_FLYING_SPEED, FLYING_SPEED)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, DEFAULT_POWER.damage());
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(OWNER_UUID, Optional.empty());
    builder.add(MODE, FamiliarMode.FIGHT.ordinal());
    builder.add(FADING, false);
    builder.add(SUMMONED, 0L);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(1, new FamiliarScoutGoal(this));
    goalSelector.add(2, new FamiliarAttackGoal(this));
    goalSelector.add(3, new FamiliarFollowGoal(this));
    goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
    goalSelector.add(8, new LookAroundGoal(this));
    targetSelector.add(1, new FamiliarTargetGoal(this));
  }

  @Override
  protected EntityNavigation createNavigation(final World world) {
    final BirdNavigation navigation = new BirdNavigation(this, world);
    navigation.setCanPathThroughDoors(false);
    navigation.setCanSwim(true);
    navigation.setCanEnterOpenDoors(true);
    return navigation;
  }

  public void summon(final UUID master, final FamiliarPower summoned, final int lifetimeTicks) {
    dataTracker.set(OWNER_UUID, Optional.of(master));
    power = summoned;
    carried = new SimpleInventory(summoned.carrySlots());
    ticksLeft = lifetimeTicks;
    dataTracker.set(SUMMONED, getWorld().getTime());
    applyPower();
    setHealth(getMaxHealth());
  }

  private void applyPower() {
    getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(power.health());
    getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(power.damage());
  }

  @Override
  protected ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    return isAlive() && !fading() ? FamiliarOrders.interact(this, player, hand) : ActionResult.PASS;
  }

  @Override
  public void onDeath(final DamageSource source) {
    super.onDeath(source);
    ServantDeaths.tell(this, owner());
  }

  @Override
  public void tick() {
    noClip = mode().passesThroughBlocks();
    super.tick();
    if (getWorld().isClient || !isAlive()) {
      return;
    }
    MobNaming.ensure(this);
    ticksLeft--;
    dataTracker.set(FADING, FamiliarRules.fading(ticksLeft));
    if (ticksLeft <= 0) {
      vanish();
    }
  }

  public void vanish() {
    if (!(getWorld() instanceof ServerWorld world)) {
      return;
    }
    dropCarried();
    world.spawnParticles(
        ParticleTypes.SOUL, getX(), getBodyY(0.5), getZ(), 10, 0.25, 0.3, 0.25, 0.02);
    world.playSound(
        null, getBlockPos(), OccultSounds.FAMILIAR_FADE, SoundCategory.NEUTRAL, 0.8F, 1.0F);
    discard();
  }

  @Override
  protected void dropInventory() {
    super.dropInventory();
    dropCarried();
  }

  private void dropCarried() {
    if (!carried.isEmpty()) {
      ItemScatterer.spawn(getWorld(), this, carried);
      carried.clear();
    }
  }

  public SimpleInventory carried() {
    return carried;
  }

  public void returnCarried(final PlayerEntity owner) {
    for (int slot = 0; slot < carried.size(); slot++) {
      owner.getInventory().offerOrDrop(carried.removeStack(slot));
    }
  }

  @Override
  public Optional<UUID> owner() {
    return dataTracker.get(OWNER_UUID);
  }

  public boolean isOwnedBy(final LivingEntity entity) {
    return Servants.serves(this, entity);
  }

  @Nullable
  public LivingEntity ownerHere() {
    return Servants.masterHere(this);
  }

  public FamiliarMode mode() {
    return FamiliarMode.byIndex(dataTracker.get(MODE));
  }

  public FamiliarPower power() {
    return power;
  }

  public boolean buried() {
    return !getWorld().isBlockSpaceEmpty(this, getBoundingBox().contract(BURIED_MARGIN));
  }

  public long summonedAt() {
    return dataTracker.get(SUMMONED);
  }

  public int ticksLeft() {
    return ticksLeft;
  }

  public boolean fading() {
    return dataTracker.get(FADING);
  }

  public void order(final FamiliarMode next, @Nullable final LivingEntity owner) {
    dataTracker.set(MODE, next.ordinal());
    getNavigation().stop();
    scoutTicks = 0;
    scoutTarget =
        next == FamiliarMode.SCOUT && owner != null
            ? FamiliarRules.scoutTarget(
                owner.getEyePos(), owner.getRotationVector(), power.scoutRange())
            : null;
    if (!FamiliarRules.fights(next)) {
      setTarget(null);
    }
  }

  @Nullable
  public Vec3d scoutTarget() {
    return scoutTarget;
  }

  public int scoutTicks() {
    return scoutTicks;
  }

  public int scoutTick() {
    return ++scoutTicks;
  }

  @Override
  public void travel(final Vec3d movementInput) {
    if (isLogicalSideForUpdatingMovement()) {
      final boolean wet = isTouchingWater() || isInLava();
      updateVelocity(wet ? WATER_PUSH : getMovementSpeed(), movementInput);
      move(MovementType.SELF, getVelocity());
      setVelocity(getVelocity().multiply(wet ? WATER_DRAG : AIR_DRAG));
    }
    updateLimbs(false);
  }

  @Override
  public boolean isInvulnerableTo(final DamageSource source) {
    return source.isOf(DamageTypes.IN_WALL) || super.isInvulnerableTo(source);
  }

  @Override
  public boolean handleFallDamage(
      final float fallDistance, final float damageMultiplier, final DamageSource source) {
    return false;
  }

  @Override
  protected void fall(
      final double heightDifference,
      final boolean onGround,
      final BlockState state,
      final BlockPos landedPosition) {}

  @Override
  public boolean isPushable() {
    return false;
  }

  @Override
  protected void tickCramming() {}

  @Override
  protected void pushAway(final Entity entity) {}

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return false;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return OccultSounds.FAMILIAR_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return OccultSounds.FAMILIAR_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return OccultSounds.FAMILIAR_DEATH;
  }

  @Override
  protected float getSoundVolume() {
    return 0.5F;
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state ->
                state.setAndContinue(
                    FamiliarPose.of(
                            fading(), getWorld().getTime() - summonedAt(), handSwinging, mode())
                        .clip())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    owner().ifPresent(id -> nbt.putUuid(OWNER, id));
    nbt.putString(MODE_KEY, mode().name());
    nbt.putInt(TICKS_LEFT, ticksLeft);
    nbt.putLong(SUMMONED_AT, summonedAt());
    FamiliarPower.CODEC
        .encodeStart(NbtOps.INSTANCE, power)
        .result()
        .ifPresent(encoded -> nbt.put(POWER, encoded));
    nbt.put(CARRIED, carried.toNbtList(getRegistryManager()));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(
        OWNER_UUID, nbt.containsUuid(OWNER) ? Optional.of(nbt.getUuid(OWNER)) : Optional.empty());
    dataTracker.set(MODE, FamiliarMode.byName(nbt.getString(MODE_KEY)).ordinal());
    dataTracker.set(SUMMONED, nbt.getLong(SUMMONED_AT));
    power =
        FamiliarPower.CODEC.parse(NbtOps.INSTANCE, nbt.get(POWER)).result().orElse(DEFAULT_POWER);
    ticksLeft = nbt.contains(TICKS_LEFT) ? nbt.getInt(TICKS_LEFT) : power.lifetimeTicks();
    carried = new SimpleInventory(power.carrySlots());
    carried.readNbtList(nbt.getList(CARRIED, NbtElement.COMPOUND_TYPE), getRegistryManager());
    applyPower();
  }
}
