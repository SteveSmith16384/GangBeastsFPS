package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.CanBeCarriedComponent;
import com.scs.splitscreenfps.game.components.HasModelComponent;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PositionComponent;

import ssmith.libgdx.ShapeHelper;

public class EquipmentEntityFactory {

	public static AbstractEntity createPickup(Game game, float posX, float posY, float posZ) {
		AbstractEntity pickup = new AbstractEntity(game.ecs, "Pickup");

		float w = .5f;
		float h = .5f;
		float d = .5f;

		pickup.addComponent(new CanBeCarriedComponent());

		TextureRegion tex = game.getTexture("textures/spritesforyou.png", 8, 8, 0, 4);
		Material black_material = new Material(TextureAttribute.createDiffuse(tex));
		ModelBuilder modelBuilder = game.modelBuilder;

		Model box_model = ShapeHelper.createCube(modelBuilder, w, h, d, black_material);
		ModelInstance instance = new ModelInstance(box_model, new Vector3(posX, posY, posZ));
		HasModelComponent model = new HasModelComponent(instance, 1f, true);
		pickup.addComponent(model);

		pickup.addComponent(new PositionComponent());

		btBoxShape boxShape = new btBoxShape(new Vector3(w/2, h/2, d/2));
		Vector3 local_inertia = new Vector3();
		boxShape.calculateLocalInertia(1f, local_inertia);
		btRigidBody groundObject = new btRigidBody(w*h*d, null, boxShape, local_inertia);
		groundObject.userData = pickup;
		groundObject.setRestitution(.5f);
		groundObject.setCollisionShape(boxShape);
		groundObject.setWorldTransform(instance.transform);
		pickup.addComponent(new PhysicsComponent(groundObject));

		return pickup;
	}
}
