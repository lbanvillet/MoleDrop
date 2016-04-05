package banvillet.moledrop;

import android.hardware.SensorManager;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.LevelLoader;

import java.io.IOException;

import banvillet.moledrop.camera.CroppedResolutionPolicy;
import banvillet.moledrop.level.CustomLevelLoader;

public class GameActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, ScrollDetector.IScrollDetectorListener, PinchZoomDetector.IPinchZoomDetectorListener {

    // ===========================================================
    // Constants
    // ===========================================================
    private static final int CAMERA_WIDTH = 480;
    private static final int CAMERA_HEIGHT = 320;
    private static final int CAMERA_INIT_VELOCITY = 200;
    private static final int CAMERA_MAX_VELOCITY = 5000;
    private static final float CAMERA_INIT_ZOOM_SPEED = 0.4f;
    private static final float CAMERA_MAX_ZOOM_SPEED = 10;
    private static final float MIN_ZOOM_FACTOR = 0.25f;
    private static final float MAX_ZOOM_FACTOR = 2f;

    // ===========================================================
    // Fields
    // ===========================================================
    public Scene scene;
    public PhysicsWorld physicsWorld;
    public GameFactory gameFactory;

    private SmoothCamera camera;
    private SurfaceScrollDetector scrollDetector;
    private PinchZoomDetector pinchZoomDetector;
    private float pinchZoomStartedCameraZoomFactor;

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, CAMERA_MAX_VELOCITY, CAMERA_INIT_VELOCITY, CAMERA_INIT_ZOOM_SPEED);
        return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new CroppedResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.gameFactory = new GameFactory(this);
    }

    @Override
    public Scene onCreateScene() {

        //Set the engine
        this.mEngine.setTouchController(new MultiTouchController());
        this.mEngine.registerUpdateHandler(new FPSLogger());

        //Create the scene and world
        this.scene = new Scene();
        this.scene.setBackground(new Background(0, 0, 0));
        this.scene.setOnSceneTouchListener(this);
        this.scrollDetector = new SurfaceScrollDetector(this);
        this.pinchZoomDetector = new PinchZoomDetector(this);
        this.physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

        //Level loader
        final LevelLoader levelLoader = new CustomLevelLoader(this);

        try {
            levelLoader.loadLevelFromAsset(this.getAssets(), "example.lvl");
        } catch (final IOException e) {
            Debug.e(e);
        }

        //Populate the scene with sample elements
//        final Rectangle jump = new Rectangle(CAMERA_WIDTH / 4 - 100, CAMERA_HEIGHT / 2, 100, 10, vertexBufferObjectManager);
//        final FixtureDef jumpFixtureDef = PhysicsFactory.createFixtureDef(0, 1f, 0.5f);
//        PhysicsFactory.createBoxBody(this.physicsWorld, jump, BodyDef.BodyType.StaticBody, jumpFixtureDef);
//        this.scene.attachChild(jump);

        //Attach world to the scene
        this.scene.registerUpdateHandler(this.physicsWorld);
        return this.scene;
    }

    @Override
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {

        if (this.camera.getMaxVelocityY() != CAMERA_MAX_VELOCITY || this.camera.getMaxZoomFactorChange() != CAMERA_MAX_ZOOM_SPEED) {
            this.camera.setCenter(this.camera.getCenterX(), this.camera.getCenterY());
            this.camera.setZoomFactor(this.camera.getZoomFactor());
            this.camera.setMaxVelocityY(CAMERA_MAX_VELOCITY);
            this.camera.setMaxZoomFactorChange(CAMERA_MAX_ZOOM_SPEED);
        }

        if (this.pinchZoomDetector != null) {
            this.pinchZoomDetector.onTouchEvent(pSceneTouchEvent);

            if (this.pinchZoomDetector.isZooming()) {
                this.scrollDetector.setEnabled(false);
            } else {
                if (pSceneTouchEvent.isActionDown()) {
                    this.scrollDetector.setEnabled(true);
                    this.scene.attachChild(this.gameFactory.createBoxFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 0, 0, BodyDef.BodyType.DynamicBody));
                }
                this.scrollDetector.onTouchEvent(pSceneTouchEvent);
            }
        } else {
            this.scrollDetector.onTouchEvent(pSceneTouchEvent);
        }
        return true;
    }

    @Override
    public void onAccelerationAccuracyChanged(final AccelerationData pAccelerationData) {
    }

    @Override
    public void onAccelerationChanged(final AccelerationData pAccelerationData) {
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

    @Override
    public void onScrollStarted(ScrollDetector pScrollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
    }

    @Override
    public void onScroll(ScrollDetector pScrollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        final float zoomFactor = this.camera.getZoomFactor();
        this.camera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }

    @Override
    public void onScrollFinished(ScrollDetector pScrollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
    }

    @Override
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
        this.pinchZoomStartedCameraZoomFactor = this.camera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        final float newZoomFactor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
        if(newZoomFactor < MAX_ZOOM_FACTOR && newZoomFactor > MIN_ZOOM_FACTOR){
            this.camera.setZoomFactor(newZoomFactor);
        }
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        final float newZoomFactor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
        if(newZoomFactor < MAX_ZOOM_FACTOR && newZoomFactor > MIN_ZOOM_FACTOR){
            this.camera.setZoomFactor(newZoomFactor);
        }
    }


    // ===========================================================
    // Methods
    // ===========================================================

    public void initialiseLevel(final float width, final float height) {

        //Initialise the camera with smooth moves
        this.camera.setBounds(0, -40, width, height + 40);
        this.camera.setBoundsEnabled(true);
        scene.registerUpdateHandler(new TimerHandler(1, true, getInitKinematicFirstPhase(width, height)));

        //Create borders
        this.scene.attachChild(this.gameFactory.createWall(0, height - 2, width, 2));
        this.scene.attachChild(this.gameFactory.createWall(0f, 0f, width, 2));
        this.scene.attachChild(this.gameFactory.createWall(0, 0, 2, height));
        this.scene.attachChild(this.gameFactory.createWall(width - 2, 0, 2, height));
    }

    private ITimerCallback getInitKinematicFirstPhase(final float width, final float height){
        return new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {

                //If the screen has not been touched by player
                if (GameActivity.this.camera.getMaxVelocityY() != CAMERA_MAX_VELOCITY) {
                    GameActivity.this.camera.setCenter(width / 2, height);
                    GameActivity.this.scene.registerUpdateHandler(new TimerHandler(4.5f, true, getInitKinematicSecondPhase(width, height)));
                }
            }
        };
    }

    private ITimerCallback getInitKinematicSecondPhase(final float width, final float height){
        return new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {

                //If the screen has not been touched by player
                if (GameActivity.this.camera.getMaxVelocityY() != CAMERA_MAX_VELOCITY) {
                    GameActivity.this.camera.setZoomFactor(MIN_ZOOM_FACTOR);
                }
            }
        };
    }
}