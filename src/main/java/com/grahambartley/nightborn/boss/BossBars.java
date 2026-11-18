package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.lord.BossBarLook;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

public final class BossBars {

  private BossBars() {}

  public static Optional<BossBarLook> state(
      final boolean warded, final boolean draining, final Optional<ChannelBar> channel) {
    return BossBarLook.state(warded, draining)
        .or(() -> channel.map(bar -> BossBarLook.phase(bar.color(), bar.key())));
  }

  public static <T extends MobEntity & ArenaBoss> void show(
      final T boss, final BossFight fight, final Optional<BossFightPlan> plan) {
    if (plan.isEmpty() || Downed.state(boss) != null) {
      boss.presence().update(boss);
      return;
    }
    final long now = boss.getWorld().getTime();
    final float percent = percent(boss, fight, plan.get());
    final Optional<BossBarLook> state =
        state(fight.warded(now), fight.draining(now), fight.channelBar(now));
    if (state.isPresent()) {
      boss.presence()
          .update(state.get().title(boss.getDisplayName()), state.get().color(), percent);
      return;
    }
    final BossPhase phase = plan.get().phase(fight.phase());
    boss.presence()
        .update(
            BossBarLook.phase(phase.color(), phase.translationKey()).title(boss.getDisplayName()),
            phase.color(),
            percent);
  }

  private static float percent(
      final LivingEntity boss, final BossFight fight, final BossFightPlan plan) {
    return PhaseTrack.barPercent(
        boss.getHealth(), fight.floorHealth(boss, plan), fight.ceilingHealth(boss, plan));
  }
}
