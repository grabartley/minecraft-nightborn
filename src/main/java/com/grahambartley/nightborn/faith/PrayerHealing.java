package com.grahambartley.nightborn.faith;

import com.grahambartley.nightborn.accessory.GiftAttributes;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import com.grahambartley.nightborn.dismember.Wounds;
import com.grahambartley.nightborn.occult.Circles;
import com.grahambartley.nightborn.skill.HeldRanks;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

final class PrayerHealing {

  private PrayerHealing() {}

  static List<RegistryEntry<StatusEffect>> clears(final LivingEntity caster) {
    return PrayerGift.cleared(
        HeldRanks.of(caster).stream().flatMap(rank -> rank.prayerGifts().stream()).toList());
  }

  static boolean heal(
      final LivingEntity caster,
      final LivingEntity target,
      final float health,
      final List<RegistryEntry<StatusEffect>> clears) {
    if (!target.isAlive() || Circles.stopsPrayers(target.getWorld(), target.getPos())) {
      return false;
    }
    clears.forEach(target::removeStatusEffect);
    Wounds.close(target);
    final float before = target.getHealth();
    target.heal(
        PrayerRules.boosted(health, GiftAttributes.value(caster, GiftAttributes.PRAYER_HEALING)));
    final int healed = PrayerRules.counted(target.getHealth() - before);
    if (healed > 0 && target != caster) {
      Deeds.record(caster, Deed.of(DeedKinds.PRAYER_HEALED).times(healed));
    }
    if (healed > 0 && target != caster && target instanceof ServerPlayerEntity player) {
      player.sendMessage(Text.translatable("nightborn.faith.mended"), true);
    }
    return true;
  }
}
