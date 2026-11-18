package com.grahambartley.nightborn.faction;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;

public final class GuildMembership {

  private GuildMembership() {}

  public static boolean isHunter(final AttachmentTarget player) {
    return OathAttachment.get(player).hunter(vampire(player));
  }

  public static boolean maySwear(final AttachmentTarget player) {
    return OathAttachment.get(player).maySwear(vampire(player));
  }

  public static boolean prove(final AttachmentTarget player) {
    final Oath before = OathAttachment.get(player);
    if (before.proven() || vampire(player)) {
      return false;
    }
    OathAttachment.set(player, before.prove());
    return true;
  }

  public static boolean swear(final AttachmentTarget player) {
    if (vampire(player) || isHunter(player)) {
      return false;
    }
    OathAttachment.set(player, OathAttachment.get(player).swear());
    return true;
  }

  private static boolean vampire(final AttachmentTarget player) {
    return FactionAttachment.get(player) == Faction.VAMPIRE;
  }
}
