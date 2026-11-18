package com.grahambartley.nightborn.wheel;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.moment.Moments;
import com.grahambartley.nightborn.network.ChooseWheelPayload;
import com.grahambartley.nightborn.network.OpenWheelPayload;
import com.grahambartley.nightborn.skill.SkillTree;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public final class CommandWheel {

  public static final TagKey<Item> KEEPS_OWN_USE =
      TagKey.of(RegistryKeys.ITEM, Identifier.of(Nightborn.MOD_ID, "keeps_own_use"));

  public static final double REACH_SLACK = 1.0;

  private static final List<WheelSubject<?>> SUBJECTS = new CopyOnWriteArrayList<>();
  private static final Map<UUID, WheelMenu> MENUS = new ConcurrentHashMap<>();

  private CommandWheel() {}

  public static void register() {
    ServerPlayNetworking.registerGlobalReceiver(
        ChooseWheelPayload.ID, (payload, context) -> choose(context.player(), payload));
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> MENUS.remove(handler.getPlayer().getUuid()));
  }

  public static <T extends Entity> WheelSubject<T> subject(
      final Class<T> type, final SkillTree theme) {
    final WheelSubject<T> subject = new WheelSubject<>(type, theme);
    SUBJECTS.add(subject);
    return subject;
  }

  public static ActionResult use(final PlayerEntity player, final Hand hand, final Entity target) {
    if (!WheelGate.opens(
        hand == Hand.MAIN_HAND,
        player.getStackInHand(hand).isIn(KEEPS_OWN_USE),
        player.isSpectator())) {
      return ActionResult.PASS;
    }
    if (!(player instanceof ServerPlayerEntity server)) {
      return ActionResult.SUCCESS;
    }
    return open(server, target) ? ActionResult.CONSUME : ActionResult.PASS;
  }

  public static boolean open(final ServerPlayerEntity player, final Entity target) {
    return SUBJECTS.stream()
        .map(subject -> openWith(subject, player, target))
        .filter(Boolean::booleanValue)
        .findFirst()
        .orElse(false);
  }

  private static <T extends Entity> boolean openWith(
      final WheelSubject<T> subject, final ServerPlayerEntity player, final Entity target) {
    final Optional<T> cast = subject.cast(target);
    if (cast.isEmpty()) {
      return false;
    }
    final List<WheelEntry> entries = subject.entries(player, cast.get());
    if (entries.isEmpty()) {
      return false;
    }
    show(player, target, subject.theme(), target.getDisplayName(), entries);
    return true;
  }

  public static void show(
      final ServerPlayerEntity player,
      final Entity target,
      final SkillTree theme,
      final Text title,
      final List<WheelEntry> entries) {
    final List<WheelEntry> shown =
        entries.subList(0, Math.min(entries.size(), OpenWheelPayload.MOST_ENTRIES));
    MENUS.put(player.getUuid(), new WheelMenu(target.getId(), shown));
    ServerPlayNetworking.send(player, new OpenWheelPayload(target.getId(), theme, title, shown));
  }

  public static boolean choose(final ServerPlayerEntity player, final ChooseWheelPayload payload) {
    final WheelMenu menu = MENUS.remove(player.getUuid());
    if (menu == null || Moments.locked(player)) {
      return false;
    }
    final Optional<WheelEntry> chosen =
        menu.find(payload.entityId(), payload.action(), payload.argument());
    final Entity target = player.getServerWorld().getEntityById(payload.entityId());
    if (!WheelChoice.accepted(
        chosen.isPresent(),
        target != null,
        target != null && target.isAlive(),
        target != null && player.canInteractWithEntity(target, REACH_SLACK))) {
      return false;
    }
    return SUBJECTS.stream()
        .filter(subject -> subject.has(chosen.get().action()))
        .anyMatch(subject -> performWith(subject, player, target, chosen.get()));
  }

  private static <T extends Entity> boolean performWith(
      final WheelSubject<T> subject,
      final ServerPlayerEntity player,
      final Entity target,
      final WheelEntry chosen) {
    return subject.cast(target).filter(t -> subject.perform(player, t, chosen)).isPresent();
  }
}
