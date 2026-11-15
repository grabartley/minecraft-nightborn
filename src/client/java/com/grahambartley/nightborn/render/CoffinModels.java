package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.Coffins;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class CoffinModels {

  private static Map<Block, Identifier> lids = Map.of();

  private CoffinModels() {}

  public static Identifier lid(final Identifier coffin) {
    return Identifier.of(coffin.getNamespace(), "block/" + coffin.getPath() + "_lid");
  }

  public static Identifier lidOf(final Block coffin) {
    return lids.get(coffin);
  }

  public static void register() {
    lids =
        Coffins.all().stream()
            .collect(
                Collectors.toUnmodifiableMap(
                    Function.identity(), block -> lid(Registries.BLOCK.getId(block))));
    ModelLoadingPlugin.register(context -> context.addModels(lids.values()));
  }
}
