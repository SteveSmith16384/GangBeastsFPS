package com.scs.splitscreenfps.game.components;

public class WeaponSettingsComponent {

	// Weapon types
	public static final int WEAPON_BULLET = 1;
	public static final int WEAPON_GRENADE = 2;
	public static final int WEAPON_ROCKET = 3;
	
	public int weapon_type;
	public long shot_interval;
	public long reload_interval;
	public int max_ammo;
	public int range;
	public int damage;
	
	/*
	public WeaponSettingsComponent() {
	}*/
	
	
	public WeaponSettingsComponent(int type, long _shot_interval, long _reload_interval, int ammo, int _range, int _damage) {
		this.weapon_type = type;
		this.max_ammo = ammo;
		shot_interval = _shot_interval;
		reload_interval = _reload_interval;
		damage = _damage;
		range = _range;
	}
	
}