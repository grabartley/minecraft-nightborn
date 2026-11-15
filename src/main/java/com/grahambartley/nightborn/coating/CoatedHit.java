package com.grahambartley.nightborn.coating;

public enum CoatedHit {
  AMMO,
  WEAPON,
  NONE;

  public static CoatedHit of(
      final boolean vampire,
      final boolean blocked,
      final boolean coatedAmmo,
      final boolean coatedWeapon) {
    if (!vampire || blocked) {
      return NONE;
    }
    if (coatedAmmo) {
      return AMMO;
    }
    return coatedWeapon ? WEAPON : NONE;
  }
}
