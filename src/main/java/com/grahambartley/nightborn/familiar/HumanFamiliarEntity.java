package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.familiar.goal.BetrayOwnerGoal;
import com.grahambartley.nightborn.familiar.goal.GuardCoffinGoal;
import com.grahambartley.nightborn.familiar.goal.RetaliateGoal;
import com.grahambartley.nightborn.familiar.goal.RunErrandGoal;
import com.grahambartley.nightborn.familiar.goal.StayHomeGoal;
import com.grahambartley.nightborn.name.MobNaming;
import com.grahambartley.nightborn.name.NameBearer;
import com.grahambartley.nightborn.servant.ServantDeaths;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.InventoryOwner;
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
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
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

public class HumanFamiliarEntity extends PathAwareEntity
    implements VillagerDataContainer, InventoryOwner, NameBearer {

  public static final int CARRY_SLOTS = 8;

  private static final TrackedData<VillagerData> VILLAGER_DATA =
      DataTracker.registerData(HumanFamiliarEntity.class, TrackedDataHandlerRegistry.VILLAGER_DATA);

  private static final String OWNER = "Owner";
  private static final String LOYALTY = "Loyalty";
  private static final String PAID_THROUGH = "PaidThroughDay";
  private static final String LAST_DAY = "LastDay";
  private static final String TURNED = "Turned";
  private static final String HOME = "Home";
  private static final String VILLAGER = "VillagerData";

  private final SimpleInventory carried = new SimpleInventory(CARRY_SLOTS);
  @Nullable private UUID owner;
  private int loyalty = Loyalty.MAX;
  private long paidThroughDay;
  private long lastDay = -1L;
  private boolean turned;
  @Nullable private BlockPos home;
  private int lastSlightedAge = Integer.MIN_VALUE;

  public HumanFamiliarEntity(final EntityType<? extends PathAwareEntity> type, final World world) {
    super(type, world);
    setCanPickUpLoot(false);
  }

  public static DefaultAttributeContainer.Builder createFamiliarAttributes() {
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
  }

  @Override
  protected void initGoals() {
    goalSelector.add(0, new SwimGoal(this));
    goalSelector.add(2, new MeleeAttackGoal(this, 1.0, true));
    goalSelector.add(3, new RunErrandGoal(this));
    goalSelector.add(4, new StayHomeGoal(this));
    goalSelector.add(6, new WanderAroundFarGoal(this, 0.6));
    goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
    goalSelector.add(8, new LookAroundGoal(this));
    targetSelector.add(1, new RetaliateGoal(this));
    targetSelector.add(2, new BetrayOwnerGoal(this));
    targetSelector.add(3, new GuardCoffinGoal(this));
  }

  public void hire(
      final UUID hiredBy,
      final VillagerData data,
      final long today,
      final int startLoyalty,
      final BlockPos hiredAt) {
    owner = hiredBy;
    setVillagerData(data.withProfession(VillagerProfession.NONE).withLevel(1));
    loyalty = Loyalty.clamp(startLoyalty);
    paidThroughDay = today;
    lastDay = today;
    turned = false;
    home = hiredAt;
    setPersistent();
  }

  @Override
  protected ActionResult interactMob(final PlayerEntity player, final Hand hand) {
    if (getWorld() instanceof ServerWorld && isAlive()) {
      return FamiliarCare.interact(this, player, hand);
    }
    return ActionResult.PASS;
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    final boolean hurt = super.damage(source, amount);
    if (hurt && getWorld() instanceof ServerWorld) {
      FamiliarCare.slighted(this, source);
    }
    return hurt;
  }

  @Override
  public void tick() {
    super.tick();
    MobNaming.ensure(this);
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    if (age % 20 == 0) {
      FamiliarUpkeep.check(this);
    }
  }

  @Override
  public void onDeath(final DamageSource source) {
    super.onDeath(source);
    if (owner != null && getWorld() instanceof ServerWorld world) {
      FamiliarRegistry.of(world.getServer()).free(owner, getUuid());
      ServantDeaths.tell(this, owner());
    }
  }

  @Override
  protected void dropInventory() {
    super.dropInventory();
    for (final ItemStack stack : carried.clearToList()) {
      dropStack(stack);
    }
  }

  @Override
  public boolean bearsWomansName() {
    return false;
  }

  @Override
  public Optional<UUID> namedBy() {
    return owner();
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

  @Override
  public SimpleInventory getInventory() {
    return carried;
  }

  public Optional<UUID> owner() {
    return Optional.ofNullable(owner);
  }

  public boolean isOwnedBy(final PlayerEntity player) {
    return player.getUuid().equals(owner);
  }

  public int loyalty() {
    return loyalty;
  }

  public void changeLoyalty(final int delta) {
    loyalty = Loyalty.change(loyalty, delta);
  }

  public void loyalty(final int value) {
    loyalty = Loyalty.clamp(value);
  }

  public long paidThroughDay() {
    return paidThroughDay;
  }

  public void paidThroughDay(final long day) {
    paidThroughDay = day;
  }

  public long lastDay() {
    return lastDay;
  }

  public void lastDay(final long day) {
    lastDay = day;
  }

  public boolean turned() {
    return turned;
  }

  public void turn() {
    turned = true;
  }

  public Optional<BlockPos> home() {
    return Optional.ofNullable(home);
  }

  public void home(final BlockPos pos) {
    home = pos.toImmutable();
  }

  long ticksSinceSlighted() {
    return (long) age - lastSlightedAge;
  }

  void markSlighted() {
    lastSlightedAge = age;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    if (owner != null) {
      nbt.putUuid(OWNER, owner);
    }
    nbt.putInt(LOYALTY, loyalty);
    nbt.putLong(PAID_THROUGH, paidThroughDay);
    nbt.putLong(LAST_DAY, lastDay);
    nbt.putBoolean(TURNED, turned);
    if (home != null) {
      nbt.put(HOME, NbtHelper.fromBlockPos(home));
    }
    VillagerData.CODEC
        .encodeStart(NbtOps.INSTANCE, getVillagerData())
        .result()
        .ifPresent(encoded -> nbt.put(VILLAGER, encoded));
    writeInventory(nbt, getRegistryManager());
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    owner = nbt.containsUuid(OWNER) ? nbt.getUuid(OWNER) : null;
    loyalty = nbt.contains(LOYALTY) ? Loyalty.clamp(nbt.getInt(LOYALTY)) : loyalty;
    paidThroughDay = nbt.getLong(PAID_THROUGH);
    lastDay = nbt.contains(LAST_DAY) ? nbt.getLong(LAST_DAY) : -1L;
    turned = nbt.getBoolean(TURNED);
    home = NbtHelper.toBlockPos(nbt, HOME).orElse(null);
    if (nbt.contains(VILLAGER)) {
      VillagerData.CODEC
          .parse(NbtOps.INSTANCE, nbt.get(VILLAGER))
          .result()
          .ifPresent(this::setVillagerData);
    }
    readInventory(nbt, getRegistryManager());
  }
}
