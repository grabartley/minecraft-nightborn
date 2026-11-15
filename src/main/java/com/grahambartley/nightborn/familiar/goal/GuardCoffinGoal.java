package com.grahambartley.nightborn.familiar.goal;

import com.grahambartley.nightborn.familiar.HumanFamiliarEntity;
import com.grahambartley.nightborn.server.ServerConfigService;
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
            !(intruder instanceof CreeperEntity)
                && familiar
                    .home()
                    .filter(
                        home ->
                            intruder
                                .getBlockPos()
                                .isWithinDistance(
                                    home, ServerConfigService.current().familiars().guardRange()))
                    .isPresent());
  }
}
