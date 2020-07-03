package com.scs.splitscreenfps.game;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.DebugDrawer;
import com.badlogic.gdx.physics.bullet.collision.ClosestRayResultCallback;
import com.badlogic.gdx.physics.bullet.collision.ContactListener;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractEvent;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.IModule;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.components.ExplodeAfterTimeSystem;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PlayerData;
import com.scs.splitscreenfps.game.components.RemoveEntityAfterTimeComponent;
import com.scs.splitscreenfps.game.entities.AbstractPlayersAvatar;
import com.scs.splitscreenfps.game.entities.AvatarFactory;
import com.scs.splitscreenfps.game.entities.GraphicsEntityFactory;
import com.scs.splitscreenfps.game.entities.TextEntity;
import com.scs.splitscreenfps.game.input.IInputMethod;
import com.scs.splitscreenfps.game.levels.AbstractLevel;
import com.scs.splitscreenfps.game.levels.BuildingSiteLevel;
import com.scs.splitscreenfps.game.systems.AnimationSystem;
import com.scs.splitscreenfps.game.systems.BulletSystem;
import com.scs.splitscreenfps.game.systems.CycleThroughModelsSystem;
import com.scs.splitscreenfps.game.systems.CycleThruDecalsSystem;
import com.scs.splitscreenfps.game.systems.DrawDecalSystem;
import com.scs.splitscreenfps.game.systems.DrawGuiSpritesSystem;
import com.scs.splitscreenfps.game.systems.DrawModelSystem;
import com.scs.splitscreenfps.game.systems.DrawTextIn3DSpaceSystem;
import com.scs.splitscreenfps.game.systems.DrawTextSystem;
import com.scs.splitscreenfps.game.systems.HarmOnContactSystem;
import com.scs.splitscreenfps.game.systems.PhysicsSystem;
import com.scs.splitscreenfps.game.systems.PlayerInputSystem;
import com.scs.splitscreenfps.game.systems.PlayerMovementSystem;
import com.scs.splitscreenfps.game.systems.ProcessCollisionSystem;
import com.scs.splitscreenfps.game.systems.RemoveEntityAfterTimeSystem;
import com.scs.splitscreenfps.game.systems.RespawnPlayerSystem;
import com.scs.splitscreenfps.game.systems.SecondaryAbilitySystem;
import com.scs.splitscreenfps.game.systems.ShootingSystem;
import com.scs.splitscreenfps.pregame.PreGameScreen;
import com.scs.splitscreenfps.selectcharacter.GameSelectionData;

/**
 * This is the main game, where the players move about n'stuff.
 *
 */
public class Game implements IModule {

	private BillBoardFPS_Main main;
	private SpriteBatch batch2d;
	public BitmapFont font_small, font_med, font_large;
	public final ViewportData[] viewports;

	public AbstractPlayersAvatar[] players;
	public List<IInputMethod> inputs;
	public BasicECS ecs;
	public AbstractLevel currentLevel; // todo - use ILevelInterface

	private int game_stage;
	private long restartTime;
	private List<AbstractEntity> losers = new ArrayList<AbstractEntity>();
	private List<String> log = new LinkedList<String>();
	private GameSelectionData gameSelectionData;

	// Specific systems 
	private DrawModelSystem drawModelSystem;
	private PhysicsSystem physicsSystem;
	private RespawnPlayerSystem respawnSystem;

	public int currentViewId;
	public AssetManager assetManager = new AssetManager();
	private ProcessCollisionSystem coll;
	private btDefaultCollisionConfiguration collisionConfig;
	private btSequentialImpulseConstraintSolver constraintSolver;
	private final ClosestRayResultCallback callback = new ClosestRayResultCallback(new Vector3(), new Vector3());

	private DebugDrawer debugDrawer;
	private btBroadphaseInterface broadphase;
	private btCollisionDispatcher dispatcher;
	public btDiscreteDynamicsWorld dynamicsWorld;
	public boolean physics_enabled = true;
	private long startPhysicsTime;

	// Temp vars
	private Vector3 tmp_from = new Vector3();
	private Vector3 tmp_to = new Vector3();
	private Vector3 tmp_to2 = new Vector3();

	//private Matrix4 viewMatrix = new Matrix4();
	//private Texture backgroundTexture;
	//private SpriteBatch spritebatch;
	
