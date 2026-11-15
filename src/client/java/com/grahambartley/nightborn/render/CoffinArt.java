package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class CoffinArt {

  public static final Identifier GEO = Identifier.of(Nightborn.MOD_ID, "geo/block/coffin.geo.json");
  public static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/block/coffin.animation.json");
  static final Identifier VELVET =
      Identifier.of(Nightborn.MOD_ID, "textures/block/burgundy_velvet.png");
  static final Identifier FITTINGS = Identifier.ofVanilla("textures/block/raw_gold_block.png");
  static final float MODEL_CENTRE_Z = 4.0F / 16.0F;

  private static final String WOOD = "_wood";
  private static final String LINING = "_velvet";
  private static final String GOLD = "_gold";

  private CoffinArt() {}

  public static Identifier planks(final Identifier planksBlock) {
    return Identifier.of(
        planksBlock.getNamespace(), "textures/block/" + planksBlock.getPath() + ".png");
  }

  public static Optional<Identifier> texture(final String bone, final Identifier wood) {
    if (bone.endsWith(WOOD)) {
      return Optional.of(wood);
    }
    if (bone.endsWith(LINING)) {
      return Optional.of(VELVET);
    }
    if (bone.endsWith(GOLD)) {
      return Optional.of(FITTINGS);
    }
    return Optional.empty();
  }
}
