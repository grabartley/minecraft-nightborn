package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.Nightborn;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class BloodGiftGrants {

  public static final AttachmentType<ActiveGift> ACTIVE =
      AttachmentRegistry.create(
          Identifier.of(Nightborn.MOD_ID, "blood_gift"),
          builder -> builder.persistent(ActiveGift.CODEC));

  static final String GAINED = "nightborn.blood_gift.gained";

  private BloodGiftGrants() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(BloodGiftGrants::tick);
  }

  public static void grant(
      final ServerPlayerEntity vampire, final Identifier giftId, final float strength) {
    final Optional<BloodGift> gift = BloodGifts.byId(vampire.getRegistryManager(), giftId);
    if (gift.isEmpty()) {
      return;
    }
    final ActiveGift current = vampire.getAttached(ACTIVE);
    final boolean refresh = current != null && current.refreshes(giftId, strength);
    if (!refresh) {
      end(vampire);
      vampire.setAttached(ACTIVE, new ActiveGift(giftId, strength));
      vampire.sendMessage(Text.translatable(GAINED, Text.translatable(gift.get().name())), true);
    }
    vampire.removeStatusEffect(BloodGiftEffect.ENTRY);
    vampire.addStatusEffect(
        new StatusEffectInstance(BloodGiftEffect.ENTRY, gift.get().ticks(), 0, false, false, true));
    apply(vampire, gift.get().modifiers(), strength);
  }

  public static Optional<ActiveGift> active(final ServerPlayerEntity player) {
    return Optional.ofNullable(player.getAttached(ACTIVE));
  }

  private static void tick(final MinecraftServer server) {
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      final ActiveGift active = player.getAttached(ACTIVE);
      if (active == null) {
        continue;
      }
      final Optional<BloodGift> gift = BloodGifts.byId(player.getRegistryManager(), active.gift());
      if (gift.isEmpty() || !player.hasStatusEffect(BloodGiftEffect.ENTRY)) {
        end(player);
      } else {
        apply(player, gift.get().modifiers(), active.strength());
      }
    }
  }

  private static void apply(
      final ServerPlayerEntity player, final List<GiftModifier> modifiers, final float strength) {
    for (int index = 0; index < modifiers.size(); index++) {
      final GiftModifier modifier = modifiers.get(index);
      final EntityAttributeInstance instance = player.getAttributeInstance(modifier.attribute());
      if (instance != null && !instance.hasModifier(GiftModifier.id(index))) {
        instance.addTemporaryModifier(modifier.modifier(index, strength));
      }
    }
  }

  static void end(final ServerPlayerEntity player) {
    player.removeAttached(ACTIVE);
    Registries.ATTRIBUTE
        .streamEntries()
        .map(player::getAttributeInstance)
        .filter(instance -> instance != null)
        .forEach(
            instance -> {
              for (int index = 0; index < BloodGift.MOST_MODIFIERS; index++) {
                instance.removeModifier(GiftModifier.id(index));
              }
            });
  }
}
