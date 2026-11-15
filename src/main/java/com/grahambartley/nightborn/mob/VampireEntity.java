package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.coffin.CoffinRise;
import com.grahambartley.nightborn.config.NightbornConfig;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.mob.goal.FleeWhileEscapingGoal;
import com.grahambartley.nightborn.mob.goal.LieDownedGoal;
import com.grahambartley.nightborn.mob.goal.SeekShadeGoal;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.VisibleStateAttachment;
import java.util.Optional;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AvoidSunlightGoal;
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
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class VampireEntity extends HostileEntity implements GeoEntity {

  static final String LOOK_KEY = "Look";
  static final int ANIMATION_BLEND_TICKS = 4;

  private static final TrackedData<Integer> LOOK =
      DataTracker.registerData(VampireEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final EndingEdge landingEdge = new EndingEdge();
  private VampirePose shownPose = VampirePose.IDLE;

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
    builder.add(LOOK, 0);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new LieDownedGoal(this));
    goalSelector.add(1, new SwimGoal(this));
    goalSelector.add(1, new FleeWhileEscapingGoal(this, 1.4));
    goalSelector.add(2, new SeekShadeGoal(this, 1.3));
    goalSelector.add(3, new AvoidSunlightGoal(this));
    goalSelector.add(4, new MeleeAttackGoal(this, 1.15, false));
    goalSelector.add(6, new WanderAroundFarGoal(this, 0.8));
    goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    goalSelector.add(7, new LookAroundGoal(this));
    targetSelector.add(1, new RevengeGoal(this));
    targetSelector.add(
        2, new ActiveTargetGoal<>(this, LivingEntity.class, 10, true, false, this::hunts));
  }

  public boolean hunts(final LivingEntity target) {
    return Downed.state(this) == null
        && Hunting.vampireHunts(getWorld())
        && !ShadeRules.endangered(this)
        && Prey.isPrey(target);
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
    final NightbornConfig config = ServerConfigService.current();
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
    }
    return bitten;
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    final LivingEntity target = getTarget();
    if (target != null
        && (Downed.state(this) != null
            || Hunting.dropsTarget(
                Hunting.vampireHunts(getWorld()),
                ShadeRules.endangered(this),
                target == getAttacker() || !Prey.isPrey(target)))) {
      setTarget(null);
    }
    levitateAboutHome();
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
    setLook(random.nextInt(VampireLook.COUNT));
    VampireCore.tick(this, config.core(), config.blood());
    setHealth(getMaxHealth());
    if (spawnReason == SpawnReason.NATURAL) {
      BatEscort.gather(world, this, config.mobs().batEscortChance());
    }
    return super.initialize(world, difficulty, spawnReason, entityData);
  }

  public VampireLook look() {
    return VampireLook.of(dataTracker.get(LOOK));
  }

  public void setLook(final int index) {
    dataTracker.set(LOOK, VampireLook.of(index).index());
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putInt(LOOK_KEY, dataTracker.get(LOOK));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    if (nbt.contains(LOOK_KEY)) {
      setLook(nbt.getInt(LOOK_KEY));
    }
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.VAMPIRE_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.VAMPIRE_DEATH;
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
              shownPose = pose;
              final PlayState play = state.setAndContinue(pose.clip());
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
  }

  public VampirePose shownPose() {
    return shownPose;
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
