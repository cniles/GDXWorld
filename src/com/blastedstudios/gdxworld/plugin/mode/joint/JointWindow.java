package com.blastedstudios.gdxworld.plugin.mode.joint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.gdxworld.ui.AbstractWindow;
import com.blastedstudios.gdxworld.ui.leveleditor.LevelEditorScreen;
import com.blastedstudios.gdxworld.plugin.mode.joint.JointMode;
import com.blastedstudios.gdxworld.world.joint.DistanceJoint;
import com.blastedstudios.gdxworld.world.joint.GDXJoint;
import com.blastedstudios.gdxworld.world.joint.RevoluteJoint;
import com.blastedstudios.gdxworld.world.joint.WeldJoint;

class JointWindow extends AbstractWindow {
	private BaseJointWindow baseWindow;
	private final Skin skin;
	private final JointMode mode;
	private final LevelEditorScreen levelEditorScreen;
	
	public JointWindow(final Skin skin, final LevelEditorScreen levelEditorScreen,
			final JointMode mode) {
		super("Joint Editor", skin);
		this.skin = skin;
		this.mode = mode;
		this.levelEditorScreen = levelEditorScreen;
		final List typeList = new List(JointType.values(), skin);
		final Button newButton = new TextButton("New", skin);
		newButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				createBaseWindow(JointType.values()[typeList.getSelectedIndex()], null);
			}
		});
		add(typeList);
		row();
		add(newButton);
		setMovable(false);
		pack();
	}
	
	private void createBaseWindow(JointType type, GDXJoint joint){
		if(baseWindow != null)
			baseWindow.remove();
		switch(type){
		case DistanceJoint:
			DistanceJoint djoint = joint == null ? new DistanceJoint() : (DistanceJoint)joint;
			baseWindow = new DistanceWindow(skin, mode, djoint);
			break;
		case WeldJoint:
			WeldJoint wjoint = joint == null ? new WeldJoint() : (WeldJoint)joint;
			baseWindow = new WeldWindow(skin, mode, wjoint);
			break;
		case RevoluteJoint:
			RevoluteJoint rjoint = joint == null ? new RevoluteJoint() : (RevoluteJoint)joint;
			baseWindow = new RevoluteWindow(skin, mode, rjoint);
			break;
		default:
			Gdx.app.log("JointWindow.newButton.clicked", "Joint not implemented: " + type);
			break;
		}
		if(baseWindow != null)
			levelEditorScreen.getStage().addActor(baseWindow);
	}

	public void clicked(Vector2 pos) {
		if(baseWindow != null)
			baseWindow.clicked(pos);
	}

	@Override public boolean remove(){
		removeBaseJointWindow();
		return super.remove();
	}

	public void removeBaseJointWindow() {
		if(baseWindow != null){
			baseWindow.remove();
			baseWindow = null;
		}
	}

	public boolean contains(float x, float y){
		return super.contains(x, y) || (baseWindow != null && baseWindow.contains(x, y));
	}

	public void setSelected(GDXJoint joint) {
		if(baseWindow == null)
			createBaseWindow(joint.getJointType(), joint);
	}

	public BaseJointWindow getBaseWindow() {
		return baseWindow;
	}
}
