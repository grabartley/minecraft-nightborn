package com.grahambartley.nightborn.mob.brain;

import com.grahambartley.nightborn.faction.Allies;
import com.grahambartley.nightborn.faction.Sides;
import com.grahambartley.nightborn.skill.Mending;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.skill.Powers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public final class GroupViews {

  public static final float VIEW_RADIUS = 24.0F;
  static final double REUSE = 8.0;
  static final int UNDER_ATTACK_TICKS = 60;
  static final int SCAN_TICKS = 10;

  private static final Map<RegistryKey<World>, SharedScans<LivingEntity>> SCANS = new HashMap<>();

  private GroupViews() {}

  static void register() {
    ServerLifecycleEvents.SERVER_STOPPED.register(server -> SCANS.clear());
  }

  public static Situation.Group read(final MobEntity mob) {
    final World world = mob.getWorld();
    final long now = world.getTime();
    final List<LivingEntity> near =
        SCANS
            .computeIfAbsent(world.getRegistryKey(), key -> new SharedScans<>(REUSE, SCAN_TICKS))
            .near(
                now,
                mob.getPos(),
                at ->
                    world.getEntitiesByClass(
                        LivingEntity.class,
                        new Box(at, at).expand(VIEW_RADIUS + REUSE),
                        candidate -> candidate.isAlive() && !candidate.isSpectator()));
    final List<LivingEntity> allies = new ArrayList<>();
    final List<LivingEntity> others = new ArrayList<>();
    for (final LivingEntity other : near) {
      if (other == mob
          || !other.isAlive()
          || other.isRemoved()
          || mob.distanceTo(other) > VIEW_RADIUS) {
        continue;
      }
      (Allies.allied(mob, other) ? allies : others).add(other);
    }
    final List<LivingEntity> foes = new ArrayList<>();
    for (final LivingEntity other : others) {
      if (PowerReach.touches(mob, other)
          && Sides.foe(
              Allies.opposed(mob, other),
              fightsUs(other, mob, allies),
              fought(other, mob, allies))) {
        foes.add(other);
      }
    }
    final Claims claims = HelpClaims.in(world);
    final List<Situation.Member> members = new ArrayList<>(allies.size());
    for (final LivingEntity ally : allies) {
      final boolean underAttack =
          underAttack(
              ally.getAttacker() != null,
              ally.age,
              ally.getLastAttackedTime(),
              foes.stream().anyMatch(foe -> Powers.foe(foe) == ally));
      members.add(
          new Situation.Member(
              ally.getUuid(),
              mob.distanceTo(ally),
              health(ally),
              underAttack,
              ally.getAttached(Mending.TYPE) != null,
              fighting(
                  ally instanceof MobEntity fighter && fighter.getTarget() != null, underAttack),
              claims.claimedByOther(ally.getUuid(), mob.getUuid(), now)));
    }
    return Situation.Group.of(
        members, foes.stream().map(foe -> (double) mob.distanceTo(foe)).toList());
  }

  public static boolean underAttack(
      final boolean attacker, final int age, final int lastAttacked, final boolean targetedByFoe) {
    return attacker && age - lastAttacked <= UNDER_ATTACK_TICKS || targetedByFoe;
  }

  public static boolean fighting(final boolean hasTarget, final boolean underAttack) {
    return hasTarget || underAttack;
  }

  private static boolean fightsUs(
      final LivingEntity other, final MobEntity mob, final List<LivingEntity> allies) {
    final LivingEntity foe = Powers.foe(other);
    return foe != null && (foe == mob || allies.contains(foe));
  }

  private static boolean fought(
      final LivingEntity other, final MobEntity mob, final List<LivingEntity> allies) {
    return mob.getTarget() == other || allies.stream().anyMatch(ally -> Powers.foe(ally) == other);
  }

  private static float health(final LivingEntity entity) {
    return entity.getMaxHealth() <= 0.0F ? 0.0F : entity.getHealth() / entity.getMaxHealth();
  }
}
