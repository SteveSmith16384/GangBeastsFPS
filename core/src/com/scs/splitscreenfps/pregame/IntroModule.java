package com.scs.splitscreenfps.pregame;

import java.awt.Rectangle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.IModule;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.components.ChangeColourComponent;
import com.scs.splitscreenfps.game.components.HasGuiSpriteComponent;
import com.scs.splitscreenfps.game.entities.TextEntity;
import com.scs.splitscreenfps.game.systems.ChangeColourSystem;
import com.scs.splitscreenfps.game.systems.DrawGuiSpritesSystem;
import com.scs.splitscreenfps.game.systems.DrawTextSystem;
import com.scs.splitscreenfps.game.systems.dependencies.IGetCurrentViewport;

public class IntroModule implements IModule, IGetCurrentViewport {

	private final BillBoardFPS_Main main;
	private final BasicECS ecs;
	private final SpriteBatch spriteBatch;
	private FrameBuffer frameBuffer;
	private AssetManager assetManager = new AssetManager();
	private Rectangle viewRect;

	private DrawGuiSpritesSystem drawGuiSpritesSystem;
	private ChangeColourSystem colChangeSystem;
	private DrawTextSystem drawTextSystem;
	
	public IntroModule(BillBoardFPS_Main _main) {
		super();

		main = _main;
		spriteBatch = new SpriteBatch();

		ecs = new BasicECS();
		drawGuiSpritesSystem = new DrawGuiSpritesSystem(ecs, this, spriteBatch);
		colChangeSystem = new ChangeColourSystem(ecs);
		this.drawTextSystem = new DrawTextSystem(ecs, this, this.spriteBatch);
		
		// Logo
		AbstractEntity entity = new AbstractEntity(ecs, "Logo");
		Texture weaponTex = this.getTexture("overblown_logo.png");
		Sprite sprite = new Sprite(weaponTex);
		HasGuiSpriteComponent hgsc = new HasGuiSpriteComponent(sprite, HasGuiSpriteComponent.Z_FILTER, new com.badlogic.gdx.math.Rectangle(0.2f, 0.6f, .6f, .3f));
		entity.addComponent(hgsc);
		ecs.addEntity(entity);

		// Text
		TextEntity text = new TextEntity(ecs, "PRESS SPACE TO START!", 50, 50, -1, Color.WHITE, 0, main.font_large, true);
		text.addComponent(new ChangeColourComponent(Color.WHITE, Color.RED, 300));
		ecs.addEntity(text);
		loadAssetsForResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		BillBoardFPS_Main.audio.startMusic("music/megasong.mp3");
	}


	private void loadAssetsForResize(int w, int h) {
		//this.viewRect = new Rectangle(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		this.viewRect = new Rectangle(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		//frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, true);//Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Settings.LOGICAL_SIZE_PIXELS, Settings.LOGICAL_SIZE_PIXELS, true);
		frameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		drawGuiSpritesSystem.rescaleSprites();
		drawTextSystem.rescaleText();

	}


	public Texture getTexture(String tex_filename) {
		assetManager.load(tex_filename, Texture.class);
		assetManager.finishLoading();
		Texture tex = assetManager.get(tex_filename);
		return tex;
	}


	@Override
	public void render() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			Gdx.app.exit();
		}

		if (Settings.AUTO_START || Settings.USE_MAP_EDITOR) {
			showPlayersJoinModule();
			return;
		}
		
		ecs.addAndRemoveEntities();
		colChangeSystem.process();
		
		Gdx.gl.glViewport(0, 0, Settings.LOGICAL_SIZE_PIXELS, Settings.LOGICAL_SIZE_PIXELS);//Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		frameBuffer.begin();
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		spriteBatch.begin();
		
		drawGuiSpritesSystem.process();
		drawTextSystem.process();
		
		spriteBatch.end();

		frameBuffer.end();

		//Draw buffer and FPS
		spriteBatch.begin();
		spriteBatch.draw(frameBuffer.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());
		spriteBatch.end();

		readKeyboard();
	}


	private void readKeyboard() {
		if (Gdx.input.isKeyJustPressed(Keys.S) || Gdx.input.isKeyJustPressed(Keys.SPACE)) {
			this.showPlayersJoinModule();
		}
	}


	private void showPlayersJoinModule() {
		main.next_module = new PlayersJoinGameModule(main);
	}


	@Override
	public void dispose() {
		this.spriteBatch.dispose();
		this.frameBuffer.dispose();
		assetManager.dispose();
		ecs.dispose();

	}


	@Override
	public void setFullScreen(boolean fullscreen) {
		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}


	@Override
	public void resize(int w, int h) {
		this.loadAssetsForResize(w, h);
	}


	@Override
	public void controlledAdded(Controller controller) {
		// Do nothing
	}


	@Override
	public void controlledRemoved(Controller controller) {
		// Do nothing
	}


	@Override
	public int getCurrentViewportIdx() {
		return 0;
	}


	@Override
	public Rectangle getCurrentViewportRect() {
		return viewRect;
	}

}
