package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.blood.GiftModifier;
import com.grahambartley.nightborn.faction.Faction;
import com.grahambartley.nightborn.vampire.Modifiers;
import com.grahambartley.nightborn.vampire.Vampires;
import dev.emi.trinkets.api.TrinketsApi;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class AccessoryGifts {

  static final String MODIFIER_PREFIX = "accessory/";

  private AccessoryGifts() {}

  public static void apply(final LivingEntity wearer) {
    final Faction faction = Vampires.isVampire(wearer) ? Faction.VAMPIRE : Faction.HUMAN;
    final Map<Identifier, Accessory> worn = new HashMap<>();
    TrinketsApi.getTrinketComponent(wearer)
        .ifPresent(
            component ->
                component.forEach(
                    (slot, stack) ->
                        Accessories.of(wearer.getWorld().getRegistryManager(), stack)
                            .ifPresent(
                                accessory ->
                                    worn.put(Registries.ITEM.getId(stack.getItem()), accessory))));
    final Map<Identifier, GiftModifier> wanted = wanted(worn, faction);
    Registries.ATTRIBUTE
        .streamEntries()
        .forEach(
            attribute -> {
              final EntityAttributeInstance instance = wearer.getAttributeInstance(attribute);
              if (instance == null) {
                return;
              }
              for (final EntityAttributeModifier modifier : List.copyOf(instance.getModifiers())) {
                if (stray(modifier.id(), attribute, wanted)) {
                  instance.removeModifier(modifier.id());
                }
              }
            });
    wanted.forEach(
        (id, gift) -> Modifiers.set(wearer, gift.attribute(), id, gift.amount(), gift.operation()));
    Modifiers.clampHealth(wearer);
  }

  static Map<Identifier, GiftModifier> wanted(
      final Map<Identifier, Accessory> worn, final Faction wearer) {
    final Map<Identifier, GiftModifier> wanted = new HashMap<>();
    worn.forEach(
        (item, accessory) -> {
          if (accessory.worksFor(wearer)) {
            for (int i = 0; i < accessory.gifts().size(); i++) {
              wanted.put(modifierId(item, i), accessory.gifts().get(i));
            }
          }
        });
    return wanted;
  }

  static Identifier modifierId(final Identifier item, final int index) {
    return Identifier.of(
        Nightborn.MOD_ID,
        MODIFIER_PREFIX + item.getNamespace() + "/" + item.getPath() + "/" + index);
  }

  static boolean stray(
      final Identifier modifier,
      final RegistryEntry<EntityAttribute> on,
      final Map<Identifier, GiftModifier> wanted) {
    if (!modifier.getNamespace().equals(Nightborn.MOD_ID)
        || !modifier.getPath().startsWith(MODIFIER_PREFIX)) {
      return false;
    }
    final GiftModifier keep = wanted.get(modifier);
    return keep == null || !keep.attribute().equals(on);
  }
}
