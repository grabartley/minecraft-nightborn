package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.servant.job.DayJobOrders;
import com.grahambartley.nightborn.servant.job.ScoutOrders;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;

public final class FamiliarWheel {

  private FamiliarWheel() {}

  static void register() {
    CommandWheel.subject(HumanFamiliarEntity.class, SkillTree.VAMPIRE)
        .add(new ErrandOrders())
        .add(new DayJobOrders<HumanFamiliarEntity>())
        .add(new ScoutOrders<HumanFamiliarEntity>(SkillTree.VAMPIRE))
        .add(new AskAfter());
  }
}
