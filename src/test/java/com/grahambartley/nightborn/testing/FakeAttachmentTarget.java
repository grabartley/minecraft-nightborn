package com.grahambartley.nightborn.testing;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public final class FakeAttachmentTarget implements AttachmentTarget {

  private final Map<AttachmentType<?>, Object> attached = new HashMap<>();
  private int writes;

  @Override
  @SuppressWarnings("unchecked")
  public <A> A getAttached(final AttachmentType<A> type) {
    return (A) attached.get(type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <A> A setAttached(final AttachmentType<A> type, final A value) {
    writes++;
    return (A) (value == null ? attached.remove(type) : attached.put(type, value));
  }

  @Override
  public boolean hasAttached(final AttachmentType<?> type) {
    return attached.containsKey(type);
  }

  public int writes() {
    return writes;
  }
}
