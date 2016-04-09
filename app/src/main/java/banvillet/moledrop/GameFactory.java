package banvillet.moledrop;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

import org.andengine.entity.primitive.Mesh;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.extension.physics.box2d.util.triangulation.EarClippingTriangulator;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.color.Color;

import java.util.ArrayList;
import java.util.List;

import banvillet.moledrop.item.PhysicalEntity;
import banvillet.moledrop.physics.PhysicsHelper;

/**
 * Contains the game activity: scene and physical world.
 * <p/>
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

    public void createGroundAndHole(final float width, final float height) {

        float hole_X = 200;
        final FixtureDef solidFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);

        //Set ground texture on the left
        BitmapTextureAtlas groundTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 256, 128, TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA);
        TextureRegion groundTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(groundTexture, this.gameActivity, "ground.png", 0, 0);
        groundTextureRegion.setTextureWidth(1280);
        groundTexture.load();
        Sprite groundSpriteLeft = new Sprite(-1280 + hole_X, height, groundTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, groundSpriteLeft, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(groundSpriteLeft);

        //Set ground texture on the right
        Sprite groundSpriteRight = new Sprite(hole_X + 192, height, groundTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, groundSpriteRight, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(groundSpriteRight);

        //Set ground under layer
        Rectangle underGround = new Rectangle(-1000, height + 128, 2000, 400, this.gameActivity.getVertexBufferObjectManager());
        underGround.setColor(89f / 255f, 74f / 255f, 63f / 255f);
        this.gameActivity.scene.attachChild(underGround);

        //Set grass texture on the left
        BitmapTextureAtlas grassTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 256, 5, TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA);
        TextureRegion grassTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(grassTexture, this.gameActivity, "grass.png", 0, 0);
        grassTextureRegion.setTextureWidth(1280);
        grassTexture.load();
        Sprite grassSpriteLeft = new Sprite(-1280 + hole_X, height - 5, grassTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        this.gameActivity.scene.attachChild(grassSpriteLeft);

        //Set grass texture on the right
        Sprite grassSpriteRight = new Sprite(hole_X + 192, height - 5, grassTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        this.gameActivity.scene.attachChild(grassSpriteRight);

        //Set hole left
        BitmapTextureAtlas holeLeftTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 128, TextureOptions.DEFAULT);
        TextureRegion holeLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeLeftTexture, this.gameActivity, "hole_left.png", 0, 0);
        holeLeftTexture.load();
        Sprite holeLeftSprite = new Sprite(hole_X, height, holeLeftTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, holeLeftSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(holeLeftSprite);

        //Set hole right
        BitmapTextureAtlas holeRightTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 128, TextureOptions.DEFAULT);
        TextureRegion holeRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeRightTexture, this.gameActivity, "hole_right.png", 0, 0);
        holeRightTexture.load();
        Sprite holeRightSprite = new Sprite(hole_X + 128, height, holeRightTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, holeRightSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(holeRightSprite);

        //Set hole triangle left
        BitmapTextureAtlas holeTriangleLeftTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 64, TextureOptions.DEFAULT);
        TextureRegion holeTriangleLeftTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeTriangleLeftTexture, this.gameActivity, "hole_triangle_left.png", 0, 0);
        holeTriangleLeftTexture.load();
        Sprite holeTriangleLeftSprite = new Sprite(hole_X, height - 64, holeTriangleLeftTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsHelper.createTriangleBody(this.gameActivity.physicsWorld, holeTriangleLeftSprite, BodyDef.BodyType.StaticBody, solidFixtureDef, false);
        this.gameActivity.scene.attachChild(holeTriangleLeftSprite);

        //Set hole triangle right
        BitmapTextureAtlas holeTriangleRightTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 64, TextureOptions.DEFAULT);
        TextureRegion holeTriangleRightTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeTriangleRightTexture, this.gameActivity, "hole_triangle_right.png", 0, 0);
        holeTriangleRightTexture.load();
        Sprite holeTriangleRightSprite = new Sprite(hole_X + 128, height - 64, holeTriangleRightTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsHelper.createTriangleBody(this.gameActivity.physicsWorld, holeTriangleRightSprite, BodyDef.BodyType.StaticBody, solidFixtureDef, true);
        this.gameActivity.scene.attachChild(holeTriangleRightSprite);

        //Set hole bottom
        BitmapTextureAtlas holeBottomTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 68, TextureOptions.DEFAULT);
        TextureRegion holeBottomTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeBottomTexture, this.gameActivity, "hole_bottom.png", 0, 0);
        holeBottomTexture.load();
        Sprite holeBottomSprite = new Sprite(hole_X + 64, height + 60, holeBottomTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, holeBottomSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(holeBottomSprite);

        //Set hole middle
        BitmapTextureAtlas holeMiddleTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 60, TextureOptions.DEFAULT);
        TextureRegion holeMiddleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(holeMiddleTexture, this.gameActivity, "hole_middle.png", 0, 0);
        holeMiddleTexture.load();
        Sprite holeMiddleSprite = new Sprite(hole_X + 64, height, holeMiddleTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        this.gameActivity.scene.attachChild(holeMiddleSprite);

        //Set left tree trunk
        BitmapTextureAtlas treeTrunkTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 64, 128, TextureOptions.REPEATING_NEAREST_PREMULTIPLYALPHA);
        TextureRegion treeTrunkTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(treeTrunkTexture, this.gameActivity, "tree_trunk.png", 0, 0);
        treeTrunkTextureRegion.setTextureHeight(height);
        treeTrunkTexture.load();
        Sprite leftTreeTrunkSprite = new Sprite(-64, 0, treeTrunkTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, leftTreeTrunkSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(leftTreeTrunkSprite);

        //Set right tree trunk
        Sprite rightTreeTrunkSprite = new Sprite(width, 0, treeTrunkTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsFactory.createBoxBody(this.gameActivity.physicsWorld, rightTreeTrunkSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(rightTreeTrunkSprite);

        //Set left tree top
        BitmapTextureAtlas treeTopTexture = new BitmapTextureAtlas(this.gameActivity.getTextureManager(), 240, 186, TextureOptions.DEFAULT);
        TextureRegion treeTopTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(treeTopTexture, this.gameActivity, "tree_top.png", 0, 0);
        treeTopTexture.load();
        Sprite leftTreeTopSprite = new Sprite(-152, -93, treeTopTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsHelper.createTreeTopBody(this.gameActivity.physicsWorld, leftTreeTopSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(leftTreeTopSprite);

        //Set right tree top
        Sprite rightTreeTopSprite = new Sprite(width - 88, -93, treeTopTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        PhysicsHelper.createTreeTopBody(this.gameActivity.physicsWorld, rightTreeTopSprite, BodyDef.BodyType.StaticBody, solidFixtureDef);
        this.gameActivity.scene.attachChild(rightTreeTopSprite);
    }

    public PhysicalEntity createMesh(final float x, final float y, ArrayList<Vector2> vertices) {
        final FixtureDef meshFixtureDef = PhysicsFactory.createFixtureDef(0.5f, 0.5f, 0.5f);

        List<Vector2> uniqueBodyVerticesTriangulated = new EarClippingTriangulator().computeTriangles(vertices);
        float[] meshTriangles = new float[uniqueBodyVerticesTriangulated.size() * 3];
        for (int i = 0; i < uniqueBodyVerticesTriangulated.size(); i++) {
            meshTriangles[i * 3] = uniqueBodyVerticesTriangulated.get(i).x;
            meshTriangles[i * 3 + 1] = uniqueBodyVerticesTriangulated.get(i).y;
            uniqueBodyVerticesTriangulated.get(i).mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
        }
        Mesh mesh = new Mesh(x, y, meshTriangles, uniqueBodyVerticesTriangulated.size(), Mesh.DrawMode.TRIANGLES, this.gameActivity.getVertexBufferObjectManager());
        mesh.setColor(Color.WHITE);
        Body body = PhysicsFactory.createTrianglulatedBody(this.gameActivity.physicsWorld, mesh, uniqueBodyVerticesTriangulated, BodyDef.BodyType.StaticBody, meshFixtureDef);
        this.gameActivity.scene.attachChild(mesh);
        return new PhysicalEntity(mesh, body);
    }

    public PhysicalEntity createMole(final float x, final float y) {
        final FixtureDef objectFixtureDef = PhysicsFactory.createFixtureDef(0.2f, 0.2f, 0.5f);
        AnimatedSprite face;
        face = new AnimatedSprite(x, y, this.circleFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        Body body = PhysicsFactory.createCircleBody(this.gameActivity.physicsWorld, face, BodyDef.BodyType.StaticBody, objectFixtureDef);
        face.animate(200);
        this.gameActivity.physicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));
        this.gameActivity.scene.attachChild(face);
        return new PhysicalEntity(face, body);
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
        AnimatedSprite face;
        if (width == 0 || height == 0) {
            face = new AnimatedSprite(x, y, this.circleFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        } else {
            face = new AnimatedSprite(x, y, width, height, this.circleFaceTextureRegion, this.gameActivity.getVertexBufferObjectManager());
        }
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
