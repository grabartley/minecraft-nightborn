package com.grahambartley.nightborn.hunt;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.guild.GuildMasterEntity;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.guild.party.PartyMember;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.gen.structure.Structure;

public final class BandCheck {

  public static final TagKey<Structure> CAMPS =
      TagKey.of(RegistryKeys.STRUCTURE, Identifier.of(Nightborn.MOD_ID, "hunter_camps"));
  static final double CAMP_REACH = 16.0;

  private BandCheck() {}

  public static boolean last(final LivingEntity victim) {
    if (!(victim.getWorld() instanceof ServerWorld world)) {
      return false;
    }
    final Optional<UUID> leader = leader(victim);
    if (leader.isPresent()) {
      return noneStanding(
          world,
          victim,
          victim.getBoundingBox().expand(PartyMember.LEADER_NEAR),
          other -> other.getUuid().equals(leader.get()) || leader(other).equals(leader));
    }
    return camp(world, victim)
        .map(
            box ->
                noneStanding(
                    world,
                    victim,
                    Box.from(box).expand(CAMP_REACH),
                    other ->
                        other instanceof PathAwareEntity resident
                            && resident.hasPositionTarget()
                            && box.contains(resident.getPositionTarget())))
        .orElse(false);
  }

  static Optional<UUID> leader(final LivingEntity hunter) {
    if (hunter instanceof HunterEntity member) {
      return member.partyLeader();
    }
    if (hunter instanceof GuildMasterEntity master && master.party() != null) {
      return Optional.of(master.getUuid());
    }
    return Optional.empty();
  }

  private static Optional<BlockBox> camp(final ServerWorld world, final LivingEntity victim) {
    if (!(victim instanceof PathAwareEntity resident) || !resident.hasPositionTarget()) {
      return Optional.empty();
    }
    final BlockPos home = resident.getPositionTarget();
    if (!world.isPosLoaded(home.getX(), home.getZ())) {
      return Optional.empty();
    }
    final StructureStart start = world.getStructureAccessor().getStructureContaining(home, CAMPS);
    return start.hasChildren() ? Optional.of(start.getBoundingBox()) : Optional.empty();
  }

  private static boolean noneStanding(
      final ServerWorld world,
      final LivingEntity victim,
      final Box area,
      final Predicate<LivingEntity> bandmate) {
    return world
        .getEntitiesByClass(
            LivingEntity.class,
            area,
            other ->
                other != victim
                    && other.isAlive()
                    && other.getType().isIn(DownedTargeting.HUNTERS)
                    && !StandIn.is(other)
                    && bandmate.test(other))
        .isEmpty();
  }
}
