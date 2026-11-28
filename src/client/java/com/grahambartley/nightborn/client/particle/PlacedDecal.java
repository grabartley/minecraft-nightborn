package com.grahambartley.nightborn.client.particle;

public interface PlacedDecal<D extends PlacedDecal<D>> {

  boolean gone(long now);

  boolean retiring();

  void retire();

  boolean absorbs(D incoming);

  void absorb(D incoming);
}
