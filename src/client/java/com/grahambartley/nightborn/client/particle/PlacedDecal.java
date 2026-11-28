package com.grahambartley.nightborn.client.particle;

public interface PlacedDecal<D extends PlacedDecal<D>> {

  boolean gone();

  boolean retiring();

  void retire();

  void dropNow();

  boolean absorbs(D incoming);

  void absorb(D incoming);
}
