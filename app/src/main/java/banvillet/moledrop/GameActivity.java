package banvillet.moledrop;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;

import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouchController;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.list.ListUtils;
import org.andengine.util.debug.Debug;
import org.andengine.util.level.LevelLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import banvillet.moledrop.camera.CroppedResolutionPolicy;
import banvillet.moledrop.item.PhysicalEntity;
import banvillet.moledrop.level.CustomLevelLoader;

public class GameActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener, ScrollDetector.IScrollDetectorListener, PinchZoomDetector.IPinchZoomDetectorListener {

    // ===========================================================
    // Constants
    // ===========================================================
    private static final int CAMERA_WIDTH = 320;
    private static final int CAMERA_HEIGHT = 480;
    private static final int CAMERA_INIT_VELOCITY = 200;
    private static final int CAMERA_MAX_VELOCITY = 5000;
    private static final float CAMERA_INIT_ZOOM_SPEED = 0.4f;
    private static final float CAMERA_MAX_ZOOM_SPEED = 10;

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
    private float minZoomFactor = 1f;
    private float maxZoomFactor = 1f;

    private BitmapTextureAtlas autoParallaxBackgroundTexture;
    private TextureRegion parallaxBackground;
    private TextureRegion parallaxBackgroundMid;

    private boolean isEditMode = false;
    private Vector2 initPosWall;
    private PhysicalEntity wall;

