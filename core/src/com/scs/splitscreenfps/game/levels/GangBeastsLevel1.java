package com.scs.splitscreenfps.game.levels;

import java.util.Properties;
import java.util.regex.Pattern;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.EntityFactory;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.MapData;
import com.scs.splitscreenfps.game.components.CanShoot;
import com.scs.splitscreenfps.game.components.HasModelComponent;
import com.scs.splitscreenfps.game.components.PlayerData;
import com.scs.splitscreenfps.game.components.PositionComponent;
import com.scs.splitscreenfps.game.data.MapSquare;
import com.scs.splitscreenfps.game.entities.Floor;
import com.scs.splitscreenfps.game.entities.Wall;
import com.scs.splitscreenfps.game.gamemodes.IScoreSystem;
import com.scs.splitscreenfps.game.gamemodes.LastPlayerOnPointScoreSystem;
import com.scs.splitscreenfps.game.systems.BulletSystem;
import com.scs.splitscreenfps.game.systems.ShootingSystem;

import ssmith.libgdx.GridPoint2Static;
import ssmith.libgdx.ShapeHelper;

public class GangBeastsLevel1 extends AbstractLevel {

	public static Properties prop;

	public IScoreSystem scoreSystem;
	private btCollisionShape ballShape;
	private btRigidBody ballObject;

	private ModelInstance ground, ball;

	public GangBeastsLevel1(Game _game) {
		super(_game);

		scoreSystem = new LastPlayerOnPointScoreSystem(game);

		prop = new Properties();
	}


	@Override
	public void setupAvatars(AbstractEntity player, int playerIdx) {
		player.addComponent(new PlayerData(playerIdx));

		// Todo - move to where we know size of the map
		Camera cam = game.players[playerIdx].camera;
		cam.lookAt(7,  0.4f,  7);
		cam.update();

		// Add crosshairs
		/*Texture weaponTex = new Texture(Gdx.files.internal("quantumleague/crosshairs.png"));		
		Sprite sprite = new Sprite(weaponTex);
		sprite.setPosition((Gdx.graphics.getWidth()-sprite.getWidth())/2, 0);		
		HasGuiSpriteComponent hgsc = new HasGuiSpriteComponent(sprite, HasGuiSpriteComponent.Z_CARRIED, new Rectangle(0.45f, 0.45f, 0.1f, 0.1f));
		game.players[playerIdx].addComponent(hgsc);
		 */

		player.addComponent(new CanShoot());
	}


	@Override
	public void setBackgroundColour() {
		int winning_side = this.scoreSystem.getWinningPlayer();
		if (winning_side == 0) {
			Gdx.gl.glClearColor(1f, .3f, .3f, 1);
		} else if (winning_side == 1) {
			Gdx.gl.glClearColor(.3f, .3f, 1, 1);
		} else {
			Gdx.gl.glClearColor(.6f, .6f, 1, 1);
		}
	}


	@Override
	public void load() {
		/*
		ground = ShapeHelper.createCube("colours/red.png", 5, 5, 10, 1);
		//ground.transform.rotate(Vector3.X, 20);

		btBoxShape groundShape = new btBoxShape(new Vector3(5f, 0.5f, 5f));

		btRigidBody groundObject = new btRigidBody(0f, null, groundShape);
		groundObject.userData = "Ground";
		//groundObject.setCollisionFlags(flags);
		groundObject.setRestitution(.9f);
		groundObject.setCollisionShape(groundShape);
		groundObject.setWorldTransform(ground.transform);
		game.dynamicsWorld.addRigidBody(groundObject);
*/
		
		//Floor floor = new Floor(game, game.ecs, "Floor", "textures/floor006.png", 5, 0, 5, 8, 8);
		//game.ecs.addEntity(floor);
/*
		ball = ShapeHelper.createSphere("colours/cyan.png", 5, 5, 5, 1);
		ballShape = new btSphereShape(0.5f);
		ballObject = new btRigidBody(1f, null, ballShape);
		ballObject.userData = "Ball";
		ballObject.setRestitution(.9f);
		ballObject.setCollisionShape(ballShape);
		ballObject.setWorldTransform(ball.transform);

		game.dynamicsWorld.addRigidBody(ballObject);
*/
		
		// Add crates
		if (Settings.SMALL_MAP) {
			loadMapFromFile("map_small.csv");
		} else {
			loadMapFromFile("map1.csv");
		}

	}


