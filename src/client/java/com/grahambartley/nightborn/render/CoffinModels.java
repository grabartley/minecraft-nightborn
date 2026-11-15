package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.Coffins;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class CoffinModels {

  private CoffinModels() {}

  public static Identifier lid(final Identifier coffin) {
    return Identifier.of(coffin.getNamespace(), "block/" + coffin.getPath() + "_lid");
  }

  public static void register() {
    ModelLoadingPlugin.register(
        context ->
            context.addModels(
                Coffins.all().stream()
                    .map(Registries.BLOCK::getId)
                    .map(CoffinModels::lid)
                    .toList()));
  }
}
