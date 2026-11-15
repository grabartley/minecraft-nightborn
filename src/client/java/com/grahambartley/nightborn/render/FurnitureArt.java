package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.util.Identifier;

public final class FurnitureArt {

  private FurnitureArt() {}

  public static Identifier geo(final String artModel) {
    return id("geo/block/" + artModel + ".geo.json");
  }

  public static Identifier animation(final String artModel) {
    return id("animations/block/" + artModel + ".animation.json");
  }

  public static Identifier id(final String path) {
    return Identifier.of(Nightborn.MOD_ID, path);
  }

  public static Identifier texture(final Identifier blockId) {
    return Identifier.of(blockId.getNamespace(), "textures/block/" + blockId.getPath() + ".png");
  }
}
