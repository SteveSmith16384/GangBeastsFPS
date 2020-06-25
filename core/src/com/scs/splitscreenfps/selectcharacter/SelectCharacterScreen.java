package com.scs.splitscreenfps.selectcharacter;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.scs.splitscreenfps.BillBoardFPS_Main;
import com.scs.splitscreenfps.IModule;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.AvatarFactory;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.input.IInputMethod;

public class SelectCharacterScreen implements IModule {

	private SpriteBatch batch2d;
	private BitmapFont font_small, font_large;
	private List<String> log = new LinkedList<String>();
	private FrameBuffer frameBuffer;
	private BillBoardFPS_Main main;
	public List<IInputMethod> inputs;
	private Sprite logo;
	private GameSelectionData gameSelectionData;

	// Gfx pos data
	private int spacing_x;
	private Sprite arrow;

	public SelectCharacterScreen(BillBoardFPS_Main _main, List<IInputMethod> _inputs) {
		super();

		main = _main;
		inputs = _inputs;

		this.gameSelectionData = new GameSelectionData(inputs.size());

		batch2d = new SpriteBatch();

		//frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Settings.LOGICAL_SIZE_PIXELS, Settings.LOGICAL_SIZE_PIXELS, true);
		frameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		loadAssetsForResize();

		this.appendToLog("Welcome to " + Settings.TITLE);

		spacing_x = Settings.LOGICAL_SIZE_PIXELS / (AvatarFactory.MAX_CHARS+1);

		//BillBoardFPS_Main.audio.startMusic("sfx/battleThemeA.mp3");
	}


	private void loadAssetsForResize() {
		batch2d = new SpriteBatch();

		//frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 1024, 1024, true);
		frameBuffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/SHOWG.TTF"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = Gdx.graphics.getBackBufferHeight()/30;
		//Settings.p("Font size=" + parameter.size);
		font_small = generator.generateFont(parameter); // font size 12 pixels
		parameter.size = Gdx.graphics.getBackBufferHeight()/10;
		//Settings.p("Font size=" + parameter.size);
		font_large = generator.generateFont(parameter); // font size 12 pixels
		generator.dispose(); // don't forget to dispose to avoid memory leaks!

		/*Texture logoTex = new Texture(Gdx.files.internal("logo1.png"));		
		logo = new Sprite(logoTex);
		logo.setBounds(0,  0 , Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		logo.setColor(0.4f, 0.4f, 0.4f, 1);
		 */

		arrow = new Sprite(new Texture(Gdx.files.internal("blue_arrow_down.png")));
	}


	@Override
	public void render() {
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			System.exit(0);
		}

/*		if (Settings.AUTO_START) {
			main.next_module = new Game(main, inputs);
			return;
		}
*/
		// Read inputs
		boolean all_selected = true;
		for (int playerIdx=0 ; playerIdx<this.inputs.size() ; playerIdx++) {
			IInputMethod input = this.inputs.get(playerIdx);
			if (this.gameSelectionData.selected_character[playerIdx] == false) {
				all_selected = false;
				if (input.getStrafeLeft() > .5f) {
					this.gameSelectionData.character[playerIdx]--;
					if (this.gameSelectionData.character[playerIdx] < 0) {
						this.gameSelectionData.character[playerIdx] = AvatarFactory.MAX_CHARS;
					}
				} else if (input.getStrafeRight() > .5f) {
					this.gameSelectionData.character[playerIdx]++;
					if (this.gameSelectionData.character[playerIdx] > AvatarFactory.MAX_CHARS) {
						this.gameSelectionData.character[playerIdx] = 0;
					}
				} 
			}
			if (input.isShootPressed()) {
				this.gameSelectionData.selected_character[playerIdx] = true;
			}
		}
		
		if (all_selected) {
			this.startGame();
			return;
		}

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		frameBuffer.begin();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		batch2d.begin();

		if (logo != null) {
			logo.draw(batch2d);
		}

		font_small.setColor(1,  1,  1,  1);

		// Draw characters
		int y_pos =  Settings.LOGICAL_SIZE_PIXELS/2;
		for (int i=0 ; i<AvatarFactory.MAX_CHARS ; i++) {
			int x_pos = spacing_x * (i+1);
			font_small.draw(batch2d, AvatarFactory.getName(i), x_pos, y_pos);
		}

		// Draw arrows
		for (int playerIdx=0 ; playerIdx<this.inputs.size() ; playerIdx++) {
			y_pos = y_pos + (30*playerIdx);
			int x_pos = spacing_x * (this.gameSelectionData.character[playerIdx]+1);

			arrow.setBounds(x_pos,  y_pos , 30, 30);
			arrow.draw(batch2d);
		}

		// Draw log
		int y = (int)(Gdx.graphics.getHeight()*0.4);// - 220;
		for (String s :this.log) {
			font_small.draw(batch2d, s, 10, y);
			y -= this.font_small.getLineHeight();
		}

		if (Settings.TEST_SCREEN_COORDS) {
			font_small.draw(batch2d, "TL", 20, 20);
			font_small.draw(batch2d, "50", 50, 50);
			font_small.draw(batch2d, "150", 150, 150);
			font_small.draw(batch2d, "TR", Gdx.graphics.getBackBufferWidth()-20, 20);
			font_small.draw(batch2d, "BL", 10, Gdx.graphics.getBackBufferHeight()-20);
			font_small.draw(batch2d, "BR", Gdx.graphics.getBackBufferWidth()-20, Gdx.graphics.getBackBufferHeight()-20);
		}

		batch2d.end();

		frameBuffer.end();

		//Draw buffer and FPS
		batch2d.begin();
		batch2d.draw(frameBuffer.getColorBufferTexture(), 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -Gdx.graphics.getHeight());

		/*if (Settings.SHOW_FPS) {
			font.draw(batch2d, "FPS: "+Gdx.graphics.getFramesPerSecond(), 10, font.getLineHeight());
		}*/

		batch2d.end();
	}


	private void startGame() {
		// Check all players have selected a character
		main.next_module = new Game(main, inputs, gameSelectionData);
	}


	@Override
	public void dispose() {
		this.batch2d.dispose();
		this.frameBuffer.dispose();
		this.font_small.dispose();
		this.font_large.dispose();
	}


	@Override
	public void setFullScreen(boolean fullscreen) {
		batch2d.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}


	@Override
	public void resize(int w, int h) {
		this.loadAssetsForResize();
	}


	private void appendToLog(String s) {
		this.log.add(s);
		while (log.size() > 6) {
			log.remove(0);
		}
	}

}