	private void loadMapFromFile(String file) {
		String str = Gdx.files.internal(file).readString();
		String[] str2 = str.split("\n");

		this.map_width = str2[0].split("\t").length;
		this.map_height = str2.length;

		game.mapData = new MapData(map_width, map_height);

		int row = 0;
		for (String s : str2) {
			s = s.trim();
			if (s.length() > 0 && s.startsWith("#") == false) {
				String cells[] = s.split("\t");
				for (int col=0 ; col<cells.length ; col++) {
					game.mapData.map[col][row] = new MapSquare(game.ecs);

					String cell = cells[col];
					String tokens[] = cell.split(Pattern.quote("+"));
					for (String token : tokens) {
						if (token.equals("S")) { // Start pos
							this.startPositions.add(new GridPoint2Static(col, row));
							//Floor floor = new Floor(game.ecs, "quantumleague/textures/corridor.jpg", col, row, 1, 1, false);
							//game.ecs.addEntity(floor);
						} else if (token.equals("W")) { // Wall
							game.mapData.map[col][row].blocked = true;
							Wall wall = new Wall(game.ecs, "Wall", "textures/set3_example_1.png", col, 0, row, 1, 1, 1, true);
							game.ecs.addEntity(wall);
							
							AbstractEntity crate = EntityFactory.createCrate(game.ecs, "textures/crate.png", col, 3, row, .3f, .3f, .3f);
							game.ecs.addEntity(crate);
						} else if (token.equals("C")) { // Chasm
							game.mapData.map[col][row].blocked = true;
						} else if (token.equals("F")) { // Floor
							if ((col-1) % 4 == 0 && (row-1)  % 4 == 0) {
								Floor floor = new Floor(game.ecs, "Floor", "textures/floor006.png", col, 0, row, 4, 4);
								game.ecs.addEntity(floor);
							}
						} else if (token.equals("G")) { // Goal point
							//Floor floor = new Floor(game.ecs, "Centre", "textures/centre.png", col, .01f, row, 1, 1);
							//game.ecs.addEntity(floor);
						} else if (token.equals("B")) { // Border
							game.mapData.map[col][row].blocked = true;
							//Wall wall = new Wall(game.ecs, "BorderWall", "textures/mjst_metal_beamwindow_diffuse.png", col, 0, row, 1, 1, 1, true);
							//game.ecs.addEntity(wall);
						} else {
							throw new RuntimeException("Unknown cell type: " + token);
						}
					}
				}
				row++;
			}
		}
/*
		float thickness = .1f;
		// White lines
		Floor floor1 = new Floor(game.ecs, "Floor", "colours/white.png", 1.5f, .001f, 1.5f, this.map_width-3, thickness);
		game.ecs.addEntity(floor1);
		Floor floor2 = new Floor(game.ecs, "Floor", "colours/white.png", 1.5f, .001f, (this.map_height/2)+.5f, this.map_width-3, thickness);
		game.ecs.addEntity(floor2);
		Floor floor3 = new Floor(game.ecs, "Floor", "colours/white.png", 1.5f, .001f, this.map_height-1.5f, this.map_width-3, thickness);
		game.ecs.addEntity(floor3);
		Floor floor4 = new Floor(game.ecs, "Floor", "colours/white.png", 1.5f, .001f, 1.5f, thickness, this.map_height - 3f);
		game.ecs.addEntity(floor4);
		Floor floor5 = new Floor(game.ecs, "Floor", "colours/white.png",  this.map_width-1.5f, .001f, 1.5f, thickness, this.map_height - 3f);
		game.ecs.addEntity(floor5);
		*/
	}


	@Override
	public void addSystems(BasicECS ecs) {
		ecs.addSystem(new BulletSystem(ecs, game));
		ecs.addSystem(new ShootingSystem(ecs, game, this));

	}


	@Override
	public void update() {
		game.ecs.processSystem(BulletSystem.class);
		game.ecs.processSystem(ShootingSystem.class);
	}


	public void renderUI(SpriteBatch batch2d, int viewIndex) {
		float yOff = game.font_med.getLineHeight() * 1.2f;

		game.font_med.setColor(1, 1, 1, 1);

		PlayerData playerData = (PlayerData)game.players[viewIndex].getComponent(PlayerData.class);
		game.font_med.draw(batch2d, "Health: " + (int)(playerData.health), 10, (yOff*4));
		game.font_med.draw(batch2d, this.scoreSystem.getHudText(playerData.side), 10, (yOff*5));
	}


	public static void setAvatarColour(AbstractEntity e, boolean alive) {
		// Reset player colours
		HasModelComponent hasModel = (HasModelComponent)e.getComponent(HasModelComponent.class);
		ModelInstance instance = hasModel.model;
		for (int i=0 ; i<instance.materials.size ; i++) {
			if (alive) {
				instance.materials.get(i).set(ColorAttribute.createDiffuse(Color.BLACK));
				instance.materials.get(i).set(ColorAttribute.createAmbient(Color.BLACK));
			} else {
				instance.materials.get(i).set(ColorAttribute.createDiffuse(Color.WHITE));
				instance.materials.get(i).set(ColorAttribute.createAmbient(Color.WHITE));
			}
		}
	}


	public void nextGamePhase() {
		BillBoardFPS_Main.audio.play("sfx/AirHorn.wav");

		BillBoardFPS_Main.audio.startMusic("sfx/fight.wav");

		//this.qlRecordAndPlaySystem.loadNewRecordData();
		//this.qlPhaseSystem.startGamePhase();

		for (int playerIdx=0 ; playerIdx<game.players.length ; playerIdx++) {
			// Reset all health
			PlayerData playerData = (PlayerData)game.players[playerIdx].getComponent(PlayerData.class);
			playerData.health = 100;
			setAvatarColour(game.players[playerIdx], true);

			// Move players avatars back to start
			GridPoint2Static start = this.startPositions.get(playerIdx);
			PositionComponent posData = (PositionComponent)game.players[playerIdx].getComponent(PositionComponent.class);
			posData.position.x = start.x + 0.5f;
			posData.position.z = start.y + 0.5f;
		}
	}


	@Override
	public void startGame() {
		//BillBoardFPS_Main.audio.play("sfx/AirHorn.wav");

		BillBoardFPS_Main.audio.startMusic("sfx/fight.wav");
	}

/*
	static class MyMotionState extends btMotionState {
		Matrix4 transform;
		@Override
		public void getWorldTransform (Matrix4 worldTrans) {
			worldTrans.set(transform);
		}
		@Override
		public void setWorldTransform (Matrix4 worldTrans) {
			transform.set(worldTrans);
		}
	}
*/
}