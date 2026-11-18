package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.servant.ChestMarking;
import com.grahambartley.nightborn.servant.ServantChests;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class Familiars {

  public static final EntityType<HumanFamiliarEntity> HUMAN_FAMILIAR =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "human_familiar"),
          EntityType.Builder.create(HumanFamiliarEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.62F)
              .maxTrackingRange(10)
              .build("human_familiar"));

  public static final Item CONTRACT =
      Registry.register(
          Registries.ITEM,
          Identifier.of(Nightborn.MOD_ID, "familiars_contract"),
          new Item(new Item.Settings().maxCount(16)));

  private Familiars() {}

  public static void initialize() {
    FabricDefaultAttributeRegistry.register(
        HUMAN_FAMILIAR, HumanFamiliarEntity.createFamiliarAttributes());
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(CONTRACT));
    FamiliarHiring.register();
    ChestMarking.countServants(
        (server, owner) -> FamiliarRegistry.of(server).bondOf(owner).isPresent());
    ServerLifecycleEvents.SERVER_STARTED.register(
        server -> ServantChests.of(server).adopt(FamiliarRegistry.of(server).takeLegacyMarks()));
    FamiliarWheel.register();
  }
}
