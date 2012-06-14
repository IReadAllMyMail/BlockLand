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
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Vibrator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

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
			public float tilt =0;
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
			public int gravityX=25,gravityY=25;
			
			private World mWorld;
			
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
				this.mBoundChaseCamera = new BoundCamera(360, 47, 640, 330, 0, 1512, -248, 480);
				

				return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(855, 480), mBoundChaseCamera);
			}
			
			@Override
			public void onCreateResources() {
				BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
				
				this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 50,64, TextureOptions.BILINEAR);
				this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "bob.png", 0, 0, 1,1); 
				
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
				

				final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);   
				
				this.mScene.onUpdate(0.05f);
				this.mScene.registerUpdateHandler(this.mPhysicsWorld);
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				
				
				this.mPhysicsWorld.setContactListener(new ContactListener() {
					@Override
					public void beginContact(final Contact pContact) {
						vibrator.vibrate(24);
						System.out.println(pContact.getFixtureA().getBody().getUserData());
						
						
				   
					}
					@Override
					public void endContact(final Contact pContact) {
					}
					@Override
					public void preSolve(Contact contact,Manifold oldManifold) {

					}
					@Override
					public void postSolve(Contact contact,ContactImpulse impulse) {         
					}
					});

				
				mScene.registerUpdateHandler(new IUpdateHandler() {
	                
	              

					public void reset() {
						
						
					}

					@Override
					public void onUpdate(float pSecondsElapsed) {
						mBoundChaseCamera.setRotation(tilt);
						
					}}); 
	                   

				
		return mScene;
		}
			
			 

		
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				if(this.mPhysicsWorld != null) {
					if(pSceneTouchEvent.isActionDown()) {

						
						gravityY=-15;
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
				final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*3, gravityY);
				tilt=(pAccelerationData.getX());
				
				
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
		                Body guy = null;
		                face = new Sprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
						guy = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
						guy.setFixedRotation(false);
				this.mScene.attachChild(face);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, guy, true, true));
				
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				
			}
			
			private void addWall(final float pX, final float pY) {
		                Sprite wall = new Sprite(pX, pY, this.mWallTextureRegion, this.getVertexBufferObjectManager());
		                Body body;
						body= PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, FIXTURE_DEF);
						
						
				this.mScene.attachChild(wall);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body, true, true));
				wall.setUserData("wall");
			}
			
			 private JSONObject storeMyBodyInfo(final String myDescription, final Sprite mySprite, final Boolean myKill)
		        {
		// STORE INFORMATION ABOUT THE CURRENT BODY SO THAT WE HAVE ENOUGH INFO TO DELETE IT LATER
		                JSONObject myObject = new JSONObject();
		       
		                try {
		                        myObject.put("myDescription", myDescription);
		                        myObject.put("killMe", myKill);
		                        myObject.put("mySprite", mySprite);
		                } catch (JSONException e) {
		                        Debug.d("storeMyBodyInfo FAILED: " + e);
		                }
		                return myObject;
		        }
			
			
			
			// ===========================================================
			// Inner and Anonymous Classes
			// ===========================================================
		}
