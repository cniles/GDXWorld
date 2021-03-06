package com.blastedstudios.gdxworld.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.blastedstudios.gdxworld.util.FileUtil;
import com.blastedstudios.gdxworld.util.Properties;
import com.blastedstudios.gdxworld.world.GDXBackground;
import com.blastedstudios.gdxworld.world.GDXLevel;
import com.blastedstudios.gdxworld.world.shape.GDXShape;

public class GDXRenderer {
	private final float GDX_SCALE = .05f;
	private boolean drawBackground, drawShapes;
	private Map<String, Texture> textureMap;
	private SpriteBatch batch;
	private static final Texture EMPTY = new Texture(1,1,Format.RGBA4444);
	
	public GDXRenderer(boolean drawBackground, boolean drawShapes){
		this.drawBackground = drawBackground;
		this.drawShapes = drawShapes;
		textureMap = new HashMap<String, Texture>();
		batch = new SpriteBatch();
		if(!Properties.getBool("renderer.blend.enabled", true))
			batch.disableBlending();
	}
	
	public void render(GDXLevel level, OrthographicCamera camera, Iterable<Entry<GDXShape,Body>> bodies){
		batch.setProjectionMatrix(camera.combined);
        batch.begin();
        
		if(drawBackground)
			for(GDXBackground background : level.getBackgrounds())
				drawBackground(background, batch);
		if(drawShapes)
			for(Entry<GDXShape,Body> entry : bodies)
				drawShape(camera, entry.getKey(), entry.getValue(), batch);
		batch.end();
	}
	
	public void drawShape(OrthographicCamera camera, GDXShape shape, Body body, SpriteBatch batch){
		Texture texture = getTexture(shape.getName() + ".png");
		if(texture != null && !shape.getName().equals("") && body != null){
			Sprite sprite = new Sprite(texture);
			sprite.setScale(GDX_SCALE);
			sprite.setRotation((float)Math.toDegrees(body.getAngle()));
			sprite.setPosition(body.getPosition().x - texture.getWidth()/2f, body.getPosition().y - texture.getHeight()/2f);
			sprite.draw(batch);
		}
	}
	
	public void drawBackground(GDXBackground background, SpriteBatch batch){
		Texture texture = getTexture(background.getTexture());
		if(texture != null){
			//Disabling parallax for now
			//float depth = Math.max(background.getDepth(), .001f);
			Vector2 offset = new Vector2(texture.getWidth(),texture.getHeight()).scl(background.getScale()/2f);
			//Vector2 xy = toParallax(depth, background.getCoordinates(), camera).sub(offset);
			Vector2 xy = background.getCoordinates().cpy().sub(offset);
			batch.draw(texture, xy.x, xy.y, texture.getWidth()*background.getScale(), 
					texture.getHeight()*background.getScale());
		}
	}
	
	/**
	 * Convert from world coordinates to parallax screen coordinates
	 */
	public static Vector2 toParallax(float depth, Vector2 world, Camera camera){
		Vector2 camOffset = new Vector2(camera.position.x,camera.position.y).div(depth);
		return world.cpy().sub(camOffset);
	}

	public boolean isDrawBackground() {
		return drawBackground;
	}

	public void setDrawBackground(boolean drawBackground) {
		this.drawBackground = drawBackground;
	}

	public boolean isDrawShapes() {
		return drawShapes;
	}

	public void setDrawShapes(boolean drawShapes) {
		this.drawShapes = drawShapes;
	}
	
	public Texture getTexture(String name){
		if(!textureMap.containsKey(name)){
			FileHandle file = FileUtil.find(Gdx.files.internal("data"), name);
			if(file != null){
				try{
					textureMap.put(name, new Texture(file));
					Gdx.app.log("GDXRenderer.render", "Added texture " + name);
				}catch(Exception e){
					Gdx.app.error("GDXRenderer.render", "Texture found but error loading " + 
							name + ", using empty");
					textureMap.put(name, EMPTY);
				}
			}else{
				Gdx.app.error("GDXRenderer.render", "Texture " + name + " not found, using empty");
				textureMap.put(name, EMPTY);
			}
		}
		return textureMap.get(name);
	}
	
	public static Vector2 toWorldCoordinates(Camera cam, Vector2 screen){
		Vector3 coords = new Vector3(screen.x, screen.y, 0);
		cam.unproject(coords);
		return new Vector2(coords.x, coords.y);
	}

	public static Vector2 toScreenCoordinates(Camera cam, Vector2 world){
		Vector3 coords = new Vector3(world.x, world.y, 0);
		cam.project(coords);
		return new Vector2(coords.x, coords.y);
	}
}
