package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;

public final class GuildWheel {

  private GuildWheel() {}

  static void register() {
    CommandWheel.subject(GuildMasterEntity.class, SkillTree.HUNTER)
        .add(new GuildOathOrder())
        .add(new GuildCounterOrders(GuildCounter.Kind.GEAR))
        .add(new GuildCounterOrders(GuildCounter.Kind.CONTRACTS))
        .add(new GuildRumour());
  }
}
