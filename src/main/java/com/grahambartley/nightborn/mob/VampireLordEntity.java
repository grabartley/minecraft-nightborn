package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.lord.BossPresence;
import com.grahambartley.nightborn.mob.look.VampireRig;
import com.grahambartley.nightborn.structure.HouseholdArrival;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class VampireLordEntity extends VampireEntity {

  public static final Identifier STAGE = Identifier.of(Nightborn.MOD_ID, "lord");

  private final BossPresence presence;

  public VampireLordEntity(final EntityType<? extends VampireLordEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 50;
    this.presence = new BossPresence(getDisplayName());
  }

  public BossPresence presence() {
    return presence;
  }

  @Override
  public Set<VampireRig> rigs() {
    return Set.of(VampireRig.LORD);
  }

  @Override
  public EntityData initialize(
      final ServerWorldAccess world,
      final LocalDifficulty difficulty,
      final SpawnReason spawnReason,
      @Nullable final EntityData entityData) {
    return super.initialize(world, difficulty, spawnReason, arrival(entityData));
  }

  static HouseholdArrival arrival(@Nullable final EntityData given) {
    return given instanceof HouseholdArrival arrival && !arrival.stages().isEmpty()
        ? arrival
        : new HouseholdArrival(Map.of(STAGE, 1));
  }

  @Override
  protected void mobTick() {
    super.mobTick();
    presence.update(this);
  }

  @Override
  public void setCustomName(@Nullable final Text name) {
    super.setCustomName(name);
    presence.update(this);
  }

  @Override
  public void onStartedTrackingBy(final ServerPlayerEntity player) {
    super.onStartedTrackingBy(player);
    presence.show(player);
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
