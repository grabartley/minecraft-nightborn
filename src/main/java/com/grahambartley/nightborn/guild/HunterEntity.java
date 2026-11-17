package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.mob.look.BustNbt;
import com.grahambartley.nightborn.mob.look.Figure;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.mob.look.VampireVoice;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.GoToWalkTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HunterEntity extends PathAwareEntity
    implements GeoEntity, HunterLooking, RangedAttackMob {

  static final String FEMALE_KEY = "Female";
  static final String LOOK_KEY = "Look";
  static final String KIT_KEY = "Kit";
  static final int ANIMATION_BLEND_TICKS = 4;
  static final int CAMP_REACH = 12;

  private static final TrackedData<Boolean> FEMALE =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
  private static final TrackedData<Float> BUST =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.FLOAT);
  private static final TrackedData<String> LOOK =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.STRING);
  private static final TrackedData<Integer> ACTION =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.INTEGER);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
  private final HunterTactics tactics = new HunterTactics(this);
  private final WornLook worn = new WornLook();
  private String kitId = "";
  private HunterKit kit;
  private HunterAction shown = HunterAction.NONE;

  public HunterEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
    setEquipmentDropChance(EquipmentSlot.OFFHAND, 0.0F);
  }

  public static DefaultAttributeContainer.Builder createHunterAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0)
        .add(EntityAttributes.GENERIC_ARMOR, 2.0)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(FEMALE, false);
    builder.add(BUST, Figure.FLAT);
    builder.add(LOOK, "");
    builder.add(ACTION, HunterAction.NONE.ordinal());
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new CrossbowGoal(this, () -> kit != null && kit.ranged()));
    goalSelector.add(2, new MeleeAttackGoal(this, 1.15, true));
    goalSelector.add(4, new GoToWalkTargetGoal(this, 0.8));
    goalSelector.add(5, new WanderAroundFarGoal(this, 0.6));
    goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
    goalSelector.add(7, new LookAroundGoal(this));
    targetSelector.add(
        1, new ActiveTargetGoal<>(this, LivingEntity.class, true, HunterEntity::hunts));
  }

  static boolean hunts(final LivingEntity target) {
    return HunterTargets.hunts(
        Vampires.isVampire(target), target instanceof PlayerEntity player && player.isCreative());
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return hunts(target) && super.canTarget(target);
  }

  @Override
  public HunterRig rig() {
    return HunterRig.hunter(dataTracker.get(FEMALE));
  }

  @Override
  public Identifier texture() {
    return worn.texture(
        dataTracker.get(LOOK), look -> GuildData.texture(getRegistryManager(), look, rig()));
  }

  @Override
  public void shootAt(final LivingEntity target, final float pullProgress) {
    tactics.shoot();
  }

  void show(final HunterAction action) {
    dataTracker.set(ACTION, action.ordinal());
  }

  public HunterAction action() {
    return HunterAction.byIndex(dataTracker.get(ACTION));
  }

  @Override
  public float bust() {
    return dataTracker.get(BUST);
  }

  @Override
  public void tick() {
    super.tick();
    if (!getWorld().isClient() && !hasPositionTarget()) {
      settle();
    }
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    fight();
  }

  public void fight() {
    if (kit != null) {
      tactics.tick(kit);
    }
  }

  private void settle() {
    CampHome.keep(this, CAMP_REACH);
    dataTracker.set(FEMALE, getRandom().nextBoolean());
    dataTracker.set(BUST, figure().bust(getRandom().nextDouble()));
    wear("");
    arm("");
  }

  private void wear(final String look) {
    dataTracker.set(
        LOOK, look.isEmpty() ? GuildData.pickLook(getRegistryManager(), rig(), getRandom()) : look);
  }

  public void arm(final String wanted) {
    final var kits = GuildData.kits(getRegistryManager());
    kitId =
        Optional.ofNullable(Identifier.tryParse(wanted))
            .filter(kits::containsKey)
            .or(() -> HunterKit.pick(kits, getRandom().nextDouble()))
            .map(Identifier::toString)
            .orElse("");
    kit = kits.get(Identifier.tryParse(kitId));
    if (kit != null) {
      tactics.equip(kit);
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return MobSounds.hunter(figure(), VampireVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.hunter(figure(), VampireVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.hunter(figure(), VampireVoice.DEATH);
  }

  private VampireRig figure() {
    return rig().figure();
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putBoolean(FEMALE_KEY, dataTracker.get(FEMALE));
    BustNbt.write(nbt, bust());
    nbt.putString(LOOK_KEY, dataTracker.get(LOOK));
    nbt.putString(KIT_KEY, kitId);
    CampHome.write(this, nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(FEMALE, nbt.getBoolean(FEMALE_KEY));
    dataTracker.set(BUST, BustNbt.read(nbt, figure()));
    if (!getWorld().isClient()) {
      wear(nbt.getString(LOOK_KEY));
      arm(nbt.getString(KIT_KEY));
    }
    CampHome.read(this, nbt, CAMP_REACH);
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
                    HunterPose.of(
                            handSwinging,
                            false,
                            isSprinting() || isAttacking(),
                            state.getLimbSwingAmount())
                        .clip())));
    controllers.add(new AnimationController<>(this, "action", ANIMATION_BLEND_TICKS, this::act));
  }

  private PlayState act(final AnimationState<HunterEntity> state) {
    final HunterAction action = action();
    if (action != shown) {
      shown = action;
      state.getController().forceAnimationReset();
    }
    return action == HunterAction.NONE ? PlayState.STOP : state.setAndContinue(action.clip());
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
