package com.blastedstudios.gdxworld.plugin.mode.quest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.BeingSpawnManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.DialogManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.EndLevelManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.ManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.PhysicsManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.manifestation.RevoluteJointManifestationTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.AABBTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.ActivateTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.InputTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.KillTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.ObjectDistanceTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.PersonTriggerTable;
import com.blastedstudios.gdxworld.plugin.mode.quest.trigger.TriggerTable;
import com.blastedstudios.gdxworld.ui.AbstractWindow;
import com.blastedstudios.gdxworld.world.quest.GDXQuest;
import com.blastedstudios.gdxworld.world.quest.manifestation.AbstractQuestManifestation;
import com.blastedstudios.gdxworld.world.quest.trigger.AbstractQuestTrigger;

class QuestEditor extends AbstractWindow {
	private ManifestationTable manifestationTable;
	private TriggerTable triggerTable;
	private final Table parentManifestationTable, parentTriggerTable;
	private final List<CheckBox> manifestationBoxes = new ArrayList<>(),
			triggerBoxes = new ArrayList<>();
	
	public QuestEditor(final GDXQuest quest, final Skin skin, final QuestTable questTable) {
		super("Quest Editor", skin);
		final TextField nameField = new TextField("", skin);
		nameField.setMessageText("<quest name>");
		nameField.setText(quest.getName());
		final TextField prerequisiteField = new TextField("", skin);
		prerequisiteField.setMessageText("<prerequisites>");
		prerequisiteField.setText(quest.getPrerequisites());
		final CheckBox repeatableBox = new CheckBox("Repeatable", skin);
		repeatableBox.setChecked(quest.isRepeatable());
		final Button acceptButton = new TextButton("Accept", skin);
		final Button cancelButton = new TextButton("Cancel", skin);
		acceptButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				quest.setName(nameField.getText());
				quest.setPrerequisites(prerequisiteField.getText());
				quest.setTrigger(triggerTable.apply());
				quest.setManifestation(manifestationTable.apply());
				quest.setRepeatable(repeatableBox.isChecked());
				questTable.setName(quest.getName());
				remove();
			}
		});
		cancelButton.addListener(new ClickListener() {
			@Override public void clicked(InputEvent event, float x, float y) {
				remove();
			}
		});
		parentManifestationTable = new Table();
		parentTriggerTable = new Table();
		createManifestationTable(skin, quest.getManifestation());
		createTriggerTable(skin, quest.getTrigger());

		createQuestComponent(skin, manifestationBoxes, AbstractQuestManifestation.class, 
				quest.getManifestation(), BeingSpawnManifestationTable.class, 
				DialogManifestationTable.class,	EndLevelManifestationTable.class, 
				PhysicsManifestationTable.class, RevoluteJointManifestationTable.class);
		createQuestComponent(skin, triggerBoxes, AbstractQuestTrigger.class, 
				quest.getTrigger(), AABBTriggerTable.class, 
				ActivateTriggerTable.class, KillTriggerTable.class, 
				InputTriggerTable.class,
				ObjectDistanceTriggerTable.class, PersonTriggerTable.class);
		
		add(new Label("Name: ", skin));
		add(nameField);
		add(repeatableBox);
		row();
		add(new Label("Prerequisites: ", skin));
		add(prerequisiteField);
		row();
		add(new Label("Trigger Type: ", skin));
		for(CheckBox box : triggerBoxes)
			add(box);
		row();
		add(parentTriggerTable).colspan(3);
		row();
		add(new Label("Manifestation Type: ", skin));
		for(CheckBox box : manifestationBoxes)
			add(box);
		row();
		add(parentManifestationTable).colspan(3);
		row();
		Table controlTable = new Table();
		controlTable.add(acceptButton);
		controlTable.add(cancelButton);
		add(controlTable).colspan(3);
		pack();
		setX(Gdx.graphics.getWidth());
		setMovable(false);
	}
	
	private void createQuestComponent(final Skin skin, final List<CheckBox> checkBoxes, 
			final Class<?> componentClass, Object current, Class<?>... tableClasses){
		for(Class<?> tableClass : tableClasses){
			try {
				String boxText = (String) tableClass.getField("BOX_TEXT").get(tableClass);
				final CheckBox box = new CheckBox(boxText, skin);
				final Class<?> clazz = Class.forName(componentClass.getPackage().getName() + "." + 
						tableClass.getSimpleName().replaceAll("Table", ""));
				final Object defaultValue = clazz.getField("DEFAULT").get(clazz);
				checkBoxes.add(box);
				if(tableClass.getSimpleName().startsWith(current.getClass().getSimpleName()))
					box.setChecked(true);
				box.addListener(new ClickListener() {
					@Override public void clicked(InputEvent event, float x, float y) {
						if(componentClass == AbstractQuestManifestation.class)
							createManifestationTable(skin, defaultValue);
						else
							createTriggerTable(skin, defaultValue);
						for(CheckBox cbox : checkBoxes)
							cbox.setChecked(cbox == box);
						pack();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	private ManifestationTable createManifestationTable(Skin skin, Object manifestation){
		if(manifestationTable != null)
			manifestationTable.remove();
		manifestationTable = (ManifestationTable) createTable(ManifestationTable.class, manifestation, skin);
		parentManifestationTable.clear();
		parentManifestationTable.add(manifestationTable);
		return manifestationTable;
	}
	
	private TriggerTable createTriggerTable(Skin skin, Object trigger){
		if(triggerTable != null)
			triggerTable.remove();
		triggerTable = (TriggerTable) createTable(TriggerTable.class, trigger, skin);
		parentTriggerTable.clear();
		parentTriggerTable.add(triggerTable);
		return triggerTable;
	}
	
	/**
	 * @return Trigger or Manifestation table, needs to be named correctly
	 */
	private Table createTable(Class<?> tableClass, Object object, Skin skin){
		try {
			Class<?> clazz = Class.forName(tableClass.getPackage().getName() + "." + object.getClass().getSimpleName() + "Table");
			Method cloneMeth = object.getClass().getMethod("clone");
			return (Table) clazz.getConstructor(Skin.class, object.getClass()).newInstance(skin, cloneMeth.invoke(object));
		} catch (Exception e) {
			Gdx.app.error("QuestEditor.createTable", "Exception making table, ensure class names are valid");
			e.printStackTrace();
		}
		return null;
	}

	public void touched(Vector2 coordinates) {
		if(manifestationTable != null)
			manifestationTable.touched(coordinates);
		if(triggerTable != null)
			triggerTable.touched(coordinates);
		
	}
}
