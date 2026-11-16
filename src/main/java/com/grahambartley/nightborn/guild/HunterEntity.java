package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.util.GeckoLibUtil;

public class HunterEntity extends PathAwareEntity implements GeoEntity, HunterLooking {

  static final String FEMALE_KEY = "Female";
  static final int ANIMATION_BLEND_TICKS = 4;

  private static final TrackedData<Boolean> FEMALE =
      DataTracker.registerData(HunterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

  private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

  public HunterEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    setEquipmentDropChance(EquipmentSlot.MAINHAND, 0.0F);
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
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(2, new MeleeAttackGoal(this, 1.15, true));
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
  public EntityData initialize(
      final ServerWorldAccess world,
      final LocalDifficulty difficulty,
      final SpawnReason reason,
      @Nullable final EntityData data) {
    dataTracker.set(FEMALE, world.getRandom().nextBoolean());
    initEquipment(world.getRandom(), difficulty);
    return super.initialize(world, difficulty, reason, data);
  }

  @Override
  protected void initEquipment(final Random random, final LocalDifficulty difficulty) {
    equipStack(EquipmentSlot.MAINHAND, new ItemStack(GearItems.WOODEN_STAKE));
  }

  @Override
  public HunterLook look() {
    return HunterLook.hunter(dataTracker.get(FEMALE));
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putBoolean(FEMALE_KEY, dataTracker.get(FEMALE));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(FEMALE, nbt.getBoolean(FEMALE_KEY));
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
                            getVelocity().horizontalLengthSquared())
                        .clip())));
  }

  @Override
  public AnimatableInstanceCache getAnimatableInstanceCache() {
    return cache;
  }
}
