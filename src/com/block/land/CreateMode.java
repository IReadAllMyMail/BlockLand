package com.block.land;

		import org.andengine.engine.camera.BoundCamera;
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

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Vibrator;

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
			public Boolean hitWall =false;
			private BoundCamera mBoundChaseCamera;
			public float tilt =0;
			public int gravityShiftCount=3;
			public Sprite wall;
			float mTouchX;
			float mTouchY;
			private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.0f, 10f);

			// ===========================================================
			// Fields
			// =========================================================== 
			private BitmapTextureAtlas mBitmapTextureAtlas;
			private TiledTextureRegion mBoxFaceTextureRegion;
			private BitmapTextureAtlas mWallTextureAtlas;
			private TiledTextureRegion mWallTextureRegion;
			
			private Scene mScene;
			private PhysicsWorld mPhysicsWorld;
			
			public int gravityX=0,gravityY=25;
			// ===========================================================
			// Constructors
			// ===========================================================
			private BitmapTextureAtlas mBackTextureAtlas;
			private TiledTextureRegion mBackTextureRegion;

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
//Bitmap definition and loading.
			@Override
			public void onCreateResources() {
				BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
				
				this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 65,65, TextureOptions.BILINEAR);
				this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "star.png", 0, 0, 1,1); 
				
				this.mWallTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),72,72,TextureOptions.BILINEAR);
				this.mWallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mWallTextureAtlas, this, "wall.png", 0, 0, 1,1);
				
				this.mBackTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),1440,720,TextureOptions.BILINEAR);
				this.mBackTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBackTextureAtlas, this, "back2.png", 0, 0, 1,1);
				
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
				
				
///Level Building code
				for (int i=0;i<20;i++){
					addWall(i*72,408);
					addWall(i*72,-250);
				}
				for(int f=0;f<10;f++){
					addWall(0,400-(f*72));
					addWall(1440,390-(f*72));
				}
				addWall(504,120);
				addWall(576,120);
				addWall(648,120);
				addWall(720,120);
				
				addFace(360,47);
				mBoundChaseCamera.setChaseEntity(face);
				

				final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);   
				
				this.mScene.onUpdate(0.05f);
				this.mScene.registerUpdateHandler(this.mPhysicsWorld);
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				
//Collision detection				
				this.mPhysicsWorld.setContactListener(new ContactListener() {
					@Override
					public void beginContact(final Contact pContact) {
						vibrator.vibrate(50);
						hitWall=true;
						togglePause(true);
						
					}
					@Override
					public void endContact(final Contact pContact) {
						hitWall=false;
						
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
//OnUpdate method
					@Override
					public void onUpdate(float pSecondsElapsed) {
						float newRotate = face.getRotation()+15;
						if (hitWall == false){
						mBoundChaseCamera.setRotation(tilt);
						face.setRotation(newRotate);
						}
						else {
							this.reset();
						}
					}}); 
	                   

				
		return mScene;
		}
			
			 

//OnScene Touch
			public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
				if(this.mPhysicsWorld != null) {
					if(pSceneTouchEvent.isActionDown()) {
						
						gravityY=-15;
						final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
						this.mPhysicsWorld.setGravity(gravity);
						Vector2Pool.recycle(gravity);
						togglePause(false);
					
						
					}
					if(pSceneTouchEvent.isActionUp()){
						gravityY=15;
						final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
						this.mPhysicsWorld.setGravity(gravity);
						Vector2Pool.recycle(gravity);
						
					}
					
					
				}
				
				
				return false;
			}
			
			

			public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {
				

			}
//Tilt controller
			public void onAccelerationChanged(final AccelerationData pAccelerationData) {
				tilt=(pAccelerationData.getX());
				
					final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*5f,gravityY);
					//face.setRotation(pAccelerationData.getX()*5);
					this.mPhysicsWorld.setGravity(gravity);
					Vector2Pool.recycle(gravity);
					tilt=(pAccelerationData.getX());
					if(hitWall==true){
						tilt=(0);
						final Vector2 gravity2 = Vector2Pool.obtain(0,0);
						this.mPhysicsWorld.setGravity(gravity);
						Vector2Pool.recycle(gravity);
					}
				
				
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
		        face = new Sprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
				Body guy = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
				face.setUserData(guy);
				this.mScene.attachChild(face);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, guy, true, false));
			}
			
			
			
			private void addWall(final float pX, final float pY) {
		        Sprite wall = new Sprite(pX, pY, this.mWallTextureRegion, this.getVertexBufferObjectManager());
				Body body= PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, FIXTURE_DEF);
						
						
				this.mScene.attachChild(wall);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body, false, false));
				wall.setUserData("wall");
			}
			
			void togglePause(Boolean ok){
				if (ok==true){
					this.mEngine.stop();
				}
				else{
				this.mEngine.start();	
				}
				}
			
			
			
			
			 
			
			
			
			
			// ===========================================================
			// Inner and Anonymous Classes
			// ===========================================================
		}
