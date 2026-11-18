package com.grahambartley.nightborn;

import com.grahambartley.nightborn.accessory.AccessoryFeatures;
import com.grahambartley.nightborn.ammo.AmmoItems;
import com.grahambartley.nightborn.ammo.GarlicFumesEffect;
import com.grahambartley.nightborn.clothing.ClothingFeatures;
import com.grahambartley.nightborn.coffin.Coffins;
import com.grahambartley.nightborn.decor.DecorBlockEntities;
import com.grahambartley.nightborn.decor.DecorBlocks;
import com.grahambartley.nightborn.decor.DecorEntities;
import com.grahambartley.nightborn.enchant.EnchantFeatures;
import com.grahambartley.nightborn.gear.GearBlocks;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.guild.ContractItems;
import com.grahambartley.nightborn.guild.GuildMobs;
import com.grahambartley.nightborn.holy.HolyWater;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.journal.JournalItems;
import com.grahambartley.nightborn.mind.MindAttributes;
import com.grahambartley.nightborn.mind.MindEffect;
import com.grahambartley.nightborn.mob.NightbornMobs;
import com.grahambartley.nightborn.occult.OccultAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public final class MinecraftBootstrapExtension implements BeforeAllCallback {

  private static final AtomicBoolean INITIALIZED = new AtomicBoolean(false);

  @Override
  public void beforeAll(final ExtensionContext context) {
    if (!INITIALIZED.compareAndSet(false, true)) {
      return;
    }
    SharedConstants.createGameVersion();
    Bootstrap.initialize();
    DecorBlocks.initialize();
    DecorBlockEntities.initialize();
    Coffins.initialize();
    DecorEntities.initialize();
    HunterAttributes.register();
    MindAttributes.register();
    OccultAttributes.register();
    MindEffect.register();
    GearBlocks.initialize();
    GearItems.register();
    EnchantFeatures.register();
    ClothingFeatures.register();
    AccessoryFeatures.register();
    AmmoItems.register();
    Objects.requireNonNull(HolyWater.POTION);
    Objects.requireNonNull(ContractItems.HUNTER_CONTRACT);
    Objects.requireNonNull(JournalItems.HUNTER_JOURNAL);
    Objects.requireNonNull(NightbornMobs.VAMPIRE_BAT);
    Objects.requireNonNull(GuildMobs.HUNTER);
    Objects.requireNonNull(GarlicFumesEffect.ENTRY);
  }
}
