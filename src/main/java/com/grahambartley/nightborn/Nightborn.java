package com.grahambartley.nightborn;

import com.grahambartley.nightborn.accessory.AccessoryFeatures;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.challenge.ChallengeFeatures;
import com.grahambartley.nightborn.clothing.ClothingFeatures;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.decor.DecorItemGroup;
import com.grahambartley.nightborn.enchant.EnchantFeatures;
import com.grahambartley.nightborn.faction.FactionAttachment;
import com.grahambartley.nightborn.faction.OathAttachment;
import com.grahambartley.nightborn.familiar.Familiars;
import com.grahambartley.nightborn.gear.GearFeatures;
import com.grahambartley.nightborn.gramophone.MusicDiscs;
import com.grahambartley.nightborn.guild.GuildFeatures;
import com.grahambartley.nightborn.house.HouseFeatures;
import com.grahambartley.nightborn.hunt.HuntFeatures;
import com.grahambartley.nightborn.hunter.HunterFeatures;
import com.grahambartley.nightborn.inquisitor.InquisitorFeatures;
import com.grahambartley.nightborn.journal.Journals;
import com.grahambartley.nightborn.mind.MindFeatures;
import com.grahambartley.nightborn.mirror.MirrorItems;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.name.NameFeatures;
import com.grahambartley.nightborn.occult.OccultFeatures;
import com.grahambartley.nightborn.raid.RaidFeatures;
import com.grahambartley.nightborn.rank.RankFeatures;
import com.grahambartley.nightborn.ritual.RitualFeatures;
import com.grahambartley.nightborn.servant.ServantFeatures;
import com.grahambartley.nightborn.sound.NightbornSounds;
import com.grahambartley.nightborn.structure.NightbornStructures;
import com.grahambartley.nightborn.sun.SunShafts;
import com.grahambartley.nightborn.vampire.VampireFeatures;
import com.grahambartley.nightborn.village.VillageFeatures;
import com.grahambartley.nightborn.wheel.CommandWheel;
import com.grahambartley.nightborn.wild.WildFeatures;
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
    OathAttachment.register();
    Moments.register();
    DecorBlocks.initialize();
    DecorBlockEntities.initialize();
    MusicDiscs.initialize();
    Coffins.initialize();
    DecorEntities.initialize();
    DecorItemGroup.initialize();
    VampireFeatures.register();
    NameFeatures.register();
    CommandWheel.register();
    ServantFeatures.register();
    NightbornMobs.initialize();
    Familiars.initialize();
    GearFeatures.register();
    MirrorItems.register();
    HunterFeatures.register();
    GuildFeatures.register();
    Journals.register();
    RankFeatures.register();
    HuntFeatures.register();
    RaidFeatures.register();
    InquisitorFeatures.register();
    SunShafts.register();
    MindFeatures.register();
    OccultFeatures.register();
    EnchantFeatures.register();
    ClothingFeatures.register();
    AccessoryFeatures.register();
    NightbornStructures.initialize();
    RitualFeatures.register();
    HouseFeatures.register();
    VillageFeatures.register();
    WildFeatures.register();
    ChallengeFeatures.register();
    LOGGER.info("Nightborn initialised");
  }
}
