package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.blood.BloodValue;
import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.goal.FleeWhileEscapingGoal;
import com.grahambartley.nightborn.mob.goal.LieDownedGoal;
import com.grahambartley.nightborn.mob.goal.SeekShadeGoal;
import com.grahambartley.nightborn.mob.goal.SleepInCoffinGoal;
import com.grahambartley.nightborn.mob.goal.WatchStrangerGoal;
import com.grahambartley.nightborn.mob.home.HomeLife;
import com.grahambartley.nightborn.mob.home.Housemates;
import com.grahambartley.nightborn.mob.home.MobBlood;
import com.grahambartley.nightborn.mob.look.Appearance;
import com.grahambartley.nightborn.mob.look.OutfitDressing;
import com.grahambartley.nightborn.mob.look.VampireLook;
import com.grahambartley.nightborn.mob.look.VampireLooks;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireSize;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.HouseholdMember;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VampireEntity extends HostileEntity implements GeoEntity, HouseholdMember {

  static final String LOOK_KEY = "Look";
  static final String HEIGHT_KEY = "Height";
  static final String WIDTH_KEY = "Width";
  static final int ANIMATION_BLEND_TICKS = 4;
  static final String HISS_CONTROLLER = "hiss";
  static final String HISS_CLIP = "hiss";
  static final RawAnimation HISS = RawAnimation.begin().thenPlay(HISS_CLIP);

  private static final TrackedData<String> LOOK =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Float> HEIGHT =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<Float> WIDTH =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.FLOAT);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final EndingEdge landingEdge = new EndingEdge();
  private final HomeLife homeLife = new HomeLife();
  private final MobBlood blood = new MobBlood();
  private VampirePose shownPose = VampirePose.IDLE;
  private long nextHiss;
  private boolean warded;
  private boolean undressed;

  public VampireEntity(final EntityType<? extends VampireEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 10;
  }

  public static DefaultAttributeContainer.Builder createVampireAttributes() {
    return HostileEntity.createHostileAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(LOOK, "");
    builder.add(HEIGHT, VampireSize.BASE_HEIGHT);
    builder.add(WIDTH, 1.0F);
  }

  @Override
  public void onTrackedDataSet(final TrackedData<?> data) {
    super.onTrackedDataSet(data);
    if (HEIGHT.equals(data) || WIDTH.equals(data)) {
      calculateDimensions();
    }
  }

  @Override
  public EntityDimensions getBaseDimensions(final EntityPose pose) {
    return VampireSize.dimensions(super.getBaseDimensions(pose), height(), width());
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new LieDownedGoal(this));
    goalSelector.add(1, new SwimGoal(this));
    goalSelector.add(1, new FleeWhileEscapingGoal(this, 1.4));
    goalSelector.add(2, new SeekShadeGoal(this, 1.3));
    goalSelector.add(3, new AvoidSunlightGoal(this));
    goalSelector.add(3, new SleepInCoffinGoal(this));
    goalSelector.add(4, new MeleeAttackGoal(this, 1.15, false));
    goalSelector.add(5, new GoToWalkTargetGoal(this, 1.0));
    goalSelector.add(6, new WatchStrangerGoal(this));
    goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
    goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    goalSelector.add(7, new LookAroundGoal(this));
    targetSelector.add(1, new RevengeGoal(this));
    targetSelector.add(
        2, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, this::hunts));
  }

  public boolean hunts(final LivingEntity target) {
    return Downed.state(this) == null
        && !isSleeping()
        && Prey.isPrey(target)
        && !Prey.spared(
            getWorld().getDifficulty() == Difficulty.PEACEFUL, target instanceof PlayerEntity)
        && !Hunting.dropsTarget(pursuit(target, false));
  }

  private Hunting.Pursuit pursuit(final LivingEntity target, final boolean provoked) {
    final NightbornConfig config = ServerConfigService.current();
    final boolean hungry = blood.hungry(this, config);
    return new Hunting.Pursuit(
        Hunting.vampireHunts(getWorld()),
        hungry,
        ShadeRules.endangered(this),
        provoked,
        homeLife.outOfRange(this, target, provoked, hungry, config));
  }

  public boolean hungry() {
    return blood.hungry(this, ServerConfigService.current());
  }

  public HomeLife homeLife() {
    return homeLife;
  }

  @Override
  public void settleAt(final BlockPos home) {
    homeLife.settle(home);
  }

  @Override
  public boolean tryAttack(final Entity target) {
    if (!(target instanceof LivingEntity victim)
        || !Prey.mayBite(
            Downed.state(this) != null,
            Prey.isPrey(victim),
            victim == getTarget() || victim == getAttacker())) {
      return false;
    }
    hiss();
    final NightbornConfig config = ServerConfigService.current();
    final float before = victim.getHealth();
    final boolean bitten =
        MobBite.bite(
            this,
            victim,
            (float) getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE),
            config.turning().vampireMobTurnChance(),
            MobSounds.VAMPIRE_BITE,
            config.turning());
    if (bitten) {
      onAttacking(victim);
      if (Prey.hasDrinkableBlood(victim)) {
        blood.drink(this, victim, BloodValue.taken(before, victim.getHealth()), config);
      }
    }
    return bitten;
  }

  @Override
  public void tick() {
    super.tick();
    if (!getWorld().isClient() && isAlive()) {
      blood.tick(this, ServerConfigService.current());
      if (isSleeping() && !homeLife.bedtime(this) && !CoffinRise.rising(this)) {
        wakeUp();
      }
    }
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    if (undressed && getWorld() instanceof ServerWorld world) {
      undressed = false;
      for (final EquipmentSlot slot : EquipmentSlot.values()) {
        if (slot.isArmorSlot()) {
          equipStack(slot, ItemStack.EMPTY);
        }
      }
      dress(world);
    }
    final NightbornConfig config = ServerConfigService.current();
    homeLife.tick(this, blood.hungry(this, config), config, this::setOutToFeed);
    final LivingEntity target = getTarget();
    if (target != null
        && (Downed.state(this) != null
            || Hunting.dropsTarget(
                pursuit(target, target == getAttacker() || !Prey.isPrey(target))))) {
      setTarget(null);
    }
    final boolean nowWarded = Hissing.warded(Weakened.sources(this));
    if (nowWarded && !warded) {
      hiss();
    }
    warded = nowWarded;
    levitateAboutHome();
  }

  private void setOutToFeed() {
    if (getWorld() instanceof ServerWorld world) {
      BatEscort.gather(world, this, ServerConfigService.current().mobs().batEscortChance());
    }
  }

  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    final boolean noticed = target != null && getTarget() == null;
    super.setTarget(target);
    if (noticed) {
      hiss();
    }
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof LivingEntity attacker) {
      hiss();
      alertHousemates(attacker);
    }
    return hurt;
  }

  private void alertHousemates(final LivingEntity attacker) {
    final int range = ServerConfigService.current().mobs().vampireAlertRange();
    final boolean attackerIsHousemate =
        attacker instanceof VampireEntity other
            && Housemates.share(homeLife.home(), other.homeLife.home());
    for (final VampireEntity mate :
        getWorld()
            .getEntitiesByClass(
                VampireEntity.class,
                getBoundingBox().expand(range),
                mate -> mate != this && mate != attacker && !mate.isSleeping())) {
      if (Housemates.alerted(
          Housemates.share(homeLife.home(), mate.homeLife.home()),
          attackerIsHousemate,
          mate.squaredDistanceTo(this),
          range)) {
        mate.setAttacker(attacker);
      }
    }
  }

  @Override
  public void remove(final RemovalReason reason) {
    if (reason.shouldDestroy() && getWorld() instanceof ServerWorld world) {
      homeLife.release(world, this);
    }
    super.remove(reason);
  }

  @Override
  protected boolean isImmobile() {
    return super.isImmobile() || isSleeping();
  }

  @Override
  public boolean cannotDespawn() {
    return homeLife.home().isPresent() || super.cannotDespawn();
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return homeLife.home().isEmpty() && super.canImmediatelyDespawn(distanceSquared);
  }

  @Override
  protected boolean isDisallowedInPeaceful() {
    return homeLife.home().isEmpty();
  }

  public void warnOff() {
    hiss();
  }

  void hiss() {
    if (getWorld().isClient()
        || !isAlive()
        || Downed.state(this) != null
        || CoffinRise.pose(this).isPresent()) {
      return;
    }
    final long now = getWorld().getTime();
    if (!Hissing.ready(now, nextHiss)) {
      return;
    }
    nextHiss = Hissing.nextAllowed(now, ServerConfigService.current().mobs().vampireHissSeconds());
    triggerAnim(HISS_CONTROLLER, HISS_CLIP);
    playSound(MobSounds.vampire(rig(), VampireVoice.HISS), getSoundVolume(), getSoundPitch());
  }

  private void levitateAboutHome() {
    final boolean levitating = Levitation.isLevitating(this);
    if (MobLevitation.wants(
            levitating,
            getTarget() != null,
            random.nextFloat(),
            ServerConfigService.current().mobs())
        == levitating) {
      return;
    }
    if (levitating) {
      Levitation.setDown(this);
    } else {
      Levitation.start(this);
    }
  }

  @Override
  public EntityData initialize(
      final ServerWorldAccess world,
      final LocalDifficulty difficulty,
      final SpawnReason spawnReason,
      @Nullable final EntityData entityData) {
    final NightbornConfig config = ServerConfigService.current();
    dress(world);
    VampireCore.tick(this, config.core(), config.blood());
    setHealth(getMaxHealth());
    blood.start(this, random.nextInt(Integer.MAX_VALUE), config);
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  @Override
  protected void initEquipment(final Random random, final LocalDifficulty difficulty) {}

  private void dress(final ServerWorldAccess world) {
    final var registries = world.getRegistryManager();
    final var outfits = VampireLooks.outfits(registries);
    Appearance.pick(
            VampireLooks.looks(registries),
            outfits,
            VampireLooks.builds(registries),
            random::nextDouble)
        .ifPresent(
            appearance -> {
              setAppearance(appearance.look(), appearance.height(), appearance.width());
              appearance
                  .outfit()
                  .map(outfits::get)
                  .map(outfit -> OutfitDressing.stacks(outfit, VampireLooks::item))
                  .ifPresent(stacks -> stacks.forEach(this::equipStack));
            });
  }

  public void setAppearance(final Identifier look, final float height, final float width) {
    dataTracker.set(LOOK, look.toString());
    dataTracker.set(HEIGHT, VampireSize.clampHeight(height));
    dataTracker.set(WIDTH, VampireSize.clampWidth(width));
  }

  public Identifier lookId() {
    final Identifier id = Identifier.tryParse(dataTracker.get(LOOK));
    return id == null ? VampireLooks.NONE : id;
  }

  public Optional<VampireLook> look() {
    return VampireLooks.look(getWorld().getRegistryManager(), lookId());
  }

  public VampireRig rig() {
    return look().map(VampireLook::rig).orElse(VampireRig.MALE);
  }

  public float height() {
    return dataTracker.get(HEIGHT);
  }

  public float width() {
    return dataTracker.get(WIDTH);
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putString(LOOK_KEY, dataTracker.get(LOOK));
    nbt.putFloat(HEIGHT_KEY, height());
    nbt.putFloat(WIDTH_KEY, width());
    homeLife.write(nbt);
    blood.write(nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    homeLife.read(nbt);
    blood.read(nbt);
    undressed = nbt.contains(LOOK_KEY) && !nbt.contains(LOOK_KEY, NbtElement.STRING_TYPE);
    if (nbt.contains(LOOK_KEY, NbtElement.STRING_TYPE)) {
      final Identifier look = Identifier.tryParse(nbt.getString(LOOK_KEY));
      setAppearance(
          look == null ? VampireLooks.NONE : look,
          nbt.contains(HEIGHT_KEY) ? nbt.getFloat(HEIGHT_KEY) : VampireSize.BASE_HEIGHT,
          nbt.contains(WIDTH_KEY) ? nbt.getFloat(WIDTH_KEY) : 1.0F);
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return MobSounds.vampire(rig(), VampireVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.vampire(rig(), VampireVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.vampire(rig(), VampireVoice.DEATH);
  }

  @Override
  public float getSoundPitch() {
    return super.getSoundPitch() * VampireVoice.pitch(height());
  }

  @Override
  public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
    controllers.add(
        new AnimationController<>(
            this,
            "pose",
            ANIMATION_BLEND_TICKS,
            state -> {
              final boolean levitating = Levitation.isLevitating(this);
              landingEdge.ended(levitating);
              final VampirePose pose =
                  VampirePose.of(
                      new VampirePose.Cue(
                          CoffinRise.pose(this),
                          VisibleStateAttachment.get(this).downed(),
                          levitating,
                          landingEdge.ending(),
                          isSprinting(),
                          state.getLimbSwingAmount(),
                          Math.hypot(getX() - prevX, getZ() - prevZ)));
              shownPose = pose == VampirePose.IDLE ? IdleFidget.pose(getId(), age) : pose;
              final PlayState play = state.setAndContinue(shownPose.clip());
              landingEdge.shown(
                  pose == VampirePose.LAND,
                  pose == VampirePose.LAND && state.getController().hasAnimationFinished());
              return play;
            }));
    controllers.add(
        new AnimationController<>(
            this,
            "action",
            ANIMATION_BLEND_TICKS,
            state ->
                VampirePose.playOrStop(
                    state,
                    shownPose.allowsActions()
                        ? VampirePose.action(handSwinging, false, true)
                        : Optional.empty())));
    controllers.add(
        new AnimationController<>(this, HISS_CONTROLLER, 2, state -> PlayState.STOP)
            .triggerableAnim(HISS_CLIP, HISS));
  }

  public VampirePose shownPose() {
    return shownPose;
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
