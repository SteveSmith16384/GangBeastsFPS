package com.scs.splitscreenfps.game.levels;

import com.badlogic.gdx.Gdx;
import com.scs.basicecs.ISystem;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.entities.AvatarFactory;
import com.scs.splitscreenfps.game.gamemodes.DeathmatchSystem;

public class TempleOfTheNoobiesLevel extends AbstractLevel {

	private ISystem deathmatchSystem;
	
	public void getReadyForGame(Game game) {
		super.getReadyForGame(game);

		this.deathmatchSystem = new DeathmatchSystem(game, game.ecs, true);
	}


	@Override
	public int[] getHeroSelection() {
		return new int[]{AvatarFactory.CHAR_PHARTAH, AvatarFactory.CHAR_BOOMFIST, AvatarFactory.CHAR_BOWLING_BALL, AvatarFactory.CHAR_RACER};
	}


	@Override
	public void setBackgroundColour() {
		Gdx.gl.glClearColor(0, .6f, .8f, 1);
	}


	@Override
	public void load() {
		try {
			super.loadJsonFile("maps/templeofthenoobies.json", false);
			//super.loadJsonFile("maps/map_editor.json", false);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	@Override
	public void update() {
		deathmatchSystem.process();
	}


}
