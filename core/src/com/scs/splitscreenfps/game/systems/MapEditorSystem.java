package com.scs.splitscreenfps.game.systems;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObject;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.Game;
import com.scs.splitscreenfps.game.components.PhysicsComponent;
import com.scs.splitscreenfps.game.components.PlayerData;
import com.scs.splitscreenfps.game.entities.AbstractPlayersAvatar;
import com.scs.splitscreenfps.game.input.MouseAndKeyboardInputMethod;
import com.scs.splitscreenfps.game.mapdata.MapBlockComponent;

public class MapEditorSystem extends AbstractSystem {

	private enum Mode {ROTATION, POSITION, SIZE};

	private Game game;
	private Mode mode = Mode.POSITION;
	public String mode_text = "";
	public String selected_text = "";
	private AbstractEntity selectedObject;

	public MapEditorSystem(BasicECS ecs, Game _game) {
		super(ecs, PlayerData.class);

		game = _game;

		game.physics_enabled = false;
	}


	@Override
	public void processEntity(AbstractEntity entity) {
		AbstractPlayersAvatar player = (AbstractPlayersAvatar)entity;

		if (player.inputMethod instanceof MouseAndKeyboardInputMethod == false) {
			return;
		}

		MouseAndKeyboardInputMethod keyboard = (MouseAndKeyboardInputMethod)player.inputMethod;

		if (keyboard.isMouseClicked()) {
			btCollisionObject obj = game.rayTestByDir(player.camera.position, player.camera.direction, 100);
			selectedObject = null;
			if (obj != null) {
				selectedObject = (AbstractEntity)obj.userData;

				MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
				if (block != null) {
					Settings.p(block.name + " selected");
					this.selected_text = "Selected: " + block.id;
				} else {
					selectedObject = null;
				}
			} else {
				Settings.p("Nothing selected");
			}
		}

		if (keyboard.isKeyJustPressed(Keys.NUM_1)) { // Save
			this.saveMap();
		}

		if (keyboard.isKeyJustPressed(Keys.P)) {
			mode = Mode.POSITION;
			mode_text = "Mode: Position";
			game.appendToLog("Position mode selected");
		} else if (keyboard.isKeyJustPressed(Keys.B)) {
			//MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
			//if (block.model_filename.length() == 0) {
			mode = Mode.SIZE;
			mode_text = "Mode: Size";
			/*} else {
				Settings.p("Cannot adjust size of model");
			}*/
		} else if (keyboard.isKeyJustPressed(Keys.R)) {
			mode = Mode.ROTATION;
			mode_text = "Mode: Rotation";
		} else if (keyboard.isKeyJustPressed(Keys.T)) { // Toggle physics
			game.physics_enabled = !game.physics_enabled;
			Settings.p("Physics enabled: " + game.physics_enabled);
		} else if (keyboard.isKeyJustPressed(Keys.N)) {  // New block
			MapBlockComponent block = new MapBlockComponent();
			block.size = new Vector3(1, 1, 1);
			this.selectedObject = game.currentLevel.createAndAddEntityFromBlockData(block);
			game.currentLevel.mapdata.blocks.add(block);
		}
		if (selectedObject != null) {
			if (keyboard.isKeyJustPressed(Keys.C)) { // Clone
				MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
				MapBlockComponent new_block = block.clone();
				this.selectedObject = game.currentLevel.createAndAddEntityFromBlockData(new_block);
				game.currentLevel.mapdata.blocks.add(new_block);
			} else if (keyboard.isKeyJustPressed(Keys.X)) { // Remove
				MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
				game.currentLevel.mapdata.blocks.remove(block);
				this.selectedObject.remove();
			} else if (keyboard.isKeyJustPressed(Keys.NUM_0)) { // Reset rotation
				resetBlockRotation();
			} else if (keyboard.isKeyJustPressed(Keys.LEFT)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(.1f, 0, 0));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(.1f, 0, 0));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(10, 0, 0));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			} else if (keyboard.isKeyJustPressed(Keys.PAGE_UP)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(0, 0, .1f));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(0, 0, .1f));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(0, 0, 10));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			} else if (keyboard.isKeyJustPressed(Keys.RIGHT)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(-.1f, 0, 0));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(-.1f, 0, 0));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(-10, 0, 0));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			} else if (keyboard.isKeyJustPressed(Keys.PAGE_DOWN)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(0, 0, -.1f));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(0, 0, -.1f));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(0, 0, -10));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			} else if (keyboard.isKeyJustPressed(Keys.UP)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(0, 0.1f, 0));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(0, .1f, 0));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(0, 10, 0));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			} else if (keyboard.isKeyJustPressed(Keys.DOWN)) {
				switch (mode) {
				case POSITION:
					this.moveBlock(new Vector3(0, -0.1f, 0));
					break;
				case SIZE:
					this.resizeBlock(new Vector3(0, -0.1f, 0));
					break;
				case ROTATION:
					this.rotateBlock(new Vector3(0, -10, 0));
					break;
				default:
					throw new RuntimeException("Todo");
				}
			}
		}
	}


	private void moveBlock(Vector3 off) {
		MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
		Matrix4 mat = this.setBlockDataToCurrentPosition(block);
		PhysicsComponent md = (PhysicsComponent)selectedObject.getComponent(PhysicsComponent.class);
		block.position.add(off);
		mat.setTranslation(block.position);
		md.body.setWorldTransform(mat);
		md.body.activate();
	}


	private void resizeBlock(Vector3 adj) {
		MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
		this.setBlockDataToCurrentPosition(block);

		block.size.add(adj); 
		this.selectedObject.remove();
		this.selectedObject = game.currentLevel.createAndAddEntityFromBlockData(block);
	}


	private void rotateBlock(Vector3 adj) {
		MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
		this.setBlockDataToCurrentPosition(block);

		block.rotation.add(adj);
		this.selectedObject.remove();
		this.selectedObject = game.currentLevel.createAndAddEntityFromBlockData(block);
	}


	private void resetBlockRotation() {
		MapBlockComponent block = (MapBlockComponent)this.selectedObject.getComponent(MapBlockComponent.class);
		this.setBlockDataToCurrentPosition(block);

		block.rotation.set(0, 0, 0);
		this.selectedObject.remove();
		this.selectedObject = game.currentLevel.createAndAddEntityFromBlockData(block);
	}


	// Requird since physics may well have moved the position of the block
	private Matrix4 setBlockDataToCurrentPosition(MapBlockComponent block) {
		// Set matrix to current pos
		PhysicsComponent md = (PhysicsComponent)selectedObject.getComponent(PhysicsComponent.class);
		Matrix4 mat = new Matrix4();
		md.body.getWorldTransform(mat);
		mat.getTranslation(block.position);
		return mat;

	}


	public void saveMap() {
		try {
			// todo - loop through each block and set the model data to the position/size/rot of the block
			game.currentLevel.saveFile();
			Settings.p("Map saved");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
