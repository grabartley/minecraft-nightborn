package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.DownedState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class BossPresence {

  private final ServerBossBar bar;

  public BossPresence(final Text name) {
    this.bar = new ServerBossBar(name, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
  }

  public void show(final ServerPlayerEntity player) {
    bar.addPlayer(player);
  }

  public void hide(final ServerPlayerEntity player) {
    bar.removePlayer(player);
  }

  public void hideAll() {
    bar.clearPlayers();
  }

  public void update(final LivingEntity boss) {
    final DownedState downed = Downed.state(boss);
    update(
        boss.getDisplayName(),
        boss.getHealth(),
        boss.getMaxHealth(),
        downed == null ? null : downed.phase());
  }

  public void update(
      final Text name,
      final float health,
      final float maxHealth,
      @Nullable final DownedState.Phase phase) {
    final BossBarLook look = BossBarLook.of(phase);
    bar.setName(look.title(name));
    bar.setColor(look.color());
    bar.setPercent(BossBarLook.percent(health, maxHealth));
  }

  public ServerBossBar bar() {
    return bar;
  }
}
