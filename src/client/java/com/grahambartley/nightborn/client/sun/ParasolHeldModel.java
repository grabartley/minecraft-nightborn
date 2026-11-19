package com.grahambartley.nightborn.client.sun;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.sun.ParasolItems;
import java.util.Set;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public final class ParasolHeldModel {

  static final Identifier IN_HAND = Identifier.of(Nightborn.MOD_ID, "item/parasol_in_hand");
  static final Set<ModelTransformationMode> FLAT =
      Set.of(
          ModelTransformationMode.GUI,
          ModelTransformationMode.GROUND,
          ModelTransformationMode.FIXED);

  private ParasolHeldModel() {}

  public static void register() {
    ModelLoadingPlugin.register(context -> context.addModels(IN_HAND));
  }

  public static BakedModel modelFor(
      final ItemStack stack, final ModelTransformationMode mode, final BakedModel model) {
    if (!stack.isOf(ParasolItems.PARASOL) || !drawsInHand(mode)) {
      return model;
    }
    return MinecraftClient.getInstance().getBakedModelManager().getModel(IN_HAND);
  }

  static boolean drawsInHand(final ModelTransformationMode mode) {
    return !FLAT.contains(mode);
  }
}
