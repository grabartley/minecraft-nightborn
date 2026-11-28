package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.BloodCapacity;
import com.grahambartley.nightborn.config.AgeSettings;
import com.grahambartley.nightborn.core.CoreBoosts;
import com.grahambartley.nightborn.mob.stage.StageBody;
import com.grahambartley.nightborn.mob.stage.StageStats;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public final class AgeBody {

  static final Identifier OLD_HEALTH = Identifier.of(Nightborn.MOD_ID, "age_health");
  static final Identifier OLD_DAMAGE = Identifier.of(Nightborn.MOD_ID, "age_damage");

  private AgeBody() {}

  public static void apply(final PlayerEntity vampire, final AgeSettings settings) {
    final int rank = AgeAttachment.get(vampire).rank();
    embody(
        vampire,
        AgeLadder.of(vampire.getRegistryManager())
            .body(rank)
            .underHealthMultiplier(CoreBoosts.DOUBLE_HEALTH));
    BloodCapacity.setExtra(vampire, AgeGifts.bloodBar(rank, settings));
  }

  public static void remove(final PlayerEntity player) {
    embody(player, StageBody.SAME);
    BloodCapacity.setExtra(player, 0);
  }

  private static void embody(final PlayerEntity player, final StageBody body) {
    Modifiers.remove(player, EntityAttributes.GENERIC_MAX_HEALTH, OLD_HEALTH);
    Modifiers.remove(player, EntityAttributes.GENERIC_ATTACK_DAMAGE, OLD_DAMAGE);
    StageStats.apply(player, body);
  }
}
