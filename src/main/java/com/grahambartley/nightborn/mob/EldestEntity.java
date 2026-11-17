package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.boss.Arena;
import com.grahambartley.nightborn.boss.BossFight;
import com.grahambartley.nightborn.boss.BossFightPlan;
import com.grahambartley.nightborn.boss.BossFights;
import com.grahambartley.nightborn.boss.BossPhase;
import com.grahambartley.nightborn.boss.FightRules;
import com.grahambartley.nightborn.boss.HoldGroundGoal;
import com.grahambartley.nightborn.boss.PhaseTrack;
import com.grahambartley.nightborn.config.RitualSettings;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.eldest.EldestFoes;
import com.grahambartley.nightborn.eldest.EldestHealth;
import com.grahambartley.nightborn.eldest.FightRoll;
import com.grahambartley.nightborn.lord.BossBarLook;
import com.grahambartley.nightborn.mob.goal.HouseholdLifeGoal;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EldestEntity extends BossVampireEntity {

  public static final Identifier STAGE = Identifier.of(Nightborn.MOD_ID, "eldest");
  public static final Identifier FIGHT = Identifier.of(Nightborn.MOD_ID, "eldest");
  public static final int ROOM_RADIUS = 6;
  public static final TagKey<DamageType> SHRUGS_OFF =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "eldest_shrugs_off"));

  private final FightRoll fightRoll = new FightRoll();
  private final BossFight fight = new BossFight();

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
    fight.claim(
        Arena.room(getBlockPos(), ServerConfigService.current().ritual().roomArenaRadius()));
    leaveRoutine();
  }

  public void claimArena(final Arena arena) {
    fight.claim(arena);
  }

  public BossFight fight() {
    return fight;
  }

  private Optional<BossFightPlan> plan() {
    return BossFights.fight(getWorld().getRegistryManager(), FIGHT);
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    tickFight();
    showBar();
  }

  public void tickFight() {
    if (fight.arena().isEmpty()) {
      fight.claim(new Arena(getBlockPos(), ServerConfigService.current().ritual().arenaRadius()));
    }
    final RitualSettings settings = ServerConfigService.current().ritual();
    fight.tick(
        this,
        plan(),
        new FightRules(
            settings.arenaEmptyTicks(), settings.stakeWindowSeconds() * 20, settings.riseHealth()),
        fightRoll::clear);
  }

  @Override
  protected void showBar() {
    final Optional<BossFightPlan> plan = plan();
    if (plan.isEmpty() || Downed.state(this) != null) {
      super.showBar();
      return;
    }
    final BossPhase phase = plan.get().phase(fight.phase());
    presence()
        .update(
            BossBarLook.phase(phase.color(), phase.translationKey()).title(getDisplayName()),
            phase.color(),
            PhaseTrack.barPercent(
                getHealth(),
                fight.floorHealth(this, plan.get()),
                fight.ceilingHealth(this, plan.get())));
  }

  @Override
  protected boolean barShowsOnSight() {
    return false;
  }

  @Override
  protected boolean preoccupied() {
    return fight.busy(getWorld().getTime());
  }

  @Override
  protected boolean casts(final Identifier power) {
    return fight.casts(plan(), power);
  }

  @Override
  public boolean canTarget(final LivingEntity target) {
    return fight.holds(target.getPos()) && super.canTarget(target);
  }

  @Override
  protected void applyDamage(final DamageSource source, final float amount) {
    super.applyDamage(
        source, fight.cap(this, plan(), NightbornDamage.killsVampires(source), amount));
  }

  @Override
  public void onRemoved() {
    fight.end(this);
    super.onRemoved();
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
    goalSelector.add(0, new HoldGroundGoal(this, this::preoccupied));
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
    if (EldestFoes.shrugsOff(
        fight.changing(getWorld().getTime()),
        source.getAttacker() != null && !fight.holds(source.getAttacker().getPos()),
        source.isIn(SHRUGS_OFF),
        NightbornDamage.killsVampires(source))) {
      return false;
    }
    final boolean hurt = super.damage(source, amount);
    if (hurt && source.getAttacker() instanceof PlayerEntity player) {
      fightRoll.join(player.getUuid());
    }
    if (hurt && !getWorld().isClient()) {
      fight.hurt(this, plan(), amount);
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
    fight.write(nbt);
  }

  @Override
  public void readCustomDataFromNbt(final NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    fightRoll.read(nbt);
    fight.read(nbt);
    leaveRoutine();
  }
}
