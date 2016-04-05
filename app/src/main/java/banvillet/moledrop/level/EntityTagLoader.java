package banvillet.moledrop.level;

import com.badlogic.gdx.physics.box2d.BodyDef;

import org.andengine.entity.IEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.util.SAXUtils;
import org.andengine.util.level.IEntityLoader;
import org.xml.sax.Attributes;

import banvillet.moledrop.GameFactory;

/**
 * Class to load game entity from template file.
 *
 * Created by lb185112 on 4/5/2016.
 */
public class EntityTagLoader implements IEntityLoader {

    // ===========================================================
    // Constants
    // ===========================================================
    private static final String TAG_ENTITY_ATTRIBUTE_X = "x";
    private static final String TAG_ENTITY_ATTRIBUTE_Y = "y";
    private static final String TAG_ENTITY_ATTRIBUTE_WIDTH = "width";
    private static final String TAG_ENTITY_ATTRIBUTE_HEIGHT = "height";
    private static final String TAG_ENTITY_ATTRIBUTE_TYPE = "type";

    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX = "box";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_CIRCLE = "circle";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_TRIANGLE = "triangle";
    private static final Object TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_HEXAGON = "hexagon";

    // ===========================================================
    // Fields
    // ===========================================================
    private GameFactory gameFactory;

    // ===========================================================
    // Constructor
    // ===========================================================
    public EntityTagLoader(GameFactory gameFactory){
        this.gameFactory = gameFactory;
    }

    @Override
    public IEntity onLoadEntity(final String pEntityName, final Attributes pAttributes) {
        final int x = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_X);
        final int y = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_Y);
        final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_WIDTH);
        final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_HEIGHT);
        final String type = SAXUtils.getAttributeOrThrow(pAttributes, TAG_ENTITY_ATTRIBUTE_TYPE);

        final AnimatedSprite face;
        if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_BOX)) {
            face = this.gameFactory.createBoxFace(x, y, width, height, BodyDef.BodyType.StaticBody);
        } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_CIRCLE)) {
            face = this.gameFactory.createCircleFace(x, y, width, height, BodyDef.BodyType.StaticBody);
        } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_TRIANGLE)) {
            face = this.gameFactory.createBoxFace(x, y, width, height, BodyDef.BodyType.StaticBody);
        } else if (type.equals(TAG_ENTITY_ATTRIBUTE_TYPE_VALUE_HEXAGON)) {
            face = this.gameFactory.createBoxFace(x, y, width, height, BodyDef.BodyType.StaticBody);
        } else {
            throw new IllegalArgumentException();
        }

        return face;
    }
}
