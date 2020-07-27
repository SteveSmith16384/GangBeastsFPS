package com.scs.splitscreenfps.game.systems;

import com.badlogic.gdx.Gdx;
import com.scs.basicecs.AbstractEntity;
import com.scs.basicecs.AbstractSystem;
import com.scs.basicecs.BasicECS;
import com.scs.splitscreenfps.Settings;
import com.scs.splitscreenfps.game.components.AnimatedComponent;

public class AnimationSystem extends AbstractSystem {

	public AnimationSystem(BasicECS ecs) {
		super(ecs,  AnimatedComponent.class);
	}


	@Override
	public void processEntity(AbstractEntity entity) {
		AnimatedComponent anim = (AnimatedComponent)entity.getComponent(AnimatedComponent.class);

		if (anim.getNextAnim() != null) {
			if (anim.current_animation != anim.getNextAnim()) {
				//Settings.p("Setting anim for " + entity + " to " + anim.getNextAnim());
				anim.current_animation = anim.getNextAnim();
				anim.animationController.animate(anim.current_animation.name, anim.current_animation.loop?-1:1, 2f, null, 0f);
				anim.setNextAnim(null);
			}
		}		
		anim.animationController.update(Gdx.graphics.getDeltaTime());
	}

}
