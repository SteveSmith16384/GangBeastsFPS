package com.scs.splitscreenfps.game.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PlayerData;
import com.scs.splitscreenfps.game.components.PositionComponent;
import com.scs.splitscreenfps.game.components.SecondaryAbilityComponent;
import com.scs.splitscreenfps.game.data.ExplosionData;
import com.scs.splitscreenfps.game.entities.AbstractPlayersAvatar;
import com.scs.splitscreenfps.game.entities.BulletEntityFactory;
import com.scs.splitscreenfps.game.entities.GraphicsEntityFactory;
import com.scs.splitscreenfps.game.entities.PlayerAvatar_Person;

import ssmith.lang.NumberFunctions;

public class SecondaryAbilitySystem extends AbstractSystem {

	private Game game;
	private Vector3 tmpVec = new Vector3();
	private Matrix4 tmpMat = new Matrix4();

	public SecondaryAbilitySystem(BasicECS ecs, Game _game) {
		super(ecs, SecondaryAbilityComponent.class);

		game = _game;
	}


	@Override
	public void processEntity(AbstractEntity entity) {
		SecondaryAbilityComponent ability = (SecondaryAbilityComponent)entity.getComponent(SecondaryAbilityComponent.class);

		if (ability.count_available < ability.max_count) {
			ability.current_cooldown += Gdx.graphics.getDeltaTime();
			if (ability.current_cooldown >= ability.cooldown_duration) {
				BillBoardFPS_Main.audio.play("sfx/teleport.mp3");
				ability.current_cooldown = 0;
				ability.count_available++;
				if (ability.count_available > ability.max_count) {
					ability.count_available = ability.max_count;
				}
			}
		}


		AbstractPlayersAvatar player = (AbstractPlayersAvatar)entity;
		PlayerData playerData = (PlayerData)entity.getComponent(PlayerData.class);
		if (ability.count_available > 0) {
			playerData.ability1Ready = true;
			playerData.ability1text = ability.type + " Ready! (" + ability.count_available + ") ";
			if (player.inputMethod.isAbility1Pressed()) {
				if (ability.button_released || ability.requiresBuildUp) {
					ability.button_released = false;
					if (ability.requiresBuildUp) {
						ability.buildUpActivated = true;
						ability.power += Gdx.graphics.getDeltaTime();
						int pcent = (int)(ability.power * 100 / ability.max_power_duration);
						playerData.ability1text = "Power: " + pcent + "%";
						if (ability.power >= ability.max_power_duration) {
							this.performBuildUpAbility(player, ability);
							//ability.power = 0;
						}
					} else {
						//Settings.p("Shoot at " + System.currentTimeMillis());

						boolean success = true;

						switch (ability.type) {
						case JumpForwards:
							performPowerJump(player);
							break;
						case JumpUp:
							performJumpUp(entity);
							break;
						case JetPac:
							performJetPac(entity);
							break;
						case Blink:
							success = performRacerBlink(player);
							break;
						case Sticky_Mine:
							throwJunkratMine(player, ability);
							break;
						case Invisible_Mine:
							success = throwInvisibleMine(player, ability);
							break;
						default:
							if (Settings.STRICT) {
								throw new RuntimeException("Unknown ability: " + ability.type);
							}
						}

						if (success) {
							ability.count_available--;
						}
					}
				}
			} else { // Button released?
				ability.button_released = true;
				if (ability.buildUpActivated) {
					this.performBuildUpAbility(player, ability);
				}
			}
		} else {			
			playerData.ability1Ready = false;

			int pcent = (int)((ability.current_cooldown / ability.cooldown_duration) * 100);
			playerData.ability1text = ability.type + " " + pcent + "%";
		}
	}


	private void performBuildUpAbility(AbstractPlayersAvatar player, SecondaryAbilityComponent ability) {
		ability.buildUpActivated = false;
		//ability.lastShotTime = System.currentTimeMillis();

		switch (ability.type) {
		case PowerPunch:
			performPowerPunch(player, ability.power);
			break;
		default:
			//throw new RuntimeException("Unknown ability: " + ability.type);
		}

		ability.power = 0;
		ability.count_available--;
	}


