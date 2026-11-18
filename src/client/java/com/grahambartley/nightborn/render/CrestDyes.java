package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.house.crest.Crest;
import java.util.List;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public final class CrestDyes {

  static final int OPAQUE = 0xFF000000;
  static final int UNTINTED = 0xFFFFFFFF;

  public record Dye(Identifier texture, int colour) {}

  private CrestDyes() {}

  public static List<Dye> of(final Crest crest) {
    final String charge = "textures/block/crest_charge/" + crest.charge().asString();
    return List.of(
        new Dye(texture("textures/block/crest_banner_field.png"), tint(crest.field())),
        new Dye(texture("textures/block/crest_banner_bordure.png"), tint(crest.tincture())),
        new Dye(texture(charge + ".png"), tint(crest.tincture())),
        new Dye(texture(charge + "_outline.png"), UNTINTED));
  }

  static int tint(final DyeColor dye) {
    return OPAQUE | dye.getEntityColor();
  }

  private static Identifier texture(final String path) {
    return Identifier.of(Nightborn.MOD_ID, path);
  }
}
