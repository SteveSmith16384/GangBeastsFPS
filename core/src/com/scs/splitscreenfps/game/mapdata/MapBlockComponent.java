package com.scs.splitscreenfps.game.mapdata;

import com.badlogic.gdx.math.Vector3;

/*
 * DO NOT RENAME ANY OF THESE VARS!  THEY ARE READ IN FROM A JSON FILE!!
 */
public class MapBlockComponent {

	public static int next_id = 1;

	public int id;
	public String type = "";
	public String name = "";
	public String tags = ""; // For codes etc...
	public int texture_id;
	public String model_filename = "";
	//public boolean tiled = true; // todo - remove
	public int tiles_per_unit = -1;
	
	public Vector3 size = new Vector3();
	public Vector3 position = new Vector3();
	public Vector3 rotation = new Vector3();
	
	public float mass = 0;

	public MapBlockComponent() {
		id = next_id++;
	}
	

	public MapBlockComponent clone() {
		MapBlockComponent tmp = new MapBlockComponent();
		tmp.type = this.type;
		tmp.name = this.name;// + "_new";
		tmp.texture_id = this.texture_id;
		tmp.model_filename = this.model_filename;
		tmp.size = new Vector3(this.size);
		tmp.position = new Vector3(this.position);
		tmp.rotation = new Vector3(this.rotation);
		tmp.mass = this.mass;
		tmp.tiles_per_unit = this.tiles_per_unit;
		tmp.tags = this.tags;

		tmp.position.y += tmp.size.y; // Have it slightly higher

		return tmp;
	}
}
