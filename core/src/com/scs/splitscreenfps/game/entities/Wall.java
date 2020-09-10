package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.HasModelComponent;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PositionComponent;

import ssmith.libgdx.ShapeHelper;

public class Wall extends AbstractEntity {

	public Wall(Game game, String name, Texture tex, float posX, float posY, float posZ, float w, float h, float d, float mass_pre, int tiles_per_unit, boolean cast_shadow) {
		this(game, name, tex, null, posX, posY, posZ, w, h, d, mass_pre, 0, 0, 0, tiles_per_unit, cast_shadow, true);
	}


	// Note that the mass gets multiplied by the size
	// Positions are from the centre
	public Wall(Game game, String name, Texture tex, Color c, float posX, float posY, float posZ, float w, float h, float d, float mass_pre, float degreesX, float degreesY, float degreesZ, int tiles_per_unit, boolean cast_shadow, boolean add_physics) {
		super(game.ecs, name);

		Material material = null;
		if (tex != null) {
			tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
			material = new Material(TextureAttribute.createDiffuse(tex));
		} else {
			material = new Material(ColorAttribute.createDiffuse(c));
		}
		//material.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)); // Allow transparency, not currently required

		Model box_model = null;
		if (tiles_per_unit == 0) {
			box_model = ShapeHelper.createCube(game.modelBuilder, w, h, d, material);
			Matrix3 mat = new Matrix3();
			float max2 = Math.max(w, h);
			float max = Math.max(max2, d);
			mat.scl(max);
			box_model.meshes.get(0).transformUV(mat);
		} else {
			box_model = ShapeHelper.createCube_AdvancedScaling(game.modelBuilder, w, h, d, material, (float)tiles_per_unit);
		}
		
		ModelInstance instance = new ModelInstance(box_model, new Vector3(posX, posY, posZ));
		if (degreesX != 0) {
			instance.transform.rotate(Vector3.X, degreesX);
		}
		if (degreesY != 0) {
			instance.transform.rotate(Vector3.Y, degreesY);
		}
		if (degreesZ != 0) {
			instance.transform.rotate(Vector3.Z, degreesZ);
		}

		HasModelComponent model = new HasModelComponent(instance, 1f, cast_shadow);
		this.addComponent(model);

		if (add_physics) {
			//if (mass_pre >= 0) {
			float mass = mass_pre * w * h * d;
			if (mass > 0 && mass < 1f) {
				mass = 1; // Give a minimum mass for (e.g.) thin walls
			}

			btBoxShape boxShape = new btBoxShape(new Vector3(w/2, h/2, d/2));
			Vector3 local_inertia = new Vector3();
			boxShape.calculateLocalInertia(mass, local_inertia);
			btRigidBody body = new btRigidBody(mass, null, boxShape, local_inertia);
			body.userData = this;
			body.setRestitution(.2f);
			body.setDamping(.5f, .5f);
			//groundObject.setFriction(.1f);
			body.setCollisionShape(boxShape);
			body.setWorldTransform(instance.transform);
			this.addComponent(new PhysicsComponent(body));
		}

		this.addComponent(new PositionComponent()); // todo - if no physics, set this position!
	}

}
