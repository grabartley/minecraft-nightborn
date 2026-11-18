package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.servant.Bindable;
import com.grahambartley.nightborn.servant.goal.KeepCloseGoal;
import com.grahambartley.nightborn.servant.goal.MastersFoeGoal;
import com.grahambartley.nightborn.smoke.SmokePoof;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class RestlessDeadEntity extends HostileEntity implements Bindable {

  static final String MASTER = "Master";
  static final String TICKS_LEFT = "TicksLeft";
  static final double KEEP_CLOSE = 12.0;
  static final double CLOSE_ENOUGH = 4.0;

  private Optional<UUID> master = Optional.empty();
  private int ticksLeft;

  public RestlessDeadEntity(final EntityType<? extends HostileEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 0;
  }

  public static DefaultAttributeContainer.Builder createRestlessAttributes() {
    return HostileEntity.createHostileAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
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
    if (getWorld() instanceof ServerWorld && expires(master.isPresent(), --ticksLeft)) {
      SmokePoof.burst(this, SoundEvents.ENTITY_ZOMBIE_DEATH);
      discard();
    }
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
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    master = nbt.containsUuid(MASTER) ? Optional.of(nbt.getUuid(MASTER)) : Optional.empty();
    ticksLeft = nbt.getInt(TICKS_LEFT);
  }
}