    private PhysicalEntity mole;

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    @Override
    public EngineOptions onCreateEngineOptions() {
        this.camera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, CAMERA_MAX_VELOCITY, CAMERA_MAX_VELOCITY, CAMERA_MAX_ZOOM_SPEED);
        return new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new CroppedResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera);
    }

    @Override
    public void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        this.gameFactory = new GameFactory(this);

        this.autoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 2048, TextureOptions.BILINEAR);
        this.parallaxBackground = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.autoParallaxBackgroundTexture, this, "background.png", 0, 0);
        this.parallaxBackgroundMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.autoParallaxBackgroundTexture, this, "background_mid.png", 0, 640);
        this.autoParallaxBackgroundTexture.load();
    }

    @Override
    public Scene onCreateScene() {

        //Set the engine
        this.mEngine.setTouchController(new MultiTouchController());
        this.mEngine.registerUpdateHandler(new FPSLogger());

        //Create the scene and world
        this.scene = new Scene();
        this.scene.setOnSceneTouchListener(this);
        this.scrollDetector = new SurfaceScrollDetector(this);
        this.pinchZoomDetector = new PinchZoomDetector(this);
        this.physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);

        //Set background
        final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.parallaxBackground.getHeight(), this.parallaxBackground, this.getVertexBufferObjectManager())));
        autoParallaxBackground.attachParallaxEntity(new ParallaxBackground.ParallaxEntity(-2.0f, new Sprite(20, 0, this.parallaxBackgroundMid, this.getVertexBufferObjectManager())));
        scene.setBackground(autoParallaxBackground);

        //Level loader
        final LevelLoader levelLoader = new CustomLevelLoader(this);

        try {
            levelLoader.loadLevelFromAsset(this.getAssets(), "example.lvl");
        } catch (final IOException e) {
            Debug.e(e);
        }

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
                if (isEditMode) {
                    if (pSceneTouchEvent.isActionDown()) {
                        this.initPosWall = new Vector2(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
                    } else if (pSceneTouchEvent.isActionMove()) {

                        if (this.wall != null) {
                            this.physicsWorld.destroyBody(this.wall.getBody());
                            this.scene.detachChild(this.wall.getEntity());
                        }

                        //Create a wall for tests
                        ArrayList<Vector2> vertices = new ArrayList<>();
                        vertices.addAll((List<Vector2>) ListUtils.toList(new Vector2[]{
                                new Vector2(0, -5),
                                new Vector2(pSceneTouchEvent.getX() - this.initPosWall.x, pSceneTouchEvent.getY() - this.initPosWall.y - 5),
                                new Vector2(pSceneTouchEvent.getX() - this.initPosWall.x, pSceneTouchEvent.getY() - this.initPosWall.y + 5),
                                new Vector2(0, 5)

                        }));
                        this.wall = this.gameFactory.createMesh(this.initPosWall.x, this.initPosWall.y, vertices);
                    } else if (pSceneTouchEvent.isActionUp()) {
                        isEditMode = false;
                    }
                } else {
                    if (pSceneTouchEvent.isActionDown()) {
                        this.scrollDetector.setEnabled(true);
                        this.scene.attachChild(this.gameFactory.createCircleFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), 0, 0, BodyDef.BodyType.DynamicBody));
                    }
                    this.scrollDetector.onTouchEvent(pSceneTouchEvent);
                }
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
        if (newZoomFactor < this.maxZoomFactor && newZoomFactor > this.minZoomFactor) {
            this.camera.setZoomFactor(newZoomFactor);
        }
    }

    @Override
    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
        final float newZoomFactor = this.pinchZoomStartedCameraZoomFactor * pZoomFactor;
        if (newZoomFactor < this.maxZoomFactor && newZoomFactor > this.minZoomFactor) {
            this.camera.setZoomFactor(newZoomFactor);
        }
    }


    // ===========================================================
    // Methods
    // ===========================================================

    public void initialiseLevel(final float width, final float height) {

        this.gameFactory.createGroundAndHole(width, height);

        createControllers(3);

        initialiseCamera(width, height);

        //Create mole
        this.mole = this.gameFactory.createMole(100, 0);
    }

    private void createControllers(int buttonNumber) {
        HUD hud = new HUD();

        float buttonBarX = this.camera.getWidth() / 10;
        float buttonBarY = this.camera.getHeight() * 0.98f;
        float buttonSize = this.camera.getWidth() / 10;
        float verticalSpace = this.camera.getWidth() / 40;

        //Drop button
        BitmapTextureAtlas buttonDropTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 64, TextureOptions.DEFAULT);
        ITiledTextureRegion buttonDropTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(buttonDropTexture, this, "button_drop.png", 0, 0, 2, 1);
        final ButtonSprite buttonDropSprite = new ButtonSprite(buttonBarX, buttonBarY - buttonSize, buttonDropTextureRegion, this.getVertexBufferObjectManager(), new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                mole.getBody().setType(BodyDef.BodyType.DynamicBody);
            }
        });
        buttonDropSprite.setWidth((int) buttonSize * 2);
        buttonDropSprite.setHeight((int) buttonSize);
        hud.registerTouchArea(buttonDropSprite);
        hud.attachChild(buttonDropSprite);
        buttonDropTexture.load();

        //Wall buttons
        BitmapTextureAtlas buttonTexture = new BitmapTextureAtlas(this.getTextureManager(), 192, 64, TextureOptions.DEFAULT);
        ITiledTextureRegion buttonTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(buttonTexture, this, "button.png", 0, 0, 3, 1);
        for (int i = 1; i <= buttonNumber; i++) {
            final ButtonSprite buttonSprite = new ButtonSprite(buttonBarX, buttonBarY - i * (buttonSize + verticalSpace) - buttonSize, buttonTextureRegion, this.getVertexBufferObjectManager(), new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    isEditMode = !isEditMode;
                }
            });
            buttonSprite.setWidth((int) buttonSize);
            buttonSprite.setHeight((int) buttonSize);
            hud.registerTouchArea(buttonSprite);
            hud.attachChild(buttonSprite);
        }
        buttonTexture.load();

        camera.setHUD(hud);
    }

    private void initialiseCamera(float width, float height) {

        //Initialise the camera with smooth moves
        this.maxZoomFactor = CAMERA_WIDTH / width * 0.8f;
        this.minZoomFactor = CAMERA_WIDTH / width * 0.5f;
        this.camera.setZoomFactor(this.maxZoomFactor);
        this.camera.setMaxZoomFactorChange(CAMERA_INIT_ZOOM_SPEED);
        this.camera.setCenter(width / 2, 0);
        this.camera.setMaxVelocityY(CAMERA_INIT_VELOCITY);
        this.camera.setBounds(-width / 10, -140, width * 1.1f, height + 140);
        this.camera.setBoundsEnabled(true);
        scene.registerUpdateHandler(new TimerHandler(1, true, getInitKinematicFirstPhase(width, height)));
    }

    private ITimerCallback getInitKinematicFirstPhase(final float width, final float height) {
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

    private ITimerCallback getInitKinematicSecondPhase(final float width, final float height) {
        return new ITimerCallback() {
            @Override
            public void onTimePassed(final TimerHandler pTimerHandler) {

                //If the screen has not been touched by player
                if (GameActivity.this.camera.getMaxVelocityY() != CAMERA_MAX_VELOCITY) {
                    GameActivity.this.camera.setZoomFactor(minZoomFactor);
                }
            }
        };
    }
}