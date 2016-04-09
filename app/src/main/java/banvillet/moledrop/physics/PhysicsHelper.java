package banvillet.moledrop.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import org.andengine.entity.shape.Shape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;

import static org.andengine.extension.physics.box2d.util.constants.PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;

/**
 * Created by lb185112 on 4/7/2016.
 */
public class PhysicsHelper {


    /**
     * Creates a {@link Body} based on a {@link PolygonShape} in the form of a triangle:
     * <pre>
     *    /|
     *   / |
     * /___|
     * </pre>
     */
    public static Body createTriangleBody(final PhysicsWorld pPhysicsWorld, final Sprite pShape, final BodyDef.BodyType pBodyType, final FixtureDef pFixtureDef, boolean isLeftLonger) {

		/* Remember that the vertices are relative to the center-coordinates of the Shape. */
        final float halfWidth = pShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
        final float halfHeight = pShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

        final float top = -halfHeight;
        final float bottom = halfHeight;
        final float left = -halfHeight;
        final float right = halfWidth;

        final Vector2[] vertices = {
                new Vector2(isLeftLonger ? left : right, top),
                new Vector2(right, bottom),
                new Vector2(left, bottom)
        };

        return PhysicsFactory.createPolygonBody(pPhysicsWorld, pShape, vertices, pBodyType, pFixtureDef);
    }

    public static Body createTreeTopBody(final PhysicsWorld pPhysicsWorld, final Sprite pShape, final BodyDef.BodyType pBodyType, final FixtureDef pFixtureDef) {

		/* Remember that the vertices are relative to the center-coordinates of the Shape. */
        final float halfWidth = pShape.getWidthScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;
        final float halfHeight = pShape.getHeightScaled() * 0.5f / PIXEL_TO_METER_RATIO_DEFAULT;

        final Vector2[] vertices = {
                new Vector2(0, -4 * halfHeight / 5),
                new Vector2(4 * halfWidth /5, -2 * halfHeight / 5),
                new Vector2(4 * halfWidth /5, 3 * halfHeight / 5),
                new Vector2(0, 4 * halfHeight / 5),
                new Vector2(-4 * halfWidth /5, 3 * halfHeight / 5),
                new Vector2(-4 * halfWidth /5, -2 * halfHeight / 5)
        };

        return PhysicsFactory.createPolygonBody(pPhysicsWorld, pShape, vertices, pBodyType, pFixtureDef);
    }
}
