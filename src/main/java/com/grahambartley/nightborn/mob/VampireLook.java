package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.util.Identifier;

public record VampireLook(Rig rig, int costume) {

  public enum Rig {
    MALE("male"),
    FEMALE("female");

    private final String id;

    Rig(final String id) {
      this.id = id;
    }

    public String id() {
      return id;
    }
  }

  public static final int COSTUMES_PER_RIG = 3;
  public static final int COUNT = Rig.values().length * COSTUMES_PER_RIG;

  public static VampireLook of(final int index) {
    final int safe = Math.floorMod(index, COUNT);
    return new VampireLook(Rig.values()[safe / COSTUMES_PER_RIG], safe % COSTUMES_PER_RIG);
  }

  public int index() {
    return rig.ordinal() * COSTUMES_PER_RIG + costume;
  }

  public Identifier geo() {
    return Identifier.of(Nightborn.MOD_ID, "geo/entity/vampire_" + rig.id() + ".geo.json");
  }

  public Identifier texture() {
    return Identifier.of(
        Nightborn.MOD_ID, "textures/entity/vampire/" + rig.id() + "_" + costume + ".png");
  }

  public Identifier eyes() {
    return Identifier.of(Nightborn.MOD_ID, "textures/entity/vampire/" + rig.id() + "_eyes.png");
  }

  public static Identifier animation() {
    return Identifier.of(Nightborn.MOD_ID, "animations/entity/vampire.animation.json");
  }
}
