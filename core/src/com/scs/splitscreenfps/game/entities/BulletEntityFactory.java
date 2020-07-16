package com.scs.splitscreenfps.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.scs.basicecs.AbstractEntity;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.ExplodeAfterTimeComponent;
import com.scs.splitscreenfps.game.components.ExplodeOnContactComponent;
import com.scs.splitscreenfps.game.components.HasDecal;
import com.scs.splitscreenfps.game.components.HasModelComponent;
import com.scs.splitscreenfps.game.components.IsBulletComponent;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PlayerData;
import com.scs.splitscreenfps.game.components.PositionComponent;
import com.scs.splitscreenfps.game.components.RemoveEntityAfterTimeComponent;
import com.scs.splitscreenfps.game.components.WeaponSettingsComponent;
import com.scs.splitscreenfps.game.data.ExplosionData;

import ssmith.libgdx.GraphicsHelper;

public class BulletEntityFactory {

	public static AbstractEntity createBullet(Game game, AbstractEntity shooter, Vector3 start, Vector3 dir) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Bullet");

		e.addComponent(new PositionComponent());

		PlayerData playerData = (PlayerData)shooter.getComponent(PlayerData.class);
		WeaponSettingsComponent settings = (WeaponSettingsComponent)shooter.getComponent(WeaponSettingsComponent.class);

		HasDecal hasDecal = new HasDecal();
		hasDecal.decal = getBulletDecal(game, playerData.playerIdx);

		hasDecal.faceCamera = true;
		hasDecal.dontLockYAxis = true;
		e.addComponent(hasDecal);

		e.addComponent(new IsBulletComponent(shooter, start, settings, true));

		// Add physics
		btBoxShape shape = new btBoxShape(new Vector3(.1f, .1f, .1f));
		btRigidBody body = new btRigidBody(.1f, null, shape);
		body.userData = e;
		//body.setFriction(0);
		//body.setRestitution(.9f);
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		//body.applyCentralForce(offset.scl(100));
		//body.applyCentralImpulse(offset.scl(10));
		//body.setGravity(new Vector3());
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.disable_gravity = true;
		pc.force = dir.scl(1.5f);
		e.addComponent(pc);

		BillBoardFPS_Main.audio.play("sfx/Futuristic Shotgun Single Shot.wav");

