package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.challenge.GuildTrialOrder;
import com.grahambartley.nightborn.guild.rite.GuildRiteOrder;
import com.grahambartley.nightborn.skill.SkillAttachment;
import com.grahambartley.nightborn.skill.SkillNames;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.wheel.CommandWheel;
import java.util.List;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class GuildWheel {

  private GuildWheel() {}

  static void register() {
    CommandWheel.subject(GuildMasterEntity.class, SkillTree.HUNTER)
        .add(new GuildOathOrder())
        .add(new GuildCounterOrders(GuildCounter.Kind.GEAR))
        .add(new GuildCounterOrders(GuildCounter.Kind.CONTRACTS))
        .add(new GuildRankOrder())
        .add(new GuildJournalOrder())
        .add(new GuildTrialOrder())
        .add(new GuildRumour())
        .add(new GuildRiteOrder());
    CommandWheel.subject(HunterEntity.class, SkillTree.HUNTER)
        .titled(
            hunter ->
                hunterTitle(
                    hunter.getDisplayName(), SkillAttachment.get(hunter, SkillTree.HUNTER).lines()))
        .add(new HunterReferralOrder())
        .add(new HunterTradeOrder());
  }

  static Text hunterTitle(final Text name, final List<Identifier> paths) {
    return paths.isEmpty()
        ? name
        : Text.translatable("nightborn.hunter.wheel_title", name, SkillNames.lines(paths));
  }
}
