package com.grahambartley.nightborn.lord;

import com.grahambartley.nightborn.downed.DownedState;
import java.util.Optional;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public record BossBarLook(BossBar.Color color, Optional<String> state) {

  static final String DOWNED_KEY = "nightborn.boss.downed";
  static final String ESCAPING_KEY = "nightborn.boss.escaping";

  public static BossBarLook of(@Nullable final DownedState.Phase phase) {
    if (phase == null) {
      return new BossBarLook(BossBar.Color.RED, Optional.empty());
    }
    return switch (phase) {
      case DOWNED -> new BossBarLook(BossBar.Color.WHITE, Optional.of(DOWNED_KEY));
      case ESCAPING -> new BossBarLook(BossBar.Color.PURPLE, Optional.of(ESCAPING_KEY));
    };
  }

  public Text title(final Text name) {
    return state.<Text>map(key -> Text.translatable(key, name)).orElse(name);
  }

  public static float percent(final float health, final float maxHealth) {
    return maxHealth <= 0.0F ? 0.0F : MathHelper.clamp(health / maxHealth, 0.0F, 1.0F);
  }
}
