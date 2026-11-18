package com.grahambartley.nightborn.thrall;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.servant.ServantVigour;
import com.grahambartley.nightborn.vampire.Modifiers;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public final class ThrallVigour {

  static final Identifier MODIFIER = Identifier.of(Nightborn.MOD_ID, "thrall/master_gift");

  private ThrallVigour() {}

  public static double bonus(final double factor) {
    return Math.max(0.0, factor - 1.0);
  }

  static void follow(final ServerWorld world, final ThrallEntity thrall) {
    thrall
        .owner()
        .map(id -> world.getServer().getPlayerManager().getPlayer(id))
        .ifPresent(master -> apply(thrall, master));
  }

  private static void apply(final ThrallEntity thrall, final ServerPlayerEntity master) {
    ServantVigour.apply(thrall, MindAttributes.value(master, MindAttributes.SERVANT_VIGOUR));
    final double bonus = bonus(GiftAttributes.value(master, GiftAttributes.THRALL_HEALTH));
    if (bonus <= 0.0) {
      Modifiers.remove(thrall, EntityAttributes.GENERIC_MAX_HEALTH, MODIFIER);
      Modifiers.clampHealth(thrall);
      return;
    }
    Modifiers.set(
        thrall,
        EntityAttributes.GENERIC_MAX_HEALTH,
        MODIFIER,
        bonus,
        EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
  }
}
