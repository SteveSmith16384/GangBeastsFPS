package com.scs.splitscreenfps.game.systems;

import com.badlogic.gdx.math.Vector3;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.FallenOffEdgeEvent;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PlayerData;

public class PhysicsSystem extends AbstractSystem {

	// Flags
	public static final short COLL_PLAYER = 8;
	public static final short COLL_PLAYERS_BULLET = 9;
	public static final short COLL_ALL = -1;


	private Game game;

	public PhysicsSystem(Game _game, BasicECS ecs) {
		super(ecs, PhysicsComponent.class);

		game = _game;
	}


	@Override
	public void processEntity(AbstractEntity e) {
		PhysicsComponent pc = (PhysicsComponent)e.getComponent(PhysicsComponent.class);
		if (pc.body.isDisposed()) {
			Settings.pe("Body disposed for " + e);
		}
		if (pc.body.getCollisionShape().isDisposed()) {
			Settings.pe("Shapedisposed for " + e);
		}

		float height = pc.getTranslation().y;
		if (height < -4) {
			if (pc.removeIfFallen) {
				Settings.p("Removed " + e + " since it has fallen off");
				e.remove();
				game.ecs.events.add(new FallenOffEdgeEvent(e));
			} else {
				// Is it a player?
				PlayerData player = (PlayerData)e.getComponent(PlayerData.class);
				if (player != null) {
					if (player.health > 0) {
						game.playerDamaged(e, player, 999, player.last_person_to_hit_them);
						BillBoardFPS_Main.audio.play("sfx/qubodup-PowerDrain.ogg");
					}
				}
			}
		}
	}


	@Override
	public void addEntity(AbstractEntity e) {
		super.addEntity(e);

		PhysicsComponent pc = (PhysicsComponent)e.getComponent(PhysicsComponent.class);
		game.dynamicsWorld.addRigidBody(pc.body);

		if (pc.disable_gravity) {
			pc.body.setGravity(new Vector3());
		}
		if (pc.force != null) {
			//pc.body.applyCentralForce(pc.force.scl(1));
			pc.body.applyCentralImpulse(pc.force);
		}
	}


	@Override
	public void removeEntity(AbstractEntity e) {
		super.removeEntity(e);

		PhysicsComponent pc = (PhysicsComponent)e.getComponent(PhysicsComponent.class);
		game.dynamicsWorld.removeRigidBody(pc.body);
		if (pc.body.getCollisionShape().isDisposed() == false) {
			pc.body.getCollisionShape().dispose();
		}
		if (pc.body.isDisposed() == false) {
			pc.body.dispose();
		}
	}


}