		return e;
	}

	
	private static Decal getBulletDecal(Game game, int side) {
		Decal decal = GraphicsHelper.DecalHelper(game.getTexture("particle.png"), 0.2f);
		decal.setColor(Settings.getColourForSide(side));
		return decal;
	}

	
	public static AbstractEntity createRocket(Game game, AbstractEntity shooter, Vector3 start, Vector3 dir) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Rocket");

		e.addComponent(new PositionComponent());

		PlayerData playerData = (PlayerData)shooter.getComponent(PlayerData.class);
		WeaponSettingsComponent settings = (WeaponSettingsComponent)shooter.getComponent(WeaponSettingsComponent.class);

		HasDecal hasDecal = new HasDecal();
		/*if (playerData.playerIdx == 0) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_red.png"), 0.2f);
		} else if (playerData.playerIdx == 1) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_yellow.png"), 0.2f);
		} else if (playerData.playerIdx == 2) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_magenta.png"), 0.2f);
		} else if (playerData.playerIdx == 3) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_green.png"), 0.2f);
		} else {
			throw new RuntimeException("Invalid side: " + playerData.playerIdx);
		}*/
		
		hasDecal.decal = getBulletDecal(game, playerData.playerIdx);
		
		hasDecal.faceCamera = true;
		hasDecal.dontLockYAxis = true;
		e.addComponent(hasDecal);

		e.addComponent(new IsBulletComponent(shooter, start, settings, true));
		e.addComponent(new ExplodeOnContactComponent(settings.explData, shooter, false));

		// Add physics
		btBoxShape shape = new btBoxShape(new Vector3(.1f, .1f, .1f));
		btRigidBody body = new btRigidBody(.1f, null, shape);
		body.userData = e;
		//body.setFriction(0);
		//body.setRestitution(.9f);
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		//body.applyCentralForce(offset.scl(100));
		//body.applyCentralImpulse(offset.scl(10));
		//body.setGravity(new Vector3());
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.disable_gravity = true;
		pc.force = dir.scl(1f);
		e.addComponent(pc);

		BillBoardFPS_Main.audio.play("sfx/launches/iceball.wav");

		return e;
	}


	public static AbstractEntity createFist(Game game, AbstractEntity shooter, Vector3 start, Vector3 dir) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Punch");

		e.addComponent(new PositionComponent());

		PlayerData playerData = (PlayerData)shooter.getComponent(PlayerData.class);
		WeaponSettingsComponent settings = (WeaponSettingsComponent)shooter.getComponent(WeaponSettingsComponent.class);

		if (Settings.DEBUG_PUNCH) {
			HasDecal hasDecal = new HasDecal();
			/*if (playerData.playerIdx == 0) {
				hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_red.png"), 0.2f);
			} else if (playerData.playerIdx == 1) {
				hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_yellow.png"), 0.2f);
			} else if (playerData.playerIdx == 2) {
				hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_magenta.png"), 0.2f);
			} else if (playerData.playerIdx == 3) {
				hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_green.png"), 0.2f);
			} else {
				throw new RuntimeException("Invalid side: " + playerData.playerIdx);
			}
			hasDecal.decal.setPosition(start);*/
			hasDecal.decal = getBulletDecal(game, playerData.playerIdx);
			hasDecal.faceCamera = true;
			hasDecal.dontLockYAxis = true;
			e.addComponent(hasDecal);
		}

		e.addComponent(new IsBulletComponent(shooter, start, settings, true));

		// Add physics
		btBoxShape shape = new btBoxShape(new Vector3(.1f, .1f, .1f));
		btRigidBody body = new btRigidBody(10f, null, shape);
		body.userData = e;
		//body.setFriction(0);
		//body.setRestitution(.9f);
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		//body.applyCentralForce(offset.scl(100));
		//body.applyCentralImpulse(offset.scl(10));
		//body.setGravity(new Vector3());
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.disable_gravity = true;
		pc.force = dir.scl(100f);
		e.addComponent(pc);

		BillBoardFPS_Main.audio.play("sfx/punch.mp3");

		return e;
	}


	public static AbstractEntity createGrenade(Game game, AbstractEntity shooter, Vector3 start, Vector3 dir) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Grenade");

		e.addComponent(new PositionComponent());

		PlayerData playerData = (PlayerData)shooter.getComponent(PlayerData.class);
		WeaponSettingsComponent settings = (WeaponSettingsComponent)shooter.getComponent(WeaponSettingsComponent.class);

		HasDecal hasDecal = new HasDecal();
		hasDecal.decal = getBulletDecal(game, playerData.playerIdx);

		hasDecal.faceCamera = true;
		hasDecal.dontLockYAxis = true;
		e.addComponent(hasDecal);

		e.addComponent(new IsBulletComponent(shooter, start, settings, false));

		e.addComponent(new ExplodeAfterTimeComponent(2500, settings.explData, shooter));

		// Add physics
		btSphereShape shape = new btSphereShape(.1f);
		btRigidBody body = new btRigidBody(.1f, null, shape);
		body.userData = e;
		body.setFriction(0.9f);
		body.setRestitution(.6f);
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.force = dir.scl(1f);
		e.addComponent(pc);

		BillBoardFPS_Main.audio.play("sfx/Futuristic Shotgun Single Shot.wav");

		return e;
	}


	public static AbstractEntity createCannonball(Game game, AbstractEntity shooter, Vector3 start, Vector3 dir) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Cannonball");

		e.addComponent(new PositionComponent());

		PlayerData playerData = (PlayerData)shooter.getComponent(PlayerData.class);
		WeaponSettingsComponent settings = (WeaponSettingsComponent)shooter.getComponent(WeaponSettingsComponent.class);

		HasDecal hasDecal = new HasDecal();
		/*if (playerData.playerIdx == 0) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_red.png"), 0.2f);
		} else if (playerData.playerIdx == 1) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_yellow.png"), 0.2f);
		} else if (playerData.playerIdx == 2) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_magenta.png"), 0.2f);
		} else if (playerData.playerIdx == 3) {
			hasDecal.decal = GraphicsHelper.DecalHelper(game.getTexture("laser_bolt_green.png"), 0.2f);
		} else {
			throw new RuntimeException("Invalid side: " + playerData.playerIdx);
		}*/
		hasDecal.decal = getBulletDecal(game, playerData.playerIdx);

		hasDecal.faceCamera = true;
		hasDecal.dontLockYAxis = true;
		e.addComponent(hasDecal);

		e.addComponent(new IsBulletComponent(shooter, start, settings, false));

		e.addComponent(new RemoveEntityAfterTimeComponent(4));

		// Add physics
		btSphereShape shape = new btSphereShape(.2f);
		btRigidBody body = new btRigidBody(.2f, null, shape);
		body.userData = e;
		body.setFriction(0.6f);
		body.setRestitution(.8f);
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.force = dir.scl(4f);
		e.addComponent(pc);

		BillBoardFPS_Main.audio.play("sfx/Futuristic Shotgun Single Shot.wav");

		return e;
	}


	public static AbstractEntity createCraterStrike(Game game, AbstractPlayersAvatar shooter) {
		AbstractEntity e = new AbstractEntity(game.ecs, "CraterStrike");

		PositionComponent playerPosData = (PositionComponent)shooter.getComponent(PositionComponent.class);
		Vector3 start = new Vector3(playerPosData.position);
		start.x += shooter.camera.direction.x * 2;
		start.y += 30f;
		start.z += shooter.camera.direction.z * 2;
		
		float diam = 3f;
		
		Texture tex = game.getTexture("textures/sun.jpg");
		Material black_material = new Material(TextureAttribute.createDiffuse(tex));
		ModelBuilder modelBuilder = new ModelBuilder();
		Model sphere_model = modelBuilder.createSphere(diam,  diam,  diam, 10, 10, black_material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
		ModelInstance instance = new ModelInstance(sphere_model);
		HasModelComponent model = new HasModelComponent(instance, 1f, true);
		e.addComponent(model);
		
		e.addComponent(new PositionComponent());

		e.addComponent(new ExplodeOnContactComponent(new ExplosionData(3, 80, 5), shooter, true));

		// Add physics
		btSphereShape shape = new btSphereShape(.1f); // This is a lot smaller so the sphere goes through the ground before exploding
		btRigidBody body = new btRigidBody(1f, null, shape);
		body.userData = e;
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.force = new Vector3(0, -5, 0);
		e.addComponent(pc);

		//No, we have a voice sfx  BillBoardFPS_Main.audio.play("sfx/launches/rlaunch.wav");

		return e;
	}


	public static AbstractEntity createMine(Game game, AbstractPlayersAvatar shooter) {
		AbstractEntity e = new AbstractEntity(game.ecs, "Mine");

		PositionComponent playerPosData = (PositionComponent)shooter.getComponent(PositionComponent.class);
		Vector3 start = new Vector3(playerPosData.position);
		//start.x += shooter.camera.direction.x * 2;
		start.y += 1f;
		//start.z += shooter.camera.direction.z * 2;
		
		float diam = .5f;
		
		Texture tex = game.getTexture("textures/sun.jpg");
		Material black_material = new Material(TextureAttribute.createDiffuse(tex));
		ModelBuilder modelBuilder = new ModelBuilder();
		Model sphere_model = modelBuilder.createSphere(diam,  diam,  diam, 10, 10, black_material, VertexAttributes.Usage.Position | VertexAttributes.Usage.TextureCoordinates);
		ModelInstance instance = new ModelInstance(sphere_model);
		HasModelComponent model = new HasModelComponent(instance, 1f, true);
		e.addComponent(model);
		
		e.addComponent(new PositionComponent());

		e.addComponent(new ExplodeOnContactComponent(new ExplosionData(.2f, 50, 2), shooter, true));

		// Add physics
		btSphereShape shape = new btSphereShape(diam); // This is a lot smaller so the sphere goes through the ground before exploding
		btRigidBody body = new btRigidBody(1f, null, shape);
		body.userData = e;
		body.setCollisionShape(shape);
		Matrix4 mat = new Matrix4();
		mat.setTranslation(start);
		body.setWorldTransform(mat);
		PhysicsComponent pc = new PhysicsComponent(body);
		pc.force = new Vector3(0, -5, 0);
		e.addComponent(pc);

		//No, we have a voice sfx  BillBoardFPS_Main.audio.play("sfx/launches/rlaunch.wav");

		return e;
	}


}
