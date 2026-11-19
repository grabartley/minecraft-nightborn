package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.Nightborn;
import java.util.Locale;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ParasolScorch {

  enum Stage {
    FRESH(0.0),
    SINGED(0.35),
    SCORCHED(0.65),
    CHARRED(0.9);

    private final double from;

    Stage(final double from) {
      this.from = from;
    }
  }

  public static final Identifier FRESH = texture(Stage.FRESH);

  private ParasolScorch() {}

  public static Identifier texture(final ItemStack stack) {
    return texture(stack.getDamage(), stack.getMaxDamage());
  }

  static Identifier texture(final int damage, final int maxDamage) {
    return texture(stage(damage, maxDamage));
  }

  static Stage stage(final int damage, final int maxDamage) {
    final double worn = maxDamage <= 0 ? 0.0 : (double) damage / maxDamage;
    Stage reached = Stage.FRESH;
    for (final Stage stage : Stage.values()) {
      if (worn >= stage.from) {
        reached = stage;
      }
    }
    return reached;
  }

  private static Identifier texture(final Stage stage) {
    return Identifier.of(
        Nightborn.MOD_ID,
        "textures/entity/parasol/" + stage.name().toLowerCase(Locale.ROOT) + ".png");
  }
}
