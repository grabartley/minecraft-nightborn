package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.faction.GuildMembership;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class RankBody {

  static final Identifier HEALTH = Identifier.of(Nightborn.MOD_ID, "rank_health");

  private RankBody() {}

  public static void apply(final LivingEntity hunter) {
    Modifiers.set(
        hunter,
        EntityAttributes.GENERIC_MAX_HEALTH,
        HEALTH,
        gifts(hunter).health(),
        Operation.ADD_VALUE);
    Modifiers.clampHealth(hunter);
  }

  public static float strike(
      @Nullable final Entity attacker, final Entity target, final float damage) {
    return attacker instanceof LivingEntity hunter
        ? damage + gifts(hunter).against(Vampires.isVampire(target))
        : damage;
  }

  private static RankGifts gifts(final LivingEntity hunter) {
    return RankGifts.of(
        RankLadder.of(hunter.getRegistryManager()),
        GuildMembership.isHunter(hunter),
        OathAttachment.get(hunter).rank());
  }
}
