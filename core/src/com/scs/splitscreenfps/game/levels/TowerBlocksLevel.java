package com.scs.splitscreenfps.game.levels;

import java.io.IOException;

import com.badlogic.gdx.math.Vector3;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.ISystem;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.entities.EntityFactory;
import com.scs.splitscreenfps.game.entities.Wall;
import com.scs.splitscreenfps.game.gamemodes.DeathmatchSystem;

import ssmith.lang.NumberFunctions;

public class TowerBlocksLevel extends AbstractLevel {

	private float floor_size = 35f;

	private ISystem deathmatchSystem;

	public void getReadyForGame(Game game) {
		super.getReadyForGame(game);		
		if (Settings.TEST_VOX == false) {
			this.deathmatchSystem = new DeathmatchSystem(game, game.ecs, true);
		}
	}


	@Override
	public void load() throws JsonSyntaxException, JsonIOException, IOException {
		Wall floor = new Wall(game, "Floor", game.getTexture("textures/tones/brown1.png"), floor_size/2, -0.1f, floor_size/2, 
				floor_size, .2f, floor_size, 
				0f, true, false);
		game.ecs.addEntity(floor);

		if (Settings.TEST_VOX) {
			this.startPositions.add(new Vector3(1, 2f, 1));
			this.startPositions.add(new Vector3(1, 2f, 1));

			//loadVox("vox/graveyard.vox", 0, new Vector3(5, .1f, 5), .2f, true, false);

			Vector3 model_pos = new Vector3(3f, 0, 3f);
			String filename = "vox/monu1";
			//String filename = "vox/graveyard";
			//String filename = "vox/skyscraper1";
			super.createCollisionShapesFromVox(filename + ".vox", model_pos, .1f);
			AbstractEntity model = EntityFactory.createOnlyModel(game.ecs, "Castle", filename + ".obj", new Vector3(6.5f, 0, 3f));
			game.ecs.addEntity(model);

			//AbstractEntity model = EntityFactory.createStaticModel(game.ecs, "Castle", "vox/graveyard.obj", 5, 0, 5, 0, true);
			//game.ecs.addEntity(model);
		} else {
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
		}
	}


	@Override
	public void update() {
		if (Settings.TEST_VOX == false) {
			deathmatchSystem.process();
		}
	}


	@Override
	public String getName() {
		return "Tower Block Deathmatch";
	}


}
