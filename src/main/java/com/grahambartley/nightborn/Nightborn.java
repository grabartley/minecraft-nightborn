package com.grahambartley.nightborn;

import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.clothing.ClothingFeatures;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.decor.DecorItemGroup;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.familiar.Familiars;
import com.grahambartley.nightborn.gear.GearFeatures;
import com.grahambartley.nightborn.guild.GuildFeatures;
import com.grahambartley.nightborn.hunter.HunterFeatures;
import com.grahambartley.nightborn.mind.MindFeatures;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.occult.OccultFeatures;
import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.structure.NightbornStructures;
import com.grahambartley.nightborn.vampire.VampireFeatures;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Nightborn implements ModInitializer {

  public static final String MOD_ID = "nightborn";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  @Override
  public void onInitialize() {
    MilestoneCriterion.register();
    NightbornSounds.initialize();
    FactionAttachment.register();
    DecorBlocks.initialize();
    DecorBlockEntities.initialize();
    Coffins.initialize();
    DecorEntities.initialize();
    DecorItemGroup.initialize();
    VampireFeatures.register();
    NightbornMobs.initialize();
    Familiars.initialize();
    GearFeatures.register();
    HunterFeatures.register();
    GuildFeatures.register();
    MindFeatures.register();
    OccultFeatures.register();
    ClothingFeatures.register();
    NightbornStructures.initialize();
    LOGGER.info("Nightborn initialised");
  }
}
