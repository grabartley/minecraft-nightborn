package com.grahambartley.nightborn.tools.structures.vampire.crypt;

import com.grahambartley.nightborn.tools.structures.template.Template;
import com.grahambartley.nightborn.tools.structures.vampire.Stone;

public final class Churchyard {

  public static final int DEPTH = CryptStair.SIZE_Y;
  public static final int YARD_X = Church.SIZE_X;
  public static final int YARD_Z = Church.YARD_Z;
  static final Stone RETAINING = new Stone("cobblestone", "mossy_cobblestone", "andesite", 45);
  static final int TOPSOIL = 2;

  private Churchyard() {}

  public static Template bank(final Template crypt) {
    final Template t = new Template(crypt.path(), YARD_X, DEPTH, YARD_Z);
    for (int x = 0; x < YARD_X; x++) {
      for (int z = 0; z < YARD_Z; z++) {
        final boolean edge = x == 0 || z == 0 || x == YARD_X - 1 || z == YARD_Z - 1;
        for (int y = 0; y < DEPTH; y++) {
          if (edge) {
            t.set(x, y, z, RETAINING.at(x, y, z));
          } else {
            t.set(x, y, z, y >= DEPTH - TOPSOIL ? "dirt" : "stone");
          }
        }
      }
    }
    t.embed(crypt, CryptStair.OFFSET_X, 0, CryptStair.OFFSET_Z);
    return t;
  }
}
