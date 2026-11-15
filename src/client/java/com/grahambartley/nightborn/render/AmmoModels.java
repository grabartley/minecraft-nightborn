package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.holy.HolyWater;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class AmmoModels {

  static final Identifier CROSSBOW = Identifier.ofVanilla("crossbow");
  static final Map<Identifier, Identifier> HOLY_WATER =
      Map.of(
          Identifier.ofVanilla("potion"), model("holy_water"),
          Identifier.ofVanilla("splash_potion"), model("splash_holy_water"),
          Identifier.ofVanilla("lingering_potion"), model("lingering_holy_water"));

  private static List<Identifier> ammo = List.of();

  private AmmoModels() {}

  public static void register() {
    ammo = AmmoItems.ALL.stream().map(Registries.ITEM::getId).toList();
    ModelLoadingPlugin.register(context -> context.addModels(all(ammo)));
  }

  public static Optional<Identifier> modelFor(final ItemStack stack) {
    return modelFor(
        Registries.ITEM.getId(stack.getItem()),
        HolyWater.isHolyWater(stack),
        stack
            .getOrDefault(
                DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT)
            .getProjectiles()
            .stream()
            .map(loaded -> Registries.ITEM.getId(loaded.getItem()))
            .toList(),
        ammo);
  }

  static Optional<Identifier> modelFor(
      final Identifier item,
      final boolean holyWater,
      final List<Identifier> loaded,
      final Collection<Identifier> ammo) {
    if (HOLY_WATER.containsKey(item)) {
      return holyWater ? Optional.of(HOLY_WATER.get(item)) : Optional.empty();
    }
    if (!item.equals(CROSSBOW)) {
      return Optional.empty();
    }
    return loaded.stream().filter(ammo::contains).findFirst().map(AmmoModels::loadedCrossbow);
  }

  static List<Identifier> all(final Collection<Identifier> ammo) {
    return Stream.concat(
            HOLY_WATER.values().stream(), ammo.stream().map(AmmoModels::loadedCrossbow))
        .toList();
  }

  static Identifier loadedCrossbow(final Identifier ammo) {
    return Identifier.of(ammo.getNamespace(), "item/crossbow_" + ammo.getPath());
  }

  private static Identifier model(final String path) {
    return Identifier.of(Nightborn.MOD_ID, "item/" + path);
  }
}
