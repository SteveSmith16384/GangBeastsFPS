package com.scs.splitscreenfps.game.levels;

import java.io.IOException;

import com.badlogic.gdx.math.Vector3;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.scs.basicecs.ISystem;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.entities.SkyboxCube;
import com.scs.splitscreenfps.game.entities.Wall;
import com.scs.splitscreenfps.game.gamemodes.DeathmatchSystem;

import ssmith.lang.NumberFunctions;

public class TowerBlocksLevel extends AbstractLevel {

	private float floor_size = 35f;

	private ISystem deathmatchSystem;

	public void getReadyForGame(Game game) {
		super.getReadyForGame(game);
		this.deathmatchSystem = new DeathmatchSystem(game, game.ecs, true);
	}


	@Override
	public void load() throws JsonSyntaxException, JsonIOException, IOException {
		Wall floor = new Wall(game, "Floor", game.getTexture("colours/white.png"), floor_size/2, -0.1f, floor_size/2, 
				floor_size, .2f, floor_size, 
				0f, true, false);
		game.ecs.addEntity(floor);


		this.startPositions.add(new Vector3(1, 2f, 1));
		this.startPositions.add(new Vector3(floor_size-2, 2f, floor_size-2));
		this.startPositions.add(new Vector3(1, 2f, floor_size-2));
		this.startPositions.add(new Vector3(floor_size-2, 2f, 1));

		for (int z=0 ; z<2 ; z++) {
			for (int x=0 ; x<2 ; x++) {
				//super.loadJsonFile("maps/map_editor.json", false, new Vector3(x*8+4, 0, z*8+4), 2);
				super.loadJsonFile("maps/skyscraper" + NumberFunctions.rnd(1, 2) + ".json", false, new Vector3(x*10+7, 0.05f, z*10+7), 1);
			}
		}
		game.ecs.addEntity(new SkyboxCube(game, "Skybox", "textures/sky3.jpg", 90, 90, 90));
	}


	@Override
	public void update() {
		deathmatchSystem.process();
	}


	@Override
	public String getName() {
		return "Tower Block Deathmatch";
	}


}
