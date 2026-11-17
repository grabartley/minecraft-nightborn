package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.mob.MobSounds;
import com.grahambartley.nightborn.mob.look.BustNbt;
import com.grahambartley.nightborn.mob.look.Figure;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.name.NameStyle;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.Entity;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
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
    implements GeoEntity, HunterLooking, NameBearer, RangedAttackMob {

  static final String FEMALE_KEY = "Female";
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
  private static final TrackedData<Boolean> ARMED =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

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
    builder.add(ARMED, false);
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new CrossbowGoal(this, this::standsOff));
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
        rig(), dataTracker.get(LOOK), look -> GuildData.texture(getRegistryManager(), look, rig()));
  }

  @Override
  public void shootAt(final LivingEntity target, final float pullProgress) {
    tactics.shoot();
  }

  public String look() {
    return dataTracker.get(LOOK);
  }

  public String kit() {
    return kitId;
  }

  public boolean standsOff() {
    return kit != null && kit.ranged();
  }

  public boolean armed() {
    return dataTracker.get(ARMED);
  }

  void setArmed(final boolean armed) {
    dataTracker.set(ARMED, armed);
  }

  void speak(final HunterVoice voice) {
    playSound(MobSounds.hunter(figure(), voice), getSoundVolume(), getSoundPitch());
  }

  @Override
  public boolean tryAttack(final Entity target) {
    final boolean struck = super.tryAttack(target);
    if (struck) {
      speak(HunterVoice.STRIKE);
    }
    return struck;
  }

  @Override
  public boolean onKilledOther(final ServerWorld world, final LivingEntity other) {
    if (Vampires.isVampire(other)) {
      speak(HunterVoice.CHEER);
    }
    return super.onKilledOther(world, other);
  }

  void show(final HunterAction action) {
    dataTracker.set(ACTION, action.ordinal());
  }

  public HunterAction action() {
    return HunterAction.byIndex(dataTracker.get(ACTION));
  }

  @Override
  public NameStyle nameStyle() {
    return NameStyle.HUNTER;
  }

  @Override
  public boolean bearsWomansName() {
    return dataTracker.get(FEMALE);
  }

  @Override
  public float bust() {
    return dataTracker.get(BUST);
  }

  @Override
  public void tick() {
    super.tick();
    if (getWorld().isClient()) {
      return;
    }
    if (!hasPositionTarget()) {
      settle();
    }
    MobNaming.ensure(this);
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
    dataTracker.set(LOOK, GuildData.wornLook(look, getRegistryManager(), rig(), getRandom()));
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
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return MobSounds.hunter(figure(), HunterVoice.AMBIENT);
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return MobSounds.hunter(figure(), HunterVoice.HURT);
  }

  @Override
  protected SoundEvent getDeathSound() {
    return MobSounds.hunter(figure(), HunterVoice.DEATH);
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
    nbt.putString(GuildData.LOOK_KEY, dataTracker.get(LOOK));
    nbt.putString(KIT_KEY, kitId);
    CampHome.write(this, nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(FEMALE, nbt.getBoolean(FEMALE_KEY));
    dataTracker.set(BUST, BustNbt.read(nbt, figure()));
    if (!getWorld().isClient()) {
      equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
      equipStack(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
      wear(nbt.getString(GuildData.LOOK_KEY));
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
                            state.getLimbSwingAmount(),
                            armed())
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
