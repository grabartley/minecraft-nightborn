package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.servant.job.DayJobOrders;
import com.grahambartley.nightborn.servant.job.ScoutOrders;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;

public final class ThrallWheel {

  private ThrallWheel() {}

  static void register() {
    CommandWheel.subject(ThrallEntity.class, SkillTree.VAMPIRE)
        .add(new ThrallOrders())
        .add(new DayJobOrders<ThrallEntity>())
        .add(new ScoutOrders<ThrallEntity>(SkillTree.VAMPIRE));
  }
}