	public Game(BillBoardFPS_Main _main, List<IInputMethod> _inputs, GameSelectionData _gameSelectionData) {
		main = _main;
		inputs = _inputs;
		gameSelectionData = _gameSelectionData;

		game_stage = 0;
		batch2d = new SpriteBatch();
		this.createECS();

		viewports = new ViewportData[4];
		players = new AbstractPlayersAvatar[inputs.size()];
		for (int i=0 ; i<players.length ; i++) {
			this.viewports[i] = new ViewportData(i, false, players.length);
		}

		collisionConfig = new btDefaultCollisionConfiguration();
		dispatcher = new btCollisionDispatcher(collisionConfig);
		broadphase = new btDbvtBroadphase();
		constraintSolver = new btSequentialImpulseConstraintSolver();
		dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig);
		dynamicsWorld.setGravity(new Vector3(0, -10f, 0));
		if (Settings.DEBUG_PHYSICS) {
			debugDrawer = new DebugDrawer();
			debugDrawer.setDebugMode(DebugDrawer.DebugDrawModes.DBG_MAX_DEBUG_DRAW_MODE);
			dynamicsWorld.setDebugDrawer(debugDrawer);
		}
		coll = new ProcessCollisionSystem(this);
		new MyContactListener(coll);

		//currentLevel = new RollingBallLevel(this);
		//currentLevel = new MapEditorLevel(this);
		//currentLevel = new LoadMapLevel(this);
		//currentLevel = new AvoidTheBallsLevel(this);
		//currentLevel = new CastleLevel(this);
		//currentLevel = new IliosLevel(this);
		currentLevel = new BuildingSiteLevel(this);

		for (int i=0 ; i<players.length ; i++) {
			//players[i] = new PlayersAvatar_Person(this, i, viewports[i], inputs.get(i), gameSelectionData.character[i]);
			players[i] = AvatarFactory.createAvatar(this, i, viewports[i], inputs.get(i), gameSelectionData.character[i]);
			ecs.addEntity(players[i]);

			//AbstractEntity weapon = EntityFactory.playersWeapon(ecs, players[i]);
			//ecs.addEntity(weapon);

			Camera cam = players[i].camera;
			//cam.lookAt(7, 0.4f, 7); //makes camera slightly slanted?
			cam.update();
		}	

		loadLevel();
		this.loadAssetsForRescale(); // Need this to load font

		startPhysicsTime = System.currentTimeMillis() + 500; // Don't start physics straight away.

		this.appendToLog("Game about to start...");

