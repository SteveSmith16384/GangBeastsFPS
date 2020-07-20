
package com.scs.splitscreenfps.game.components;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

public class PhysicsComponent {

	public btRigidBody body;
	public boolean disable_gravity = false;
	public Vector3 force;
	public boolean removeIfFallen = true;
	public boolean physicsControlsRotation = true; // False for players
	public boolean position_dity = true; // Objects with mass=0 only need position setting once
	
	// Temp vars
	private Matrix4 mat = new Matrix4();
	private Vector3 vec = new Vector3();

	public PhysicsComponent(btRigidBody _body) {
		body = _body;
	}
	
	
	public void rotate(Vector3 axis, float degrees) {
		body.getWorldTransform(mat);
		mat.rotate(axis, degrees);
		
	}
	
}
