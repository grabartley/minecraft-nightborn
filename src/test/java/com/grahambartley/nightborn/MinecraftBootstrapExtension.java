package com.grahambartley.nightborn;

import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

  @Override
  public void beforeAll(final ExtensionContext context) {
    if (!INITIALIZED.compareAndSet(false, true)) {
      return;
    }
    SharedConstants.createGameVersion();
    Bootstrap.initialize();
    DecorBlocks.initialize();
    DecorBlockEntities.initialize();
    Coffins.initialize();
    DecorEntities.initialize();
    HunterAttributes.register();
  }
}
