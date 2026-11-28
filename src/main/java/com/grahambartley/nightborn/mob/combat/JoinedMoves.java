package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.mob.brain.ActionReadiness;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;

public final class JoinedMoves implements MobFighting.Moves {

  private final MobFighting.Moves base;
  private final Supplier<MobFighting.Moves> extra;

  public JoinedMoves(final MobFighting.Moves base, final Supplier<MobFighting.Moves> extra) {
    this.base = base;
    this.extra = extra;
  }

  @Override
  public void tick() {
    base.tick();
    extra.get().tick();
  }

  @Override
  public void rest() {
    base.rest();
    extra.get().rest();
  }

  @Override
  public List<ActionReadiness> actions() {
    final List<ActionReadiness> more = extra.get().actions();
    if (more.isEmpty()) {
      return base.actions();
    }
    final List<ActionReadiness> all = new ArrayList<>(base.actions());
    all.addAll(more);
    return all;
  }

  @Override
  public boolean act(final Identifier action) {
    return base.act(action) || extra.get().act(action);
  }

  @Override
  public boolean busy() {
    return base.busy() || extra.get().busy();
  }

  @Override
  public boolean reacting() {
    return base.reacting();
  }

  @Override
  public void reacted() {
    base.reacted();
  }
}
