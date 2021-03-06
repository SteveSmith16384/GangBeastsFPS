package com.scs.splitscreenfps.game.components;

import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.Settings;

public class PlayerData {

	public float health, max_health;// = Settings.START_HEALTH;
	public boolean dead = false;
	public int playerIdx;
	public String playerName;
	public long restartTime;
	public int points;
	public AbstractEntity last_person_to_hit_them;
	public String gunText = "";
	public String ability1text = "";
	public boolean ability1Ready = false;
	public String ultimateText = "";
	public boolean ultimateReady = false;
	public boolean performing_power_punch;
	public boolean has_been_punched;
	public int damage_caused, num_kills;
	public long invincible_until;
	
	public PlayerData(int _playerIdx, int _health) {
		playerIdx = _playerIdx;
		health = _health;
		this.max_health = health;
		this.playerName = getName(playerIdx);

	}

	
	public static String getName(int p) {
		switch (p) {
		case 0:
			return "GREEN";
		case 1:
			return "YELLOW";
		case 2:
			return "RED";
		case 3:
			return "PURPLE";
		case 4:
			return "BLUE";
		case 5:
			return "PURPLE";
		case 6:
			return "WHITE";
		case 7:
			return "BLACK";
		case 8:
			return "ORANGE";
		default:
			throw new RuntimeException("Unknown side: " + p);
		}

	}
}
