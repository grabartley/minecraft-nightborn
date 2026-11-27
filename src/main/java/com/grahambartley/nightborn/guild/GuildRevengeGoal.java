package com.grahambartley.nightborn.guild;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public final class GuildRevengeGoal extends RevengeGoal {

  static final double CALL_HEIGHT = 10.0;

  public <T extends PathAwareEntity & GuildFighter> GuildRevengeGoal(final T fighter) {
    super(fighter, GuildFighter.class, PlayerEntity.class);
    setGroupRevenge();
    this.fighter = fighter;
  }

  private final GuildFighter fighter;

  public static boolean answers(
      final boolean self, final boolean free, final boolean busy, final boolean teammate) {
    return !self && free && !busy && !teammate;
  }

  @Override
  public boolean canStart() {
    return fighter.canFight() && super.canStart();
  }

  @Override
  protected void callSameTypeForRevenge() {
    final LivingEntity attacker = mob.getAttacker();
    if (attacker == null) {
      return;
    }
    final double reach = getFollowRange();
    final Box box = Box.from(mob.getPos()).expand(reach, CALL_HEIGHT, reach);
    for (final MobEntity ally :
        mob.getWorld().getEntitiesByClass(MobEntity.class, box, e -> true)) {
      if (answers(
          ally == mob,
          ally instanceof GuildFighter guild && guild.canFight(),
          ally.getTarget() != null,
          ally.isTeammate(attacker))) {
        setMobEntityTarget(ally, attacker);
      }
    }
  }
}
