package banvillet.moledrop;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Contains the game activity: scene and physical world.
 * 
 * Created by lb185112 on 4/4/2016.
 */
public class GameFactory {

    // ===========================================================
    // Fields
    // ===========================================================
    private GameActivity gameActivity;
    private BitmapTextureAtlas bitmapTextureAtlas;

    private ITiledTextureRegion boxFaceTextureRegion;
    private ITiledTextureRegion circleFaceTextureRegion;
    private TiledTextureRegion triangleFaceTextureRegion;
    private TiledTextureRegion hexagonFaceTextureRegion;

    public GameFactory(GameActivity gameActivity) {
        this.gameActivity = gameActivity;

        this.bitmapTextureAtlas = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 128, TextureOptions.BILINEAR);

        this.boxFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.bitmapTextureAtlas, this.gameActivity, "face_box_tiled.png", 0, 0, 2, 1); // 64x32
        this.circleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.bitmapTextureAtlas, this.gameActivity, "face_circle_tiled.png", 0, 32, 2, 1); // 64x32
        this.triangleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.bitmapTextureAtlas, this.gameActivity, "face_triangle_tiled.png", 0, 64, 2, 1); // 64x32
        this.hexagonFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.bitmapTextureAtlas, this.gameActivity, "face_hexagon_tiled.png", 0, 96, 2, 1); // 64x32

        this.bitmapTextureAtlas.load();
    }

    // ===========================================================
    // Methods
    // ===========================================================

    public Rectangle createWall(final float x, final float y, final float width, final float height) {
        final Rectangle rectangle = new Rectangle(x, y, width, height, this.gameActivity.getVertexBufferObjectManager());
        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, rectangle, BodyDef.BodyType.StaticBody, wallFixtureDef);
        return rectangle;
    }

    public AnimatedSprite createBoxFace(final float x, final float y, final float width, final float height, BodyDef.BodyType type) {
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);
        AnimatedSprite face;
        if (width == 0 || height == 0) {
            face = new AnimatedSprite(x, y, this.boxFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        } else {
            face = new AnimatedSprite(x, y, width, height, this.boxFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        }
        Body body = PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, face, type, objectFixtureDef);
        face.animate(200);
        this.gameActivity.physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
        return face;
    }

    public AnimatedSprite createCircleFace(final float x, final float y, final float width, final float height, BodyDef.BodyType type) {
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);
        AnimatedSprite face = new AnimatedSprite(x, y, width, height, this.circleFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        Body body = PhysicsFactory.createCircleBody(this.gameActivity.physicsWorld, face, type, objectFixtureDef);
        face.animate(200);
        this.gameActivity.physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
        return face;
    }

    /**
     * E.g:
     * GameFactory.createRope(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, this.scene, this.physicsWorld, this.getVertexBufferObjectManager());
     */
    public static void createRope(int centerX, int centerY, Scene scene, PhysicsWorld physicsWorld, VertexBufferObjectManager vertexBufferObjectManager) {

        final int ropeFragmentLength = 10;
        final int fragmentNumber = 15;

        //Ceiling
        final Rectangle ceiling = new Rectangle(centerX - 1, centerY - 1, 2, 2, vertexBufferObjectManager);
        final FixtureDef ceilingFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        Body ceilingBody = PhysicsFactory.createBoxBody(physicsWorld, ceiling, BodyDef.BodyType.StaticBody, ceilingFixtureDef);
        scene.attachChild(ceiling);

        //Rope
        final FixtureDef ropeFixtureDef = PhysicsFactory.createFixtureDef(0.5f, 4f, 1f);
        Body previousElementBody = ceilingBody;
        for (int i = 0; i < fragmentNumber; i++) {
            Rectangle ropeFragment = new Rectangle(centerX + ropeFragmentLength * i, centerY - 0.5f, ropeFragmentLength, 1f, vertexBufferObjectManager);
            Body ropeFragmentBody = PhysicsFactory.createBoxBody(physicsWorld, ropeFragment, BodyDef.BodyType.DynamicBody, ropeFixtureDef);
            scene.attachChild(ropeFragment);
            physicsWorld.registerPhysicsConnector(new PhysicsConnector(ropeFragment, ropeFragmentBody, true, true));

            //Create joint
            final RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
            revoluteJointDef.initialize(previousElementBody, ropeFragmentBody, previousElementBody.getWorldCenter());
            physicsWorld.createJoint(revoluteJointDef);

            previousElementBody = ropeFragmentBody;
        }

        //Other ceiling
        final Rectangle end = new Rectangle(centerX - 1 + fragmentNumber * ropeFragmentLength, centerY - 1, 2, 2, vertexBufferObjectManager);
        final FixtureDef endFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
        Body endBody = PhysicsFactory.createBoxBody(physicsWorld, end, BodyDef.BodyType.StaticBody, endFixtureDef);
        scene.attachChild(end);
        final RevoluteJointDef endJoint = new RevoluteJointDef();
        endJoint.initialize(previousElementBody, endBody, previousElementBody.getWorldCenter());
        physicsWorld.createJoint(endJoint);
    }
}
