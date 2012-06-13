package com.block.land;

		import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;

		/**
		 * (c) 2010 Nicolas Gramlich
		 * (c) 2011 Zynga
		 *
		 * @author Nicolas Gramlich
		 * @since 18:47:08 - 19.03.2010
		 */
		public class CreateMode extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {
			// ===========================================================
			// Constants
			// ===========================================================
			public Sprite face;

			private BoundCamera mBoundChaseCamera;
			
			public Sprite wall;
			float mTouchX;
			float mTouchY;
			private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.2f);

			// ===========================================================
			// Fields
			// ===========================================================

			private BitmapTextureAtlas mBitmapTextureAtlas;
			private TiledTextureRegion mBoxFaceTextureRegion;
			
			private BitmapTextureAtlas mWallTextureAtlas;
			private TiledTextureRegion mWallTextureRegion;
			
			private Scene mScene;
			
			private PhysicsWorld mPhysicsWorld;
			private int mFaceCount = 0;
			public int gravityX=25,gravityY;
			public int wallCount=0;
			// ===========================================================
			// Constructors
			// ===========================================================
			private BitmapTextureAtlas mBackTextureAtlas;
			private TiledTextureRegion mBackTextureRegion;
			private BitmapTextureAtlas mBackTopTextureAtlas;
			private TiledTextureRegion mBackTopTextureRegion;

			// ===========================================================
			// Getter & Setter
			// ===========================================================

			// ===========================================================
			// Methods for/from SuperClass/Interfaces
			// ===========================================================

			public EngineOptions onCreateEngineOptions() {
				this.mBoundChaseCamera = new BoundCamera(360, 47, 800, 480, 0, 1512, -248, 480);
				

				return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(855, 480), mBoundChaseCamera);
			}
			
			@Override
			public void onCreateResources() {
				BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
				
				this.mBackTopTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 3000,375, TextureOptions.BILINEAR);
				this.mBackTopTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBackTopTextureAtlas, this, "backtop.png", 0, 0, 1,1);
				
				this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 65,65, TextureOptions.BILINEAR);
				this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "crate.png", 0, 0, 1,1); 
				
				this.mWallTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),72,72,TextureOptions.BILINEAR);
				this.mWallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mWallTextureAtlas, this, "wall.png", 0, 0, 1,1);
				
				this.mBackTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),1440,720,TextureOptions.BILINEAR);
				this.mBackTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBackTextureAtlas, this, "back.png", 0, 0, 1,1);
				
				this.mBackTextureAtlas.load();
				this.mBitmapTextureAtlas.load();
				this.mWallTextureAtlas.load();
				CreateMode.this.mBoundChaseCamera.setBoundsEnabled(true);
			}

			@Override
			public Scene onCreateScene() {
				this.mEngine.registerUpdateHandler(new FPSLogger());

				this.mScene = new Scene();
				this.mScene.setOnSceneTouchListener(this);
				this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
				
				//Set BackGround Here.
				this.mScene.setBackground(new Background(.05f, .05f, .05f));
				Sprite back = new Sprite(0,-280, this.mBackTextureRegion, this.getVertexBufferObjectManager());
				this.mScene.attachChild(back);
				
				
				///Level Building code]
				for (int i=0;i<20;i++){
					addWall(i*72,408);
					addWall(i*72,-250);
				}
				for(int f=0;f<10;f++){
					addWall(0,400-(f*72));
					addWall(1440,390-(f*72));
				}
				addWall(216,120);
				addWall(288,120);
				addWall(360,120);
				addWall(432,120);
				
				addFace(360,47);
				
				mBoundChaseCamera.setChaseEntity(face);
				
				
				
				this.mScene.onUpdate(0.05f);
				this.mScene.registerUpdateHandler(this.mPhysicsWorld);
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				
				mScene.registerUpdateHandler(new IUpdateHandler() {
	                
	              

					public void reset() {
						
						
					}

					@Override
					public void onUpdate(float pSecondsElapsed) {
						// TODO Auto-generated method stub
						
					}}); 
	                   

				
		return mScene;
		}
			
			 

		
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				if(this.mPhysicsWorld != null) {
					if(pSceneTouchEvent.isActionDown()) {

						
						gravityY=-25;
						final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
						this.mPhysicsWorld.setGravity(gravity);
						Vector2Pool.recycle(gravity);
					}
					
					if(pSceneTouchEvent.isActionUp()){
						gravityY=25;
						final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
						this.mPhysicsWorld.setGravity(gravity);
						Vector2Pool.recycle(gravity);
						
					}
					
				}
				
				
				return false;
			}
			
			

			public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {
				

			}

			public void onAccelerationChanged(final AccelerationData pAccelerationData) {
				final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*4, gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
			}

			@Override
			public void onResumeGame() {
				super.onResumeGame();

				this.enableAccelerationSensor(this);
			}

			@Override
			public void onPauseGame() {
				super.onPauseGame();

				this.disableAccelerationSensor();
			}

			// ===========================================================
			// Methods
			// ===========================================================

			private void addFace(final float pX, final float pY) {
				this.mFaceCount++;
				
				Debug.d("Faces: " + this.mFaceCount);
					    mTouchX = pX;
		                mTouchY = pY;
		                face = new Sprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
						Body guy = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
				this.mScene.attachChild(face);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, guy, true, true));
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				
			}
			
			private void addWall(final float pX, final float pY) {
				
				final Body body;
						wallCount++;
					    mTouchX = pX;
		                mTouchY = pY;
		                Sprite wall = new Sprite(pX, pY, this.mWallTextureRegion, this.getVertexBufferObjectManager());
						body= PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, FIXTURE_DEF);
						
				this.mScene.attachChild(wall);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body, true, true));
			}
			
			
			
			// ===========================================================
			// Inner and Anonymous Classes
			// ===========================================================
		}
