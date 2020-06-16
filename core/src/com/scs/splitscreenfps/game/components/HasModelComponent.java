package com.scs.splitscreenfps.game.components;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class HasModelComponent {

	public ModelInstance model;
	public Vector3 offset; // In case the origin of the 3D model isn't 0,0,0
	public int angleOffset;
	//public float scale; // todo - remove?
	public int dontDrawInViewId = -1; // Don't draw the player's own avatar!
	public int onlyDrawInViewId = -1; // Don't draw the targetter for other players!
	public BoundingBox bb; // For checking if in frustum  
	public boolean always_draw = false;
	
	public HasModelComponent(ModelInstance _model) {
		this(_model, new Vector3(), 0);
	}
	

	public HasModelComponent(ModelInstance _model, float yOffset, int _angleOffset) {
		this(_model);
		
		this.offset.y = yOffset;
		angleOffset = _angleOffset;
		//scale = _scale;
	}

	
	public HasModelComponent(ModelInstance _model, Vector3 _offset, int _angleOffset) {
		model = _model;
		offset = _offset;
		angleOffset = _angleOffset;
	}

	
}
