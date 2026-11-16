package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.config.StructureSettings;
import com.mojang.serialization.Codec;
import java.util.function.ToIntFunction;
import net.minecraft.util.StringIdentifiable;

public enum StructureRarity implements StringIdentifiable {
  VAMPIRE_STRUCTURES("vampire_structures", StructureSettings::structureRarity),
  HUNTER_CAMPS("hunter_camps", StructureSettings::campRarity);

  public static final Codec<StructureRarity> CODEC =
      StringIdentifiable.createCodec(StructureRarity::values);

  private final String id;
  private final ToIntFunction<StructureSettings> setting;

  StructureRarity(final String id, final ToIntFunction<StructureSettings> setting) {
    this.id = id;
    this.setting = setting;
  }

  public int percent(final StructureSettings settings) {
    return setting.applyAsInt(settings);
  }

  @Override
  public String asString() {
    return id;
  }
}
