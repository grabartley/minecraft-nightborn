package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.guild.build.PathPick;
import com.grahambartley.nightborn.rank.RankData;
import com.grahambartley.nightborn.skill.SkillTree;
import com.grahambartley.nightborn.skill.Skills;
import com.grahambartley.nightborn.ward.WardTags;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public final class GuildData {

  public static final RegistryKey<Registry<GuildOffer>> OFFER =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "guild_offer"));
  public static final RegistryKey<Registry<HunterOffer>> HUNTER_OFFER =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_offer"));
  public static final RegistryKey<Registry<HunterContract>> CONTRACT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_contract"));
  public static final RegistryKey<Registry<HunterLook>> LOOK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_look"));
  public static final RegistryKey<Registry<HunterKit>> KIT =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "hunter_kit"));

  public static final String LOOK_KEY = "Look";

  private GuildData() {}

  static void register() {
    DynamicRegistries.register(OFFER, GuildOffer.CODEC);
    DynamicRegistries.register(HUNTER_OFFER, HunterOffer.CODEC);
    DynamicRegistries.registerSynced(CONTRACT, HunterContract.CODEC);
    DynamicRegistries.registerSynced(LOOK, HunterLook.CODEC);
    DynamicRegistries.register(KIT, HunterKit.CODEC);
    ServerLifecycleEvents.SERVER_STARTED.register(server -> check(server.getRegistryManager()));
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
        (server, resources, success) -> check(server.getRegistryManager()));
  }

  private static void check(final DynamicRegistryManager registries) {
    HunterDataCheck.problems(
            looks(registries),
            kits(registries),
            id -> item(id).isPresent(),
            id -> item(id).filter(CrossbowItem.class::isInstance).isPresent(),
            id -> item(id).filter(i -> i.getDefaultStack().isIn(WardTags.CRUCIFIXES)).isPresent())
        .forEach(problem -> Nightborn.LOGGER.error("Bad hunter data: {}", problem));
    HunterOfferCheck.problems(
            hunterOffers(registries),
            Set.copyOf(PathPick.paths(Skills.allBranches(registries), SkillTree.HUNTER)),
            RankData.ranks(registries).keySet(),
            offers(registries),
            stack -> stack.contains(ContractItems.PROGRESS))
        .forEach(problem -> Nightborn.LOGGER.error("Bad hunter offer: {}", problem));
  }

  public static List<GuildOffer> offers(final DynamicRegistryManager registries) {
    return GuildOffer.inOrder(copy(registries, OFFER));
  }

  public static Map<Identifier, HunterOffer> hunterOffers(final DynamicRegistryManager registries) {
    return copy(registries, HUNTER_OFFER);
  }

  public static Map<Identifier, HunterLook> looks(final DynamicRegistryManager registries) {
    return copy(registries, LOOK);
  }

  public static Map<Identifier, HunterKit> kits(final DynamicRegistryManager registries) {
    return copy(registries, KIT);
  }

  public static String wornLook(
      final String saved,
      final DynamicRegistryManager registries,
      final HunterRig rig,
      final Random random) {
    return saved.isEmpty() ? pickLook(registries, rig, random) : saved;
  }

  public static String pickLook(
      final DynamicRegistryManager registries, final HunterRig rig, final Random random) {
    return HunterLook.pick(looks(registries), rig, random.nextDouble())
        .map(Identifier::toString)
        .orElse("");
  }

  public static Identifier texture(
      final DynamicRegistryManager registries, final String lookId, final HunterRig rig) {
    return HunterLook.texture(
        looks(registries), Optional.ofNullable(Identifier.tryParse(lookId)), rig);
  }

  public static Optional<Item> item(final Identifier id) {
    return Registries.ITEM.getOrEmpty(id);
  }

  public static Optional<HunterContract> contract(
      final RegistryWrapper.WrapperLookup registries, final Identifier id) {
    return registries
        .getOptionalWrapper(CONTRACT)
        .flatMap(wrapper -> wrapper.getOptional(RegistryKey.of(CONTRACT, id)))
        .map(entry -> entry.value());
  }

  private static <T> Map<Identifier, T> copy(
      final DynamicRegistryManager registries, final RegistryKey<Registry<T>> key) {
    final Map<Identifier, T> entries = new HashMap<>();
    registries
        .getOptional(key)
        .ifPresent(registry -> registry.getIds().forEach(id -> entries.put(id, registry.get(id))));
    return entries;
  }
}
