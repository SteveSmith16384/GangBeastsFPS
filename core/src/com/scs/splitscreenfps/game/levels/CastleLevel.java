package com.scs.splitscreenfps.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.entities.EntityFactory;
import com.scs.splitscreenfps.game.entities.Wall;

public class CastleLevel extends AbstractLevel {

	private static final float FLOOR_SIZE = 20f;

	public CastleLevel(Game _game) {
		super(_game);
	}


	@Override
	public void setBackgroundColour() {
		Gdx.gl.glClearColor(0, .6f, .8f, 1);
	}


	@Override
	public void load() {
		this.startPositions.add(new Vector3(1, 2f, 1));
		this.startPositions.add(new Vector3(FLOOR_SIZE-2, 2f, FLOOR_SIZE-2));
		this.startPositions.add(new Vector3(1, 2f, FLOOR_SIZE-2));
		this.startPositions.add(new Vector3(FLOOR_SIZE-2, 2f, 1));

		Wall floor = new Wall(game.ecs, "Floor", "textures/neon/tron_green.jpg", FLOOR_SIZE/2, -0.1f, FLOOR_SIZE/2, 
				FLOOR_SIZE, .2f, FLOOR_SIZE, 
				0f, true);
		game.ecs.addEntity(floor);

		AbstractEntity entity = EntityFactory.createModelAndPhysicsBox(game.ecs, "CastlePart", "models/kenney/castle/towerSquare.g3db", 5, 2, 5, 0, 1);
		//AbstractEntity entity = EntityFactory.createModelAndPhysicsBox(game.ecs, "CastlePart", "models/quaternius/alien.g3db", 5, 2, 5, 0, 1);
		game.ecs.addEntity(entity);

		
		FileHandle file = Gdx.files.local("maps/castle1.csv");
		String csv = file.readString();
		String rows[] = csv.split("\n");
		for (int row=0 ; row<rows.length ; row++) {
			String cols[] = rows[row].split("\t");
			for (int col=0 ; col<cols.length ; col++) {
				String tokens[] = cols[col].split(",");
				for (int t=0 ; t<tokens.length ; t++) {
					String items[] = tokens[t].split("-");
					int code = Integer.parseInt(items[0].trim());
					int angle = 0;
					if (items.length > 1) {
						Integer.parseInt(items[1].trim());
					}
					addItem(col, row, code, angle);
				}
			}
		}
	}

	
	private void addItem(int col, int row, int code, int angle) {
		// todo
		AbstractEntity entity = EntityFactory.createModelAndPhysicsBox(game.ecs, "CastlePart", "models/kenney/castle/towerBase.g3db", col, 0, row, angle, 1);
		//game.ecs.addEntity(entity);
	}
	

	@Override
	public void update() {

	}


	@Override
	public void renderUI(SpriteBatch batch2d, int currentViewId) {

	}



}