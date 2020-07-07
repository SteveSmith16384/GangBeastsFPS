package com.scs.splitscreenfps.game.components;

public class WeaponSettingsComponent {

	// Weapon types
	public static final int WEAPON_RIFLE = 1;
	public static final int WEAPON_GRENADE_LAUNCHER = 2;
	public static final int WEAPON_ROCKET_LAUNCHER = 3;
	public static final int WEAPON_PUNCH = 4;
	public static final int WEAPON_CANNON = 5;
	
	public int weapon_type;
	public long shot_interval;
	public long reload_interval;
	public int max_ammo;
	public float range;
	public int damage;
	public float expl_force;
	public float expl_range;
	public float kickback_force = 0f;
	
	public WeaponSettingsComponent(int type, long _shot_interval, long _reload_interval, int ammo, float _range, int _damage, 
			float _expl_range, float _expl_force) {
		this.weapon_type = type;
		this.max_ammo = ammo;
		shot_interval = _shot_interval;
		reload_interval = _reload_interval;
		damage = _damage;
		range = _range;
		expl_range = _expl_range;
		expl_force = _expl_force;
	}
	
}
