package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.clothing.ClothingCut;
import net.minecraft.util.Identifier;

public final class ClothingArt {

  static final String DYED_BONE_PREFIX = "dyed";

  private ClothingArt() {}

  public static Identifier geo(final String id, final ClothingCut cut) {
    return id("geo/armor/" + id + "_" + cut.id() + ".geo.json");
  }

  public static Identifier texture(final String id, final ClothingCut cut) {
    return id("textures/armor/" + id + "_" + cut.id() + ".png");
  }

  public static Identifier animation(final String id) {
    return id("animations/armor/" + id + ".animation.json");
  }

  public static boolean isDyed(final String boneName) {
    return boneName.startsWith(DYED_BONE_PREFIX);
  }

  private static Identifier id(final String path) {
    return Identifier.of(Nightborn.MOD_ID, path);
  }
}
