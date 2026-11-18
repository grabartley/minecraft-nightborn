package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.lord.BossPresence;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class BossVampireEntity extends VampireEntity {

  private final BossPresence presence;

  protected BossVampireEntity(
      final EntityType<? extends BossVampireEntity> type, final World world) {
    super(type, world);
    this.presence = new BossPresence(getDisplayName());
  }

  public BossPresence presence() {
    return presence;
  }

  @Override
  protected boolean answersEveryVampire() {
    return true;
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    showBar();
  }

  protected void showBar() {
    presence.update(this);
  }

  protected boolean barShowsOnSight() {
    return true;
  }

  @Override
  public void setCustomName(@Nullable final Text name) {
    super.setCustomName(name);
    presence.update(this);
  }

  @Override
  public void onStartedTrackingBy(final ServerPlayerEntity player) {
    super.onStartedTrackingBy(player);
    if (barShowsOnSight()) {
      presence.show(player);
    }
  }

  @Override
  public void onStoppedTrackingBy(final ServerPlayerEntity player) {
    super.onStoppedTrackingBy(player);
    presence.hide(player);
  }

  @Override
  public void onRemoved() {
    super.onRemoved();
    presence.hideAll();
  }
}