		//spritebatch = new SpriteBatch();
		//backgroundTexture = new Texture(Gdx.files.internal("textures/sky.png"), Format.RGB565, true);
		//backgroundTexture.setFilter(TextureFilter.MipMap, TextureFilter.Linear);
	}


	private void loadAssetsForRescale() {
		//this.currentLevel.loadAssets();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SHOWG.TTF"));

		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = Gdx.graphics.getBackBufferHeight()/30;
		//Settings.p("Font size=" + parameter.size);
		font_small = generator.generateFont(parameter);

		parameter = new FreeTypeFontParameter();
		parameter.size = Gdx.graphics.getBackBufferHeight()/20;
		//Settings.p("Font size=" + parameter.size);
		font_med = generator.generateFont(parameter);

		parameter = new FreeTypeFontParameter();
		parameter.size = Gdx.graphics.getBackBufferHeight()/10;
		//Settings.p("Font size=" + parameter.size);
		font_large = generator.generateFont(parameter);

		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		DrawGuiSpritesSystem sys = (DrawGuiSpritesSystem)this.ecs.getSystem(DrawGuiSpritesSystem.class);
		sys.rescaleSprites();
	}


	public void resizeViewports(boolean full_screen) {
		for (int i=0 ; i<players.length ; i++) {
			this.viewports[i].resize(i, false, players.length);
		}
	}


	private void createECS() {
		ecs = new BasicECS();
		ecs.addSystem(new PlayerInputSystem(this));
		ecs.addSystem(new DrawDecalSystem(this, ecs));
		ecs.addSystem(new CycleThruDecalsSystem(ecs));
		ecs.addSystem(new CycleThroughModelsSystem(ecs));
		ecs.addSystem(new PlayerMovementSystem(this, ecs));
		ecs.addSystem(new RemoveEntityAfterTimeSystem(ecs));
		ecs.addSystem(new DrawTextSystem(ecs, this, batch2d));
		ecs.addSystem(new AnimationSystem(ecs));
		ecs.addSystem(new DrawGuiSpritesSystem(ecs, this, this.batch2d));
		ecs.addSystem(new ExplodeAfterTimeSystem(this, ecs));
		ecs.addSystem(new BulletSystem(ecs, this));
		ecs.addSystem(new ShootingSystem(ecs, this));
		this.drawModelSystem = new DrawModelSystem(this, ecs);
		ecs.addSystem(this.drawModelSystem);
		ecs.addSystem(new DrawTextIn3DSpaceSystem(ecs, this, batch2d));
		physicsSystem = new PhysicsSystem(this, ecs);
		ecs.addSystem(physicsSystem);
		this.respawnSystem = new RespawnPlayerSystem(ecs);
		ecs.addSystem(new HarmOnContactSystem(this, ecs));
		ecs.addSystem(new SecondaryAbilitySystem(ecs, this));

	}


	private void loadLevel() {
		currentLevel.load();

		// Set start position of players
		for (int idx=0 ; idx<players.length  ; idx++) {
			//PositionComponent posData = (PositionComponent)this.players[idx].getComponent(PositionComponent.class);
			Vector3 start_pos = currentLevel.getPlayerStartPoint(idx);

			PhysicsComponent md = (PhysicsComponent)this.players[idx].getComponent(PhysicsComponent.class);
			Matrix4 mat = new Matrix4();
			mat.setTranslation(start_pos.x + 0.5f, start_pos.y, start_pos.z + 0.5f);
			md.body.setWorldTransform(mat);

			// Look down the z-axis
			this.viewports[idx].camera.direction.x = 0;
			this.viewports[idx].camera.direction.z = 1;
			this.viewports[idx].camera.update();
		}
	}


	@Override
	public void render() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			//this.mapBuilderSystem.saveMap();
			if (Settings.AUTO_START) {
				System.exit(0);
				return;
			}
			//BillBoardFPS_Main.audio.stopMusic();
			this.main.next_module = new PreGameScreen(main);
		}

		if (this.game_stage == 1) {
			if (this.restartTime < System.currentTimeMillis()) {
				this.main.next_module = new Game(main, this.inputs, gameSelectionData);
				return;
			}
		}

		this.currentLevel.update();

		this.ecs.events.clear();
		this.respawnSystem.process();
		this.ecs.getSystem(RemoveEntityAfterTimeSystem.class).process();
		this.ecs.addAndRemoveEntities();		
		this.ecs.processSystem(SecondaryAbilitySystem.class); // Must be before player movement system
		this.ecs.getSystem(PlayerInputSystem.class).process();
		this.ecs.getSystem(PlayerMovementSystem.class).process();

		if (physics_enabled) {
			if (System.currentTimeMillis() > startPhysicsTime) { // Don't start straight away
				// This must be run after the player has made inputs, but before the systems that process collisions!
				final float delta = Math.min(1f / 30f, Gdx.graphics.getDeltaTime());
				dynamicsWorld.stepSimulation(delta, 5, 1f/60f);
			}
		}

		this.ecs.processSystem(BulletSystem.class);
		this.ecs.processSystem(ShootingSystem.class);
		this.ecs.getSystem(PhysicsSystem.class).process();
		this.ecs.getSystem(AnimationSystem.class).process();
		this.ecs.getSystem(CycleThruDecalsSystem.class).process();
		this.ecs.getSystem(CycleThroughModelsSystem.class).process();
		this.ecs.getSystem(ExplodeAfterTimeSystem.class).process();
		this.ecs.getSystem(HarmOnContactSystem.class).process();

		for (currentViewId=0 ; currentViewId<players.length ; currentViewId++) {
			ViewportData viewportData = this.viewports[currentViewId];

			Gdx.gl.glViewport(viewportData.viewPos.x, viewportData.viewPos.y, viewportData.viewPos.width, viewportData.viewPos.height);

			viewportData.frameBuffer.begin();

			this.currentLevel.setBackgroundColour();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

			/*
			viewMatrix.setToOrtho2D(0, 0, 480, 320);
			spritebatch.setProjectionMatrix(viewMatrix);
			spritebatch.begin();
			spritebatch.disableBlending();
			//spritebatch.setColor(Color.WHITE);
			spritebatch.draw(backgroundTexture, 0, 0, 480, 320, 0, 0, 128, 128, false, false);
			spritebatch.end();
			*/

			this.drawModelSystem.process(viewportData.camera, false);
			this.ecs.getSystem(DrawDecalSystem.class).process();

			if (Settings.DEBUG_PHYSICS) {
				debugDrawer.begin(viewportData.camera);
				dynamicsWorld.debugDrawWorld();
				debugDrawer.end();
			}

			this.drawModelSystem.process(viewportData.camera, true);

			batch2d.begin();
			this.ecs.getSystem(DrawTextIn3DSpaceSystem.class).process();
			this.ecs.getSystem(DrawTextSystem.class).process();
			this.ecs.getSystem(DrawGuiSpritesSystem.class).process();

			//font_white.draw(batch2d, "Screen " + this.currentViewId, 10, 250);
			/*if (this.game_stage == 1) {
				if (this.losers.contains(this.players[this.currentViewId])) {
					font.setColor(0, 1, 0, 1);
					font.draw(batch2d, "YOU HAVE LOST!", 10, Gdx.graphics.getBackBufferHeight()/2);
				} else {
					font.setColor(0, 1, 1, 1);
					font.draw(batch2d, "YOU HAVE WON!", 10, Gdx.graphics.getBackBufferHeight()/2);
				}
			}*/

			currentLevel.renderUI(batch2d, currentViewId);
			float yOff = font_med.getLineHeight() * 1.2f;
			font_med.setColor(1, 1, 1, 1);
			PlayerData playerData = (PlayerData)players[currentViewId].getComponent(PlayerData.class);
			font_med.draw(batch2d, "Points: " + (int)(playerData.points), 10, (yOff*2));
			font_med.draw(batch2d, "Health: " + (int)(playerData.health), 10, (yOff*4));
			//font_med.draw(batch2d, this.scoreSystem.getHudText(playerData.side), 10, (yOff*5));

			if (currentViewId == 0 ) {
				// Draw log
				font_small.setColor(1,  1,  1,  1);
				int y = (int)(Gdx.graphics.getHeight()*0.4);// - 220;
				for (String s :this.log) {
					font_small.draw(batch2d, s, 10, y);
					y -= this.font_small.getLineHeight();
				}
			}

			/*if (Settings.TEST_SCREEN_COORDS) {
				font.draw(batch2d, "TL", 20, 20);
				font.draw(batch2d, "50", 50, 50);
				font.draw(batch2d, "150", 150, 150);
				font.draw(batch2d, "TR", Gdx.graphics.getBackBufferWidth()-20, 20);
				font.draw(batch2d, "BL", 10, Gdx.graphics.getBackBufferHeight()-20);
				font.draw(batch2d, "BR", Gdx.graphics.getBackBufferWidth()-20, Gdx.graphics.getBackBufferHeight()-20);
			}*/

			batch2d.end();

			viewportData.frameBuffer.end();

			//Draw buffer and FPS
			batch2d.begin();
			batch2d.draw(viewportData.frameBuffer.getColorBufferTexture(), viewportData.viewPos.x, viewportData.viewPos.y+viewportData.viewPos.height, viewportData.viewPos.width, -viewportData.viewPos.height);
			if (Settings.SHOW_FPS) {
				if (font_small != null) {
					font_small.draw(batch2d, "FPS: "+Gdx.graphics.getFramesPerSecond(), 10, font_small.getLineHeight()*2);
				}
			}
			batch2d.end();
		}
	}


	@Override
	public void resize(int w, int h) {
		this.loadAssetsForRescale();
	}


	@Override
	public void dispose() {
		for (currentViewId=0 ; currentViewId<players.length ; currentViewId++) {
			ViewportData viewportData = this.viewports[currentViewId];
			viewportData.dispose();
		}
		if (font_small != null) {
			font_small.dispose();
		}
		if (font_med != null) {
			font_med.dispose();
		}
		if (font_large != null) {
			font_large.dispose();
		}
		batch2d.dispose();
	}


	@Override
	public void setFullScreen(boolean fullscreen) {
		batch2d.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		this.resizeViewports(true);
	}


	public void playerHasLost(AbstractEntity avatar) {
		if (this.game_stage != 0) {
			return;
		}

		this.losers.clear();
		this.losers.add(avatar);
		loadWinLoseText();
	}


	public void playerHasWon(AbstractEntity winner) {
		if (this.game_stage != 0) {
			return;
		}

		this.losers.clear();
		for(AbstractEntity player : this.players) {
			if (player != winner) {
				this.losers.add(player);
			}
		}
		loadWinLoseText();
	}


	private void loadWinLoseText() {
		for (int i=0 ; i<this.players.length ; i++) {
			if (this.losers.contains(this.players[i])) {
				TextEntity te = new TextEntity(ecs, "YOU HAVE LOST!", Gdx.graphics.getBackBufferHeight()/2, 5, new Color(0, 0, 1, 1), i, 2);
				ecs.addEntity(te);
			} else {
				TextEntity te = new TextEntity(ecs, "YOU HAVE WON!", Gdx.graphics.getBackBufferHeight()/2, 5, new Color(0, 1, 0, 1), i, 1);
				ecs.addEntity(te);
			}
		}
		this.game_stage = 1;
		this.restartTime = System.currentTimeMillis() + 3000;
	}


	public void startSpecificLevel(int level) {
		this.game_stage = 1;
		this.restartTime = 0;

	}


	public List<AbstractEntity> getCollidedEntities(AbstractEntity e) {
		List<AbstractEntity> list = new ArrayList<AbstractEntity>();
		Iterator<AbstractEvent> it = ecs.events.iterator();
		while (it.hasNext()) {
			AbstractEvent evt = it.next();
			if (evt.getClass().equals(EventCollision.class)) {
				EventCollision coll = (EventCollision)evt;
				if (coll.entity1 == e) {
					list.add(coll.entity2);
				} else if (coll.entity2 == e) {
					list.add(coll.entity1);
				}
			}
		}
		return list;
	}


	public void playerDamaged(AbstractEntity player, PlayerData playerHitData, float amt, AbstractEntity shooter) {
		if (playerHitData.health <= 0) {
			// Already dead
			return;
		}

		Settings.p("Player " + playerHitData.playerIdx + " damaged " + amt);
		playerHitData.health -= amt;//bullet.settings.damage;

		AbstractEntity redfilter = GraphicsEntityFactory.createRedFilter(ecs, playerHitData.playerIdx);
		redfilter.addComponent(new RemoveEntityAfterTimeComponent(1));
		ecs.addEntity(redfilter);

		playerHitData.last_person_to_hit_them = shooter;

		if (playerHitData.health <= 0) {
			playerDied(player, playerHitData, shooter);
		}

	}


	public void playerDied(AbstractEntity player, PlayerData playerData, AbstractEntity shooter) {
		playerData.health = 0;
		this.respawnSystem.addEntity(player, this.currentLevel.getPlayerStartPoint(playerData.playerIdx));

		if (shooter != null) {
			PlayerData shooterData = (PlayerData)shooter.getComponent(PlayerData.class);
			shooterData.points += 1;

			if (shooterData.points >= Settings.POINTS_TO_WIN) {
				playerHasWon(shooter);
			}
		}
	}


	public void explosion(final Vector3 pos, float range, float force, float width_height) {
		//Settings.p("Explosion at " + pos);

		main.audio.play("sfx/explosion1.mp3");

		AbstractEntity expl = GraphicsEntityFactory.createNormalExplosion(ecs, pos, width_height);
		ecs.addEntity(expl);

		// Temp vars
		Matrix4 mat = new Matrix4();
		Vector3 vec = new Vector3();

		Iterator<AbstractEntity> it = this.physicsSystem.getEntityIterator();
		while (it.hasNext()) {
			AbstractEntity e = it.next();
			if (e.isMarkedForRemoval()) {
				continue;
			}
			PhysicsComponent pc = (PhysicsComponent)e.getComponent(PhysicsComponent.class);
			if (pc.body.getInvMass() != 0) {
				pc.body.getWorldTransform(mat);
				mat.getTranslation(vec);
				float distance = vec.dst(pos);
				if (distance <= range) {
					pc.body.activate();
					pc.body.applyCentralImpulse(vec.cpy().sub(pos).nor().scl(force));
					//Settings.p("Moving " + e.name);
				}
			}
		}
	}


	public btCollisionObject rayTestByDir(Vector3 ray_from, Vector3 dir, float range) {
		tmp_to.set(ray_from).mulAdd(dir, range);

		callback.setCollisionObject(null);
		callback.setClosestHitFraction(1f);

		callback.getRayFromWorld(tmp_from);
		tmp_from.set(ray_from);

		callback.getRayToWorld(tmp_to2);
		tmp_to2.set(tmp_to);

		this.dynamicsWorld.rayTest(ray_from, tmp_to2, callback);
		if (callback.hasHit()) {
			return callback.getCollisionObject();
		}

		return null;
	}


	private void appendToLog(String s) {
		this.log.add(s);
		while (log.size() > 6) {
			log.remove(0);
		}
	}


	class MyContactListener extends ContactListener {

		private ProcessCollisionSystem coll;

		public MyContactListener(ProcessCollisionSystem _coll) {
			coll = _coll;
		}

		@Override
		public boolean onContactAdded (int userValue0, int partId0, int index0, int userValue1, int partId1, int index1) {
			return true;
		}

		@Override
		public void onContactStarted (btCollisionObject ob1, btCollisionObject ob2) {
			try {
				//Settings.p(ob1.userData + " collided with " + ob2.userData);
				AbstractEntity e1 = (AbstractEntity)ob1.userData;
				AbstractEntity e2 = (AbstractEntity)ob2.userData;

				coll.processCollision(e1, e2);
			} catch (Exception ex) {
				Settings.pe(ex.getMessage());
			}
		}

		@Override
		public void onContactProcessed (int userValue0, int userValue1) {
			//Settings.p("Here");
		}

	}

}

