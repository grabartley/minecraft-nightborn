package com.grahambartley.nightborn.client;

import com.grahambartley.nightborn.coating.Coat;
import com.grahambartley.nightborn.coating.Coatings;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public final class LiveCoating {

  private LiveCoating() {}

  public static Optional<Coat> of(final ItemStack stack) {
    return stack.contains(Coatings.COAT)
        ? Coatings.live(stack, SyncedServerConfig.get(), now())
        : Optional.empty();
  }

  static long now() {
    final MinecraftClient client = MinecraftClient.getInstance();
    return client.world == null ? 0L : client.world.getTime();
  }
}
