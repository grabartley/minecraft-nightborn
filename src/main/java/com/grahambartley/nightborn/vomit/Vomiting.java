package com.grahambartley.nightborn.vomit;

import com.grahambartley.nightborn.network.VomitPayload;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.NightbornDamage;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

public final class Vomiting {

  static final int NAUSEA_TICKS = 120;

  private Vomiting() {}

  public static ItemStack swallow(final PlayerEntity vampire, final ItemStack stack) {
    final ItemStack leftBehind = Consumables.leftBehind(stack);
    vampire.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
    if (vampire instanceof ServerPlayerEntity server) {
      throwUp(server);
    }
    if (vampire.isInCreativeMode()) {
      return stack;
    }
    stack.decrement(1);
    if (stack.isEmpty()) {
      return leftBehind;
    }
    if (!leftBehind.isEmpty() && !vampire.getInventory().insertStack(leftBehind)) {
      vampire.dropItem(leftBehind, false);
    }
    return stack;
  }

  static void throwUp(final ServerPlayerEntity vampire) {
    vampire.addStatusEffect(
        new StatusEffectInstance(StatusEffects.NAUSEA, NAUSEA_TICKS, 0, false, false, true));
    final VomitPayload payload = new VomitPayload(vampire.getId());
    ServerPlayNetworking.send(vampire, payload);
    PlayerLookup.tracking(vampire).forEach(watcher -> ServerPlayNetworking.send(watcher, payload));
    final int damage = ServerConfigService.current().blood().vomitDamage();
    if (damage > 0) {
      vampire.damage(NightbornDamage.vomit(vampire.getWorld()), damage);
    }
  }
}
