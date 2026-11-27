package com.grahambartley.nightborn.rank;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.SkillSettings;
import com.grahambartley.nightborn.server.ServerConfigService;
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

  public static void apply(final LivingEntity hunter, final SkillSettings settings) {
    Modifiers.set(
        hunter,
        EntityAttributes.GENERIC_MAX_HEALTH,
        HEALTH,
        RankGifts.of(GuildRanks.of(hunter), settings).health(),
        Operation.ADD_VALUE);
    Modifiers.clampHealth(hunter);
  }

  public static float strike(
      @Nullable final Entity attacker, final Entity target, final float damage) {
    return attacker instanceof LivingEntity hunter
        ? damage
            + RankGifts.of(GuildRanks.of(hunter), ServerConfigService.current().skills())
                .against(Vampires.isVampire(target))
        : damage;
  }
}
