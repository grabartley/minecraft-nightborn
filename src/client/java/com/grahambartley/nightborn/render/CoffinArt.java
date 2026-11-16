package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.coffin.CoffinStyle;
import java.util.Optional;
import net.minecraft.util.Identifier;

public final class CoffinArt {

  public static final Identifier GEO = Identifier.of(Nightborn.MOD_ID, "geo/block/coffin.geo.json");
  public static final Identifier ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/block/coffin.animation.json");
  public static final Identifier STONE_GEO =
      Identifier.of(Nightborn.MOD_ID, "geo/block/stone_coffin.geo.json");
  public static final Identifier STONE_ANIMATION =
      Identifier.of(Nightborn.MOD_ID, "animations/block/stone_coffin.animation.json");
  static final Identifier VELVET =
      Identifier.of(Nightborn.MOD_ID, "textures/block/burgundy_velvet.png");
  static final Identifier FITTINGS = Identifier.ofVanilla("textures/block/raw_gold_block.png");
  public static final float MODEL_CENTRE_Z = 4.0F / 16.0F;

  private static final String WOOD = "_wood";
  private static final String STONE = "_stone";
  private static final String LINING = "_velvet";
  private static final String GOLD = "_gold";

  private CoffinArt() {}

  public static Identifier geo(final CoffinStyle style) {
    return style == CoffinStyle.STONE ? STONE_GEO : GEO;
  }

  public static Identifier animation(final CoffinStyle style) {
    return style == CoffinStyle.STONE ? STONE_ANIMATION : ANIMATION;
  }

  public static Identifier texture(final Identifier sourceBlock) {
    return Identifier.of(
        sourceBlock.getNamespace(), "textures/block/" + sourceBlock.getPath() + ".png");
  }

  public static Optional<Identifier> texture(final String bone, final Identifier material) {
    if (bone.endsWith(WOOD) || bone.endsWith(STONE)) {
      return Optional.of(material);
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
