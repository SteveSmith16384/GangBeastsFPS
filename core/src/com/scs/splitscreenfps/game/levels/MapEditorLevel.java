package com.scs.splitscreenfps.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.systems.MapEditorSystem;
import com.scs.splitscreenfps.game.systems.ShootingSystem;

public class MapEditorLevel extends AbstractLevel {

	public MapEditorSystem mapBuilderSystem;

	public MapEditorLevel(Game _game) {
		super(_game);

		game.ecs.removeSystem(ShootingSystem.class);

		mapBuilderSystem = new MapEditorSystem(game.ecs, game);
	}


	@Override
	public void setBackgroundColour() {
		Gdx.gl.glClearColor(0, .6f, .8f, 1);
	}


	@Override
	public void load() {
/*
		Wall floor = new Wall(game.ecs, "Floor", "textures/neon/tron_green_2x2.png", FLOOR_SIZE/2, -0.1f, FLOOR_SIZE/2, 
				FLOOR_SIZE, .2f, FLOOR_SIZE, 
				0f, true, false);
		game.ecs.addEntity(floor);
	*/	
		try {
			super.loadJsonFile("maps/default_map.json");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (this.startPositions.size() == 0) {
			// Add default start positions
			this.startPositions.add(new Vector3(1, 2f, 1));
			this.startPositions.add(new Vector3(2, 2f, 2));
		}
		
	}


	@Override
	public void update() {
		this.mapBuilderSystem.process();
	}


	@Override
	public void renderUI(SpriteBatch batch2d, int currentViewId) {
		/*float yOff = (game.font_med.getLineHeight() * 1.2f);
		float yPos = 200 + yOff;
		game.font_med.setColor(1, 1, 1, 1);
		game.font_med.draw(batch2d, mapBuilderSystem.mode_text, 10, yPos);

		yPos += yOff;
		game.font_med.draw(batch2d, mapBuilderSystem.selected_text, 10, yPos);
*/
	}


}
