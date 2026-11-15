package com.grahambartley.nightborn.coating;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.NightbornConfig;
import java.util.Optional;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class Coatings {

  public static final ComponentType<Coat> COAT =
      Registry.register(
          Registries.DATA_COMPONENT_TYPE,
          Identifier.of(Nightborn.MOD_ID, "coating"),
          ComponentType.<Coat>builder().codec(Coat.CODEC).packetCodec(Coat.PACKET_CODEC).build());

  public static final RecipeSerializer<CoatingRecipe> RECIPE =
      Registry.register(
          Registries.RECIPE_SERIALIZER,
          Identifier.of(Nightborn.MOD_ID, "coating"),
          new SpecialRecipeSerializer<>(CoatingRecipe::new));

  public static final TagKey<Item> WEAPONS = tag("coatable_weapons");
  public static final TagKey<Item> AMMO = tag("coatable_ammo");
  public static final TagKey<DamageType> WEAPON_STRIKES =
      TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "coated_weapon_strikes"));

  private Coatings() {}

  public static void register() {
    ServerLivingEntityEvents.AFTER_DAMAGE.register(CoatedStrikes::afterDamage);
    ServerTickEvents.END_SERVER_TICK.register(CoatingClock::tick);
  }

  public static Optional<Coat> of(final ItemStack stack) {
    return Optional.ofNullable(stack.get(COAT));
  }

  public static Optional<Coat> live(
      final ItemStack stack, final NightbornConfig config, final long now) {
    return of(stack)
        .filter(coat -> coat.live(CoatingTerms.of(coat.coating(), config), now, isAmmo(stack)));
  }

  public static boolean isAmmo(final ItemStack stack) {
    return stack.isIn(AMMO);
  }

  public static boolean isCoatedAmmo(final ItemStack stack) {
    return stack.contains(COAT) && isAmmo(stack);
  }

  public static boolean isCoatedWeapon(final ItemStack stack) {
    return stack.contains(COAT) && stack.isIn(WEAPONS);
  }

  private static TagKey<Item> tag(final String path) {
    return TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, path));
  }
}
