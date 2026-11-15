package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodItems;
import com.grahambartley.nightborn.blood.StarvingAttachment;
import com.grahambartley.nightborn.coffin.CoffinDayCycle;
import com.grahambartley.nightborn.command.NightbornCommand;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.downed.HardcoreVampireDeath;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.gore.GoreEvents;
import com.grahambartley.nightborn.gore.GoreParticles;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.network.NightbornPayloads;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.skill.SkillFeatures;
import com.grahambartley.nightborn.vomit.VomitEffects;
import com.grahambartley.nightborn.weakened.Weakened;

public final class VampireFeatures {

  private VampireFeatures() {}

  public static void register() {
    NightbornPayloads.register();
    ServerConfigService.register();
    BloodAttachment.register();
    StarvingAttachment.register();
    Levitation.register();
    VomitEffects.register();
    GoreParticles.register();
    GoreEvents.register();
    VisibleStateAttachment.register();
    BiteSicknessAttachment.register();
    SkillFeatures.register();
    Weakened.register();
    BiteSicknessEffect.register();
    BloodItems.register();
    NightbornCommand.register();
    Downed.register();
    FeedingSessions.register();
    BiteSickness.register();
    HardcoreVampireDeath.register();
    FactionPlayerList.register();
    CoffinDayCycle.register();
  }
}
