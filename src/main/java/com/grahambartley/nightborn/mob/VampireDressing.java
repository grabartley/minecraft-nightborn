package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.mob.look.Appearance;
import com.grahambartley.nightborn.mob.look.OutfitDressing;
import com.grahambartley.nightborn.mob.look.VampireLooks;
import com.grahambartley.nightborn.mob.look.VampireOutfit;
import java.util.Map;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;

final class VampireDressing {

  private VampireDressing() {}

  static void dress(final VampireEntity vampire, final DynamicRegistryManager registries) {
    final Map<Identifier, VampireOutfit> outfits = VampireLooks.outfits(registries);
    Appearance.pick(
            VampireLooks.looks(registries),
            outfits,
            VampireLooks.builds(registries),
            vampire.getRandom()::nextDouble)
        .ifPresent(
            appearance -> {
              vampire.setAppearance(appearance.look(), appearance.height(), appearance.width());
              vampire.shapeBust(vampire.getRandom().nextDouble());
              appearance
                  .outfit()
                  .map(outfits::get)
                  .map(outfit -> OutfitDressing.stacks(outfit, VampireLooks::item))
                  .ifPresent(stacks -> stacks.forEach(vampire::equipStack));
            });
  }
}
