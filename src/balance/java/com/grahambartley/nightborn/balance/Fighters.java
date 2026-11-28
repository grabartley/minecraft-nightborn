package com.grahambartley.nightborn.balance;

import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.guild.HunterEntity;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.mob.VampireEntity;
import com.grahambartley.nightborn.skill.Skill;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

final class Fighters {

  private Fighters() {}

  static MobEntity spawn(
      final ServerWorld world, final Roster roster, final Vec3d at, final float yaw) {
    final MobEntity mob = type(roster).create(world);
    mob.refreshPositionAndAngles(at.x, at.y, at.z, yaw, 0.0F);
    mob.setBodyYaw(yaw);
    mob.setHeadYaw(yaw);
    mob.initialize(
        world, world.getLocalDifficulty(BlockPos.ofFloored(at)), SpawnReason.NATURAL, null);
    roster
        .id()
        .map(Identifier::of)
        .ifPresent(
            id -> {
              if (mob instanceof VampireEntity vampire) {
                vampire.becomeStage(id);
              } else if (mob instanceof HunterEntity hunter) {
                hunter.rollFrom(Map.of(id, 1));
              }
            });
    mob.setPersistent();
    world.spawnEntity(mob);
    return mob;
  }

  static Set<String> tags(final MobEntity mob) {
    final Set<String> tags = new LinkedHashSet<>();
    if (mob instanceof VampireEntity vampire) {
      tags.add(
          "vampire temperament "
              + vampire
                  .householdLife()
                  .temperamentId(mob.getRegistryManager(), mob.getRandom()::nextDouble));
      skills(mob, SkillTree.VAMPIRE, "vampire bloodline ", "vampire power ", tags);
    } else {
      tags.add(
          "hunter kit "
              + (mob instanceof HunterEntity hunter && !hunter.kit().isBlank()
                  ? hunter.kit()
                  : EntityType.getId(mob.getType()).getPath()));
      skills(mob, SkillTree.HUNTER, "hunter path ", "hunter skill ", tags);
    }
    return tags;
  }

  private static void skills(
      final MobEntity mob,
      final SkillTree tree,
      final String linePrefix,
      final String skillPrefix,
      final Set<String> tags) {
    final List<Identifier> owned = SkillAttachment.get(mob, tree).ownedInOrder();
    if (owned.isEmpty()) {
      tags.add(skillPrefix + "none");
    }
    for (final Identifier id : owned) {
      final Skill skill = Skills.skill(mob.getRegistryManager(), id);
      Skills.lineOf(mob.getRegistryManager(), id).ifPresent(line -> tags.add(linePrefix + line));
      if (skill != null && skill.power().isPresent()) {
        tags.add(skillPrefix + id);
      }
    }
  }

  private static EntityType<? extends MobEntity> type(final Roster roster) {
    return switch (roster.kind()) {
      case VAMPIRE -> NightbornMobs.VAMPIRE;
      case VAMPIRE_LORD -> NightbornMobs.VAMPIRE_LORD;
      case HUNTER -> GuildMobs.HUNTER;
      case VETERAN_HUNTER -> GuildMobs.VETERAN_HUNTER;
      case GUILD_MASTER -> GuildMobs.GUILD_MASTER;
    };
  }
}
