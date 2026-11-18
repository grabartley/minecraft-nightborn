package com.grahambartley.nightborn.faction;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.util.Identifier;

public final class OathAttachment {

  public static final Identifier ID = Identifier.of(Nightborn.MOD_ID, "oath");

  public static final AttachmentType<Oath> TYPE =
      AttachmentRegistry.create(ID, builder -> builder.persistent(Oath.CODEC).copyOnDeath());

  private OathAttachment() {}

  public static void register() {}

  public static Oath get(final AttachmentTarget target) {
    return target.getAttachedOrElse(TYPE, Oath.NONE);
  }

  public static void set(final AttachmentTarget target, final Oath oath) {
    target.setAttached(TYPE, Oath.NONE.equals(oath) ? null : oath);
  }
}
