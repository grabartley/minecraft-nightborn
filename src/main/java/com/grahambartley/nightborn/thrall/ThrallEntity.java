package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.thrall.goal.ThrallFollowGoal;
import com.grahambartley.nightborn.thrall.goal.ThrallPostGoal;
import com.grahambartley.nightborn.thrall.goal.ThrallTargetGoal;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
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
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.VillagerData;
import net.minecraft.village.VillagerDataContainer;
import net.minecraft.village.VillagerProfession;
import net.minecraft.village.VillagerType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ThrallEntity extends PathAwareEntity implements VillagerDataContainer {

  static final int CHECK_BOND_TICKS = 20;

  private static final TrackedData<VillagerData> VILLAGER_DATA =
      DataTracker.registerData(ThrallEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);
  private static final TrackedData<Optional<UUID>> OWNER_UUID =
      DataTracker.registerData(ThrallEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);

  private static final String OWNER = "Owner";
  private static final String MODE = "Mode";
  private static final String POST = "Post";
  private static final String VILLAGER = "VillagerData";
  private static final String MEMORY = "VillagerMemory";

  private ThrallMode mode = ThrallMode.FOLLOW;
  @Nullable private BlockPos post;
  private VillagerMemory memory = VillagerMemory.NONE;

  public ThrallEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    setCanPickUpLoot(false);
  }

  public static DefaultAttributeContainer.Builder createThrallAttributes() {
    return MobEntity.createMobAttributes()
        .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
        .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
        .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0)
        .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0);
  }

  @Override
  protected void initDataTracker(final DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(VILLAGER_DATA, new VillagerData(VillagerType.PLAINS, VillagerProfession.NONE, 1));
    builder.add(OWNER_UUID, Optional.empty());
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(1, new MeleeAttackGoal(this, 1.0, true));
    goalSelector.add(2, new ThrallPostGoal(this));
    goalSelector.add(3, new ThrallFollowGoal(this));
    goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
    goalSelector.add(8, new LookAroundGoal(this));
    targetSelector.add(1, new ThrallTargetGoal(this));
  }

  public void bind(final UUID boundTo, final VillagerData data, final VillagerMemory kept) {
    dataTracker.set(OWNER_UUID, Optional.of(boundTo));
    setVillagerData(data);
    memory = kept;
    mode = ThrallMode.FOLLOW;
    post = getBlockPos();
    setPersistent();
  }

  @Override
  protected ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    return isAlive() ? ThrallOrders.interact(this, player, hand) : ActionResult.PASS;
  }

  @Override
  public void tick() {
    super.tick();
    if (age % CHECK_BOND_TICKS == 0 && isAlive() && getWorld() instanceof ServerWorld world) {
      Thralls.checkBond(world, this);
    }
  }

  @Override
  public void onDeath(final DamageSource source) {
    super.onDeath(source);
    if (getWorld() instanceof ServerWorld world) {
      owner().ifPresent(id -> ThrallRegistry.of(world.getServer()).free(id, getUuid()));
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.ENTITY_VILLAGER_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(final DamageSource source) {
    return SoundEvents.ENTITY_VILLAGER_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_VILLAGER_DEATH;
  }

  @Override
  public float getSoundPitch() {
    return super.getSoundPitch() * 0.8F;
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  public boolean canImmediatelyDespawn(final double distanceSquared) {
    return false;
  }

  @Override
  public VillagerData getVillagerData() {
    return dataTracker.get(VILLAGER_DATA);
  }

  @Override
  public void setVillagerData(final VillagerData data) {
    dataTracker.set(VILLAGER_DATA, data);
  }

  public Optional<UUID> owner() {
    return dataTracker.get(OWNER_UUID);
  }

  public boolean isOwnedBy(final LivingEntity entity) {
    return owner().filter(entity.getUuid()::equals).isPresent();
  }

  @Nullable
  public LivingEntity ownerHere() {
    return owner()
        .filter(id -> getWorld() instanceof ServerWorld)
        .map(id -> ((ServerWorld) getWorld()).getEntity(id))
        .filter(entity -> entity instanceof LivingEntity living && living.isAlive())
        .map(LivingEntity.class::cast)
        .orElse(null);
  }

  public VillagerMemory memory() {
    return memory;
  }

  public ThrallMode mode() {
    return mode;
  }

  public BlockPos post() {
    return post == null ? getBlockPos() : post;
  }

  public void order(final ThrallMode next) {
    mode = next;
    post = getBlockPos().toImmutable();
    getNavigation().stop();
  }

  public boolean servesSameOwner(final LivingEntity other) {
    return other instanceof ThrallEntity thrall
        && owner().isPresent()
        && owner().equals(thrall.owner());
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    owner().ifPresent(id -> nbt.putUuid(OWNER, id));
    nbt.putString(MODE, mode.name());
    if (post != null) {
      nbt.put(POST, NbtHelper.fromBlockPos(post));
    }
    VillagerData.CODEC
        .encodeStart(NbtOps.INSTANCE, getVillagerData())
        .result()
        .ifPresent(encoded -> nbt.put(VILLAGER, encoded));
    nbt.put(MEMORY, memory.write(new NbtCompound(), getRegistryManager().getOps(NbtOps.INSTANCE)));
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    dataTracker.set(
        OWNER_UUID, nbt.containsUuid(OWNER) ? Optional.of(nbt.getUuid(OWNER)) : Optional.empty());
    mode = ThrallMode.byName(nbt.getString(MODE));
    post = NbtHelper.toBlockPos(nbt, POST).orElse(null);
    if (nbt.contains(VILLAGER)) {
      VillagerData.CODEC
          .parse(NbtOps.INSTANCE, nbt.get(VILLAGER))
          .result()
          .ifPresent(this::setVillagerData);
    }
    memory =
        VillagerMemory.read(nbt.getCompound(MEMORY), getRegistryManager().getOps(NbtOps.INSTANCE));
  }
}
