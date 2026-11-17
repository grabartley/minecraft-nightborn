package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.eldest.EldestFoes;
import com.grahambartley.nightborn.eldest.EldestHealth;
import com.grahambartley.nightborn.eldest.FightRoll;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EldestEntity extends BossVampireEntity {

  public static final Identifier STAGE = Identifier.of(Nightborn.MOD_ID, "eldest");
  public static final int ROOM_RADIUS = 6;

  private final FightRoll fightRoll = new FightRoll();

  public EldestEntity(final EntityType<? extends EldestEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 100;
  }

  @Override
  public Set<VampireRig> rigs() {
    return VampireRig.ELDEST;
  }

  @Override
  public void settleAt(final BlockPos home) {
    super.settleAt(home);
    homeLife().keepTo(getBlockPos(), ROOM_RADIUS);
    leaveRoutine();
  }

  private void leaveRoutine() {
    if (homeLife().room().isPresent()) {
      goalSelector.clear(goal -> goal instanceof HouseholdLifeGoal);
    }
  }

  @Override
  public void setTarget(@Nullable final LivingEntity target) {
    final boolean rescales =
        EldestHealth.rescales(
            homeLife().room().isPresent(),
            getTarget() != null,
            target != null,
            getHealth() >= getMaxHealth());
    super.setTarget(target);
    if (rescales && !getWorld().isClient()) {
      final RitualSettings settings = ServerConfigService.current().ritual();
      scaleFor(
          getWorld()
              .getEntitiesByClass(
                  PlayerEntity.class,
                  getBoundingBox().expand(settings.fightRange()),
                  player -> player.isAlive() && !player.isSpectator())
              .size(),
          settings);
    }
  }

  @Override
  protected void initGoals() {
    super.initGoals();
    targetSelector.add(
        2,
        new ActiveTargetGoal<>(
            this,
            PlayerEntity.class,
            10,
            true,
            false,
            player ->
                EldestFoes.seeksOut(
                    Vampires.isVampire(player),
                    getWorld().getDifficulty() == Difficulty.PEACEFUL)));
  }

  @Override
  public EntityData initialize(
      final ServerWorldAccess world,
      final LocalDifficulty difficulty,
      final SpawnReason spawnReason,
      @Nullable final EntityData entityData) {
    return super.initialize(world, difficulty, spawnReason, new HouseholdArrival(Map.of(STAGE, 1)));
  }

  public void scaleFor(final int players, final RitualSettings settings) {
    EldestHealth.scale(this, players, settings.eldestHealthPerPlayer());
  }

  public Set<UUID> fighters() {
    return fightRoll.fighters();
  }

  @Override
  protected boolean fights(final LivingEntity target) {
    return EldestFoes.fights(
        Vampires.isVampire(target),
        target instanceof PlayerEntity,
        fightRoll.has(target.getUuid()));
  }

  @Override
  public boolean damage(final DamageSource source, final float amount) {
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof PlayerEntity player) {
      fightRoll.join(player.getUuid());
    }
    return hurt;
  }

  @Override
  public boolean cannotDespawn() {
    return true;
  }

  @Override
  protected boolean isDisallowedInPeaceful() {
    return false;
  }

  @Override
  public void writeCustomDataToNbt(final NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    fightRoll.write(nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    fightRoll.read(nbt);
    leaveRoutine();
  }
}
