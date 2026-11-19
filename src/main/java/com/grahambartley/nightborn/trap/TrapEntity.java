package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.batform.BatForm;
import com.grahambartley.nightborn.boss.ZoneMarkEntity;
import com.grahambartley.nightborn.hold.HoldKind;
import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.occult.MistForm;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrapEntity extends ZoneMarkEntity {

  static final double STEP_HEIGHT = 0.3;
  static final double STEP_REACH = 1.0;

  private final Set<UUID> inside = new HashSet<>();
  @Nullable private UUID owner;
  @Nullable private Identifier skill;
  @Nullable private TrapPower power;
  private long armedAt;
  private float extraDamage;
  private float seenWithin;

  public TrapEntity(final EntityType<? extends TrapEntity> type, final World world) {
    super(type, world);
  }

  static TrapEntity set(
      final ServerWorld world,
      final LivingEntity owner,
      final Identifier skill,
      final TrapPower power,
      final TrapLoadout loadout,
      final BlockPos spot) {
    final TrapEntity trap = new TrapEntity(TrapEntities.TRAP, world);
    final long now = world.getTime();
    trap.owner = owner.getUuid();
    trap.skill = skill;
    trap.power = power;
    trap.armedAt = now + loadout.armTicks();
    trap.extraDamage = loadout.extraDamage();
    trap.seenWithin = loadout.seenWithin();
    trap.shape(power.size(), Optional.of(power.look()));
    trap.linger(power.durationTicks());
    trap.refreshPositionAndAngles(
        spot.getX() + 0.5, spot.getY() + 0.02, spot.getZ() + 0.5, owner.getYaw(), 0.0F);
    world.spawnEntity(trap);
    return trap;
  }

  public boolean ownedBy(final UUID id, final Identifier power) {
    return id.equals(owner) && power.equals(skill);
  }

  @Nullable
  public LivingEntity owner() {
    return owner != null
            && getWorld() instanceof ServerWorld world
            && world.getEntity(owner) instanceof LivingEntity living
        ? living
        : null;
  }

  @Override
  public void tick() {
    super.tick();
    if (!(getWorld() instanceof ServerWorld world)) {
      return;
    }
    final LivingEntity hunter = owner();
    if (power == null || hunter == null) {
      discard();
      return;
    }
    if (world.getTime() >= armedAt) {
      watch(world, hunter, power);
    }
  }

  private void watch(final ServerWorld world, final LivingEntity hunter, final TrapPower trap) {
    final Set<UUID> seen = new HashSet<>();
    final double reach = trap.stepped() ? STEP_REACH : trap.triggerRadius();
    final Box floor = new Box(getBlockPos()).withMaxY(getBlockPos().getY() + STEP_HEIGHT);
    boolean sprung = false;
    for (final LivingEntity met :
        world.getEntitiesByClass(
            LivingEntity.class,
            getBoundingBox().expand(reach, 2.0, reach),
            candidate -> candidate.isAlive() && !candidate.isSpectator())) {
      if (!TrapRules.within(
          trap.stepped(),
          trap.triggerRadius(),
          met.squaredDistanceTo(getPos()),
          met.getBoundingBox().intersects(floor))) {
        continue;
      }
      seen.add(met.getUuid());
      if (inside.contains(met.getUuid())) {
        continue;
      }
      final TrapRules.Outcome outcome = outcome(hunter, met, trap);
      if (outcome == TrapRules.Outcome.CATCH) {
        inside.add(met.getUuid());
        catchIn(hunter, met, trap);
        sprung = true;
      } else if (outcome == TrapRules.Outcome.SPRING) {
        inside.add(met.getUuid());
        sprung = true;
      }
    }
    inside.retainAll(seen);
    if (sprung) {
      spring(world, trap);
    }
  }

  private TrapRules.Outcome outcome(
      final LivingEntity hunter, final LivingEntity met, final TrapPower trap) {
    final boolean player = met instanceof PlayerEntity;
    final boolean vampire = Vampires.isVampire(met);
    return TrapRules.meets(
        vampire,
        BatForm.isBat(met),
        MistForm.on(met),
        trap.catchesBatsAndMist(),
        vampire && PowerReach.touches(hunter, met),
        player,
        player
            && !vampire
            && TrapRules.spares(
                TrapGifts.senses(HeldTrapGifts.of(met)),
                met == hunter,
                TrapAllies.allied(hunter, met)),
        trap.stepped());
  }

  private void catchIn(final LivingEntity hunter, final LivingEntity met, final TrapPower trap) {
    final float damage = trap.damage() + extraDamage;
    if (damage > 0.0F) {
      met.damage(TrapDamage.of(this, trap.damageType(), hunter), damage);
    }
    final int pin = trap.pinTicks(met instanceof PlayerEntity);
    if (pin > 0) {
      Holds.hold(hunter, met, HoldKind.PIN, pin);
    }
    for (final StatusEffectInstance effect : trap.effects()) {
      met.addStatusEffect(new StatusEffectInstance(effect), hunter);
    }
    if (met instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.trap.caught"), true);
    }
  }

  private void spring(final ServerWorld world, final TrapPower trap) {
    world.playSound(
        null,
        getBlockPos(),
        SoundEvent.of(trap.spring().sound()),
        SoundCategory.PLAYERS,
        1.0F,
        1.0F);
    world.spawnParticles(
        trap.spring().particle(),
        getX(),
        getY() + 0.3,
        getZ(),
        trap.spring().count(),
        trap.size(),
        0.3,
        trap.size(),
        0.05);
    if (KillingGrounds.covers(world, getPos())) {
      armedAt = world.getTime();
    } else {
      discard();
    }
  }

  @Override
  public boolean canBeSpectated(final ServerPlayerEntity viewer) {
    return TrapRules.seen(Vampires.isVampire(viewer), viewer.squaredDistanceTo(this), seenWithin);
  }

  @Override
  public boolean isAttackable() {
    return false;
  }

  @Override
  public boolean canHit() {
    return false;
  }

  @Override
  public boolean handleAttack(final Entity attacker) {
    return true;
  }
}
