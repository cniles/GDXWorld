package com.blastedstudios.gdxworld.plugin.mode.joint;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.blastedstudios.gdxworld.ui.leveleditor.VertexTable;
import com.blastedstudios.gdxworld.plugin.mode.joint.JointMode;
import com.blastedstudios.gdxworld.world.joint.GDXJoint;
import com.blastedstudios.gdxworld.world.joint.WeldJoint;

class WeldWindow extends BaseJointWindow {
	private final VertexTable anchorTable;
	private final TextField referenceAngleField;
	private final WeldJoint joint;

	public WeldWindow(Skin skin, JointMode mode, GDXJoint baseJoint) {
		super("Weld Editor", skin, JointType.WeldJoint, mode, baseJoint);
		this.joint =  (WeldJoint)baseJoint;
		anchorTable = new VertexTable(joint.getAnchor(), skin, null);
		referenceAngleField = new TextField(joint.getReferenceAngle()+"", skin);
		referenceAngleField.setMessageText("<reference angle>");
		add(new Label("Anchor: ", skin));
		add(anchorTable);
		row();
		add(new Label("Reference Angle: ", skin));
		add(referenceAngleField);
		row();
		add(createControlTable()).colspan(2);
		pack();
	}
	
	@Override public void apply(){
		super.apply();
		joint.setAnchor(anchorTable.getVertex());
		joint.setReferenceAngle(Float.parseFloat(referenceAngleField.getText()));
	}

	@Override public void clicked(Vector2 pos) {
		anchorTable.setVertex(pos.x, pos.y);
	}

	@Override public Vector2 getCenter() {
		return anchorTable.getVertex();
	}
}
