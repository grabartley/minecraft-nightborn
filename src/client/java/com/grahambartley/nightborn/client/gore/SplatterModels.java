package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.decor.DecorBlocks;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;

public final class SplatterModels implements ModelLoadingPlugin {

  private static final String INVENTORY_VARIANT = "inventory";

  public static void register() {
    ModelLoadingPlugin.register(new SplatterModels());
  }

  @Override
  public void onInitializeModelLoader(final Context context) {
    context.modifyModelAfterBake().register(SplatterModels::wrapped);
  }

  private static BakedModel wrapped(
      final BakedModel model, final ModelModifier.AfterBake.Context context) {
    final ModelIdentifier topLevel = context.topLevelId();
    if (model == null
        || topLevel == null
        || INVENTORY_VARIANT.equals(topLevel.variant())
        || !Registries.BLOCK.getId(DecorBlocks.BLOOD_SPLATTER).equals(topLevel.id())) {
      return model;
    }
    return new GoreSplatterModel(model);
  }
}
