package com.grahambartley.nightborn.vampire;

import com.grahambartley.nightborn.accessory.AccessoryGifts;
import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.age.Ageing;
import com.grahambartley.nightborn.blood.BloodAttachment;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.blood.BloodGiftGrants;
import com.grahambartley.nightborn.blood.StarvingAttachment;
import com.grahambartley.nightborn.coffin.CoffinOwnership;
import com.grahambartley.nightborn.core.VampireCore;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.faction.FactionPlayerList;
import com.grahambartley.nightborn.feeding.FeedingSessions;
import com.grahambartley.nightborn.feeding.TurningBite;
import com.grahambartley.nightborn.hunt.Hunts;
import com.grahambartley.nightborn.levitation.Levitation;
import com.grahambartley.nightborn.moment.Moment;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.name.StageNames;
import com.grahambartley.nightborn.sickness.BiteSicknessAttachment;
import com.grahambartley.nightborn.sickness.BiteSicknessEffect;
import com.grahambartley.nightborn.skill.SkillAttributes;
import com.grahambartley.nightborn.weakened.Weakened;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public final class VampireTransitions {

  private VampireTransitions() {}

  public static boolean turnInCeremony(final ServerPlayerEntity player) {
    if (!turn(player)) {
      return false;
    }
    Moments.begin(player, Moment.TURNING);
    return true;
  }

  public static boolean turn(final ServerPlayerEntity player) {
    BiteSicknessAttachment.clear(player);
    player.removeStatusEffect(BiteSicknessEffect.ENTRY);
    if (Vampires.isVampire(player)) {
      return false;
    }
    FactionAttachment.set(player, Faction.VAMPIRE);
    MilestoneCriterion.reach(player, Milestone.TURNED);
    BloodAttachment.set(player, BloodCapacity.of(player));
    player.getHungerManager().setFoodLevel(BloodAttachment.FULL);
    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
    SkillAttributes.apply(player);
    AccessoryGifts.apply(player);
    player.sendMessage(Text.translatable("nightborn.turning.turned"), false);
    FactionPlayerList.refresh(player);
    StageNames.changed(player);
    return true;
  }

  public static void makeHuman(final ServerPlayerEntity player) {
    BiteSicknessAttachment.clear(player);
    player.removeStatusEffect(BiteSicknessEffect.ENTRY);
    FeedingSessions.end(player);
    FactionAttachment.set(player, Faction.HUMAN);
    Ageing.clear(player);
    Hunts.clear(player);
    BloodAttachment.set(player, BloodAttachment.FULL);
    Downed.clear(player);
    Weakened.clear(player);
    StarvingAttachment.set(player, 0);
    Levitation.setDown(player);
    TurningBite.set(player, false);
    BloodGiftGrants.end(player);
    VisibleStateAttachment.set(player, VisibleState.NONE);
    VampireCore.remove(player);
    CoffinOwnership.release(player.server, player.getUuid());
    SkillAttributes.apply(player);
    AccessoryGifts.apply(player);
    player.resetStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
    FactionPlayerList.refresh(player);
  }
}
