package com.grahambartley.nightborn.client.gramophone;

import com.grahambartley.nightborn.gramophone.GramophoneBlock;
import com.grahambartley.nightborn.mixin.client.WorldRendererAccessor;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public final class GramophoneMusic {

  private GramophoneMusic() {}

  public static boolean playing(final MinecraftClient client) {
    if (client.world == null || client.player == null) {
      return false;
    }
    final List<BlockPos> gramophones =
        ((WorldRendererAccessor) client.worldRenderer)
            .nightborn$playingSongs().entrySet().stream()
                .filter(song -> client.getSoundManager().isPlaying(song.getValue()))
                .map(song -> song.getKey())
                .filter(
                    pos -> client.world.getBlockState(pos).getBlock() instanceof GramophoneBlock)
                .toList();
    return GramophoneEarshot.hears(gramophones, client.player.getPos());
  }
}
