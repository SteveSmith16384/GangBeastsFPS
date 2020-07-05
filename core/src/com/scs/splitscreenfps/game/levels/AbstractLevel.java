package com.scs.splitscreenfps.game.levels;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.entities.Wall;
import com.scs.splitscreenfps.game.mapdata.MapBlockComponent;
import com.scs.splitscreenfps.game.mapdata.MapData;

import ssmith.lang.IOFunctions;

public abstract class AbstractLevel implements ILevelInterface {

	public Game game;
	protected List<Vector3> startPositions = new ArrayList<Vector3>();
	public MapData mapdata;

	public AbstractLevel(Game _game) {
		game = _game;
	}


	public void setBackgroundColour() {
		Gdx.gl.glClearColor(1f, 1f, 1f, 1);
	}

	public abstract void load();

	public abstract void update();

	public abstract void renderUI(SpriteBatch batch2d, int currentViewId);

	public Vector3 getPlayerStartPoint(int idx) {
		return this.startPositions.get(idx);
	}


	public void loadJsonFile(String filename) throws JsonSyntaxException, JsonIOException, FileNotFoundException {
		Gson gson = new Gson();

		mapdata = gson.fromJson(new FileReader(filename), MapData.class);
		mapdata.filename = filename;
		
		if (mapdata.textures == null) {
			mapdata.textures = new HashMap<Integer, String>();
			mapdata.textures.put(1, "todo");
		}

		for (MapBlockComponent block : mapdata.blocks) {
			if (block.id == 0) {
				block.id = MapBlockComponent.next_id++;
			}
			game.currentLevel.createAndAddEntityFromBlockData(block);
		}

	}


	public AbstractEntity createAndAddEntityFromBlockData(MapBlockComponent block) {
		/*if (block.model_filename != null && block.model_filename.length() > 0) {
			AbstractEntity model = EntityFactory.createModel(game.ecs, block.name, block.model_filename, 
					8, -2f, 7, 
					block.mass, null);
			model.addComponent(block);
			game.ecs.addEntity(model);
			return model;
		} else */

		//if (block.texture_id > 0) {
		if (block.type == null || block.type.length() == 0 || block.type.equalsIgnoreCase("cube")) {
			String tex = "textures/neon/tron_green.jpg"; // Default
			if (this.mapdata.textures.containsKey(block.texture_id)) {
				tex = this.mapdata.textures.get(block.texture_id);
			}
			Wall wall = new Wall(game.ecs, block.name, tex, block.position.x, block.position.y, block.position.z, 
					block.size.x, block.size.y, block.size.z, 
					block.mass,
					block.rotation.x, block.rotation.y, block.rotation.z, true, true);
			wall.addComponent(block);
			game.ecs.addEntity(wall);
			return wall;
		}
		Settings.p("Ignoring line");
		return null;
	}


	public void saveFile() throws JsonIOException, IOException {
		if (mapdata == null) {
			mapdata = new MapData();
		}
		if (mapdata.blocks.size() == 0) {
			// Create dummy block
			MapBlockComponent block = new MapBlockComponent();
			mapdata.blocks.add(block);
		}

		// backup old file
		IOFunctions.copyFileUsingStream(mapdata.filename, mapdata.filename + "_old");

		JsonWriter writer = new JsonWriter(new FileWriter(mapdata.filename));
		writer.setIndent("  ");
		Gson gson = new GsonBuilder().create();
		gson.toJson(mapdata, MapData.class, writer);
		writer.flush();
		writer.close();

	}
}