	private void performPowerPunch(AbstractPlayersAvatar player, float power) {
		PhysicsComponent pc = (PhysicsComponent)player.getComponent(PhysicsComponent.class);
		pc.body.activate();
		float pow = 15+(power*30);
		//Settings.p("Performing boost with pow=" + pow);
		pc.getRigidBody().applyCentralImpulse(player.camera.direction.cpy().scl(pow));
		//pc.body.appl.applyCentralForce(player.camera.direction.cpy().scl(power*3000)); Doesn't do anything?

		PositionComponent posData = (PositionComponent)player.getComponent(PositionComponent.class);
		AbstractEntity e = GraphicsEntityFactory.createBlueExplosion(game, posData.position);
		game.ecs.addEntity(e);

		PlayerData playerData = (PlayerData)player.getComponent(PlayerData.class);
		playerData.performing_power_punch = true;

		BillBoardFPS_Main.audio.play("sfx/boomfist_punch.wav");
	}


	private boolean performRacerBlink(AbstractPlayersAvatar player) {
		float dist = 7.5f;
		PositionComponent posData = (PositionComponent)player.getComponent(PositionComponent.class);
		Vector3 dir = new Vector3(player.camera.direction);
		if (dir.y < 0) {
			dir.y = 0;
			dir.nor();
		} else if (dir.y > .4f) {
			dir.y = .4f;
			dir.nor();
		}
		//boolean clear = true;
		ClosestRayResultCallback results = game.rayTestByDir(posData.position, dir, dist);
		if (results != null) {
			dist = dist * results.getClosestHitFraction();
			dist -= PlayerAvatar_Person.RADIUS;
		}
		if (dist < 2f) {
			BillBoardFPS_Main.audio.play("sfx/enemyalert.mp3");
			return false;
		}
		PhysicsComponent pc = (PhysicsComponent)player.getComponent(PhysicsComponent.class);
		pc.body.getWorldTransform(tmpMat);
		tmpMat.getTranslation(tmpVec);
		tmpVec.mulAdd(dir, dist);
		tmpMat.setTranslation(tmpVec);
		pc.body.setWorldTransform(tmpMat);
		pc.body.activate();

		// f/x
		AbstractEntity e = GraphicsEntityFactory.createBlueExplosion(game, posData.position);
		game.ecs.addEntity(e);

		BillBoardFPS_Main.audio.play("sfx/boost-start.ogg");
		return true;
	}


	private void performPowerJump(AbstractPlayersAvatar player) {
		PhysicsComponent pc = (PhysicsComponent)player.getComponent(PhysicsComponent.class);
		pc.body.activate();
		Vector3 dir = new Vector3(player.camera.direction);
		dir.y += .2f;
		dir.nor().scl(30);
		pc.getRigidBody().applyCentralImpulse(dir);
	}


	private void performJetPac(AbstractEntity entity) {
		BillBoardFPS_Main.audio.play("sfx/fart" + NumberFunctions.rnd(1, 5) + ".wav");

		PhysicsComponent pc = (PhysicsComponent)entity.getComponent(PhysicsComponent.class);
		pc.body.activate();
		pc.getRigidBody().applyCentralImpulse(new Vector3(0, 40, 0));

		PositionComponent posData = (PositionComponent)entity.getComponent(PositionComponent.class);
		AbstractEntity e = GraphicsEntityFactory.createBlueExplosion(game, posData.position);
		game.ecs.addEntity(e);
	}


	private void performJumpUp(AbstractEntity entity) {
		BillBoardFPS_Main.audio.play("sfx/jumps-soundsx01.wav");

		PhysicsComponent pc = (PhysicsComponent)entity.getComponent(PhysicsComponent.class);
		pc.body.activate();
		pc.getRigidBody().applyCentralImpulse(new Vector3(0, 20, 0));

		PositionComponent posData = (PositionComponent)entity.getComponent(PositionComponent.class);
		AbstractEntity e = GraphicsEntityFactory.createBlueExplosion(game, posData.position);
		game.ecs.addEntity(e);
	}


	private void throwJunkratMine(AbstractPlayersAvatar player, SecondaryAbilityComponent ability) {
		if (ability.entity == null || ability.entity.isMarkedForRemoval()) {
			ability.entity = BulletEntityFactory.createJunkratMine(game, player);
			game.ecs.addEntity(ability.entity);
		} else {
			// Explode current bomb
			PositionComponent posData = (PositionComponent)ability.entity.getComponent(PositionComponent.class);
			game.explosion(posData.position, new ExplosionData(3, 100, 5), player, false);
			ability.entity.remove();
			ability.entity = null;
		}
	}


	private boolean throwInvisibleMine(AbstractPlayersAvatar player, SecondaryAbilityComponent ability) {
		if (ability.entity == null || ability.entity.isMarkedForRemoval()) {
			ability.entity = BulletEntityFactory.createInvisibleMine(game, player);
			game.ecs.addEntity(ability.entity);
			return true;
		} else {
			ability.entity.remove();
			ability.entity = null;
			return false;
		}
	}
}
