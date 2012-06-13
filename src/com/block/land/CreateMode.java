package com.block.land;

		import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
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
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

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
			Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
			public Sprite wall;
			private static final int CAMERA_WIDTH = 800;
			private static final int CAMERA_HEIGHT = 480;
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
			public int gravityX=0,gravityY;
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

				

				return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
			}
			
			@Override
			public void onCreateResources() {
				BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

				this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 65,65, TextureOptions.BILINEAR);
				this.mBoxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "crate.png", 0, 0, 1,1); // 72x72
				this.mWallTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),72,72,TextureOptions.BILINEAR);
				this.mWallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mWallTextureAtlas, this, "wall.png", 0, 0, 1,1); // 72x72
				this.mBackTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),800,480,TextureOptions.BILINEAR);
				this.mBackTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBackTextureAtlas, this, "back.png", 0, 0, 1,1); // 72x72
				this.mBackTextureAtlas.load();
				this.mBitmapTextureAtlas.load();
				this.mWallTextureAtlas.load();
			}

			@Override
			public Scene onCreateScene() {
				this.mEngine.registerUpdateHandler(new FPSLogger());

				this.mScene = new Scene();
				this.mScene.setBackground(new Background(.8f, .8f, .8f));
				this.mScene.setOnSceneTouchListener(this);

				this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

				final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
				final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
				final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
				final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
				final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

				final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1, 0.0f, 0.95f);
				PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
				PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
				PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
				PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

				this.mScene.attachChild(ground);
				this.mScene.attachChild(roof);
				this.mScene.attachChild(left);
				this.mScene.attachChild(right);
				Sprite back = new Sprite(0, 0, this.mBackTextureRegion, this.getVertexBufferObjectManager());
				this.mScene.attachChild(back);
				
				
				///Level Building code]
				addWall(0,408);
				addWall(72,408);
				addWall(144,408);
				addWall(216,408);
				addWall(288,408);
				addWall(360,408);
				addWall(432,408);
				addWall(504,408);
				addWall(576,408);
				addWall(648,408);
				addWall(720,408);
				addWall(792,408);
				addWall(0,192);
				addWall(72,192);
				addWall(144,192);
				addWall(216,192);
				addFace(72,336);
				
				
				
				this.mScene.onUpdate(0.1f);
				this.mScene.registerUpdateHandler(this.mPhysicsWorld);
				final Vector2 gravity = Vector2Pool.obtain(gravityX,gravityY);
				this.mPhysicsWorld.setGravity(gravity);
				Vector2Pool.recycle(gravity);
				mScene.registerUpdateHandler(new IUpdateHandler() {
	                public void onUpdate(float pSecondsElapsed) {
	                camera.setCenter(face.getX(), face.getY());
	                }
	              

					public void reset() {
						// TODO Auto-generated method stub
						
					}}); 

	                    //Place what you want to happen here!
	                   

				
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
				final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX()*3, gravityY);
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
				final Body body;
				Debug.d("Faces: " + this.mFaceCount);
					    mTouchX = pX;
		                mTouchY = pY;
		                face = new Sprite(pX, pY, this.mBoxFaceTextureRegion, this.getVertexBufferObjectManager());
						body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, face, BodyType.DynamicBody, FIXTURE_DEF);
				this.mScene.attachChild(face);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
			}
			
			private void addWall(final float pX, final float pY) {
				
				final Body body;
				
					    mTouchX = pX;
		                mTouchY = pY;
		                Sprite wall = new Sprite(pX, pY, this.mWallTextureRegion, this.getVertexBufferObjectManager());
						body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, wall, BodyType.StaticBody, FIXTURE_DEF);
				this.mScene.attachChild(wall);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(wall, body, true, true));
			}
			
			
			
			// ===========================================================
			// Inner and Anonymous Classes
			// ===========================================================
		}
