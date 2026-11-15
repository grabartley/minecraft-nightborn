package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.HomeRules;
import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;

public class GuardCoffinGoal extends ActiveTargetGoal<HostileEntity> {

  public GuardCoffinGoal(final HumanFamiliarEntity familiar) {
    super(
        familiar,
        HostileEntity.class,
        10,
        true,
        false,
        intruder ->
            familiar
                .home()
                .filter(
                    home ->
                        HomeRules.drivesOff(
                            intruder instanceof CreeperEntity
                                || intruder.getType().isIn(Vampires.VAMPIRE_MOBS),
                            intruder.getBlockPos().getSquaredDistance(home),
                            ServerConfigService.current().familiars().guardRange()))
                .isPresent());
  }
}
