package com.grahambartley.nightborn.holy;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.config.GearSettings;
import com.grahambartley.nightborn.gear.GearItems;
import com.grahambartley.nightborn.hunter.FaithBoost;
import com.grahambartley.nightborn.hunter.HunterAttributes;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.sickness.BiteSickness;
import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.weakened.Weakened;
import java.util.Optional;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradedItem;
import net.minecraft.village.VillagerProfession;
import org.jetbrains.annotations.Nullable;

public final class HolyWater {

  public static final String WEAKENED_SOURCE = "holy_water";

  public static final RegistryKey<DamageType> DAMAGE =
      RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(Nightborn.MOD_ID, "holy_water"));

  public static final RegistryEntry<StatusEffect> BLESSING =
      Registry.registerReference(
          Registries.STATUS_EFFECT,
          Identifier.of(Nightborn.MOD_ID, "blessing"),
          new BlessingEffect());

  public static final RegistryEntry<Potion> POTION =
      Registry.registerReference(
          Registries.POTION,
          Identifier.of(Nightborn.MOD_ID, "holy_water"),
          new Potion(Nightborn.MOD_ID + ".holy_water", new StatusEffectInstance(BLESSING, 1)));

  static final int BLESSING_TRADE_LEVEL = 2;
  static final int BLESSING_PRICE = 1;
  static final int BLESSING_USES = 12;
  static final int BLESSING_XP = 5;
  static final float BLESSING_PRICE_MULTIPLIER = 0.05F;
  static final int SIZZLE_PARTICLES = 12;

  private HolyWater() {}

  public static void register() {
    FabricBrewingRecipeRegistryBuilder.BUILD.register(
        builder -> builder.registerPotionRecipe(Potions.WATER, GearItems.SILVER_NUGGET, POTION));
    TradeOfferHelper.registerVillagerOffers(
        VillagerProfession.CLERIC,
        BLESSING_TRADE_LEVEL,
        factories -> factories.add((merchant, random) -> blessing()));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
        .register(
            entries -> {
              entries.getDisplayStacks().removeIf(HolyWater::isHolyArrow);
              entries.getSearchTabStacks().removeIf(HolyWater::isHolyArrow);
            });
  }

  public static boolean isHolyWater(final ItemStack stack) {
    return stack
        .getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT)
        .matches(POTION);
  }

  public static ItemStack bottle() {
    return PotionContentsComponent.createStack(Items.POTION, POTION);
  }

  static TradeOffer blessing() {
    return new TradeOffer(
        new TradedItem(Items.EMERALD, BLESSING_PRICE),
        Optional.of(
            new TradedItem(Items.POTION)
                .withComponents(
                    components ->
                        components.add(
                            DataComponentTypes.POTION_CONTENTS,
                            new PotionContentsComponent(Potions.WATER)))),
        bottle(),
        BLESSING_USES,
        BLESSING_XP,
        BLESSING_PRICE_MULTIPLIER);
  }

  static void bless(
      @Nullable final Entity source,
      @Nullable final Entity attacker,
      final LivingEntity target,
      final double proximity) {
    if (!(target.getWorld() instanceof ServerWorld world)) {
      return;
    }
    if (Vampires.isVampire(target)) {
      scald(world, source, attacker, target, proximity);
    } else if (source == target && target instanceof ServerPlayerEntity drinker) {
      BiteSickness.cure(drinker);
    }
  }

  private static void scald(
      final ServerWorld world,
      @Nullable final Entity source,
      @Nullable final Entity attacker,
      final LivingEntity vampire,
      final double proximity) {
    final GearSettings settings = ServerConfigService.current().gear();
    final HolyWaterDose dose =
        HolyWaterDose.of(
            settings,
            proximity,
            FaithBoost.multiplier(
                HunterAttributes.faith(attacker),
                ServerConfigService.current().wards().faithBonus()));
    if (dose.isEmpty()) {
      return;
    }
    Weakened.add(vampire, WEAKENED_SOURCE, dose.strength(), dose.ticks());
    if (dose.damage() > 0.0F) {
      vampire.damage(
          new DamageSource(
              world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE),
              source == vampire ? null : source,
              attacker == vampire ? null : attacker),
          dose.damage());
    }
    sizzle(world, vampire);
  }

  public static void sizzle(final ServerWorld world, final Entity at) {
    world.spawnParticles(
        ParticleTypes.WHITE_SMOKE,
        at.getX(),
        at.getBodyY(0.6),
        at.getZ(),
        SIZZLE_PARTICLES,
        at.getWidth() / 2.0,
        at.getHeight() / 4.0,
        at.getWidth() / 2.0,
        0.02);
    world.playSound(
        null,
        at.getBlockPos(),
        SoundEvents.BLOCK_FIRE_EXTINGUISH,
        SoundCategory.NEUTRAL,
        0.6F,
        1.4F + world.random.nextFloat() * 0.3F);
  }

  private static boolean isHolyArrow(final ItemStack stack) {
    return stack.isOf(Items.TIPPED_ARROW) && isHolyWater(stack);
  }
}
