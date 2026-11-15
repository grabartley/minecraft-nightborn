package com.grahambartley.nightborn.ward;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.gear.GearBlocks;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PointOfInterestHelper;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.poi.PointOfInterestType;

public final class WardPoints {

  public static final Identifier GARLIC_STRING_ID =
      Identifier.of(Nightborn.MOD_ID, "garlic_string");
  public static final RegistryKey<PointOfInterestType> GARLIC_STRING =
      RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, GARLIC_STRING_ID);

  private WardPoints() {}

  public static void register() {
    PointOfInterestHelper.register(GARLIC_STRING_ID, 0, 1, GearBlocks.GARLIC_STRING);
  }
}
