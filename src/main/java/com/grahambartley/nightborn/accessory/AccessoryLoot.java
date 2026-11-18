package com.grahambartley.nightborn.accessory;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class AccessoryLoot {

  public static final LootFunctionType<PickUnownedLootFunction> PICK_UNOWNED =
      Registry.register(
          Registries.LOOT_FUNCTION_TYPE,
          Identifier.of(Nightborn.MOD_ID, "pick_unowned"),
          new LootFunctionType<>(PickUnownedLootFunction.CODEC));

  private AccessoryLoot() {}

  public static void register() {}
}
