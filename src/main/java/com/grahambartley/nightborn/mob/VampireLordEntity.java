package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.mob.look.VampireRig;
import java.util.Set;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class VampireLordEntity extends VampireBossEntity {

  public static final Identifier STAGE = Identifier.of(Nightborn.MOD_ID, "lord");

  public VampireLordEntity(final EntityType<? extends VampireLordEntity> type, final World world) {
    super(type, world);
    this.experiencePoints = 50;
  }

  @Override
  protected Identifier ownStage() {
    return STAGE;
  }

  @Override
  public Set<VampireRig> rigs() {
    return Set.of(VampireRig.LORD);
  }
}
