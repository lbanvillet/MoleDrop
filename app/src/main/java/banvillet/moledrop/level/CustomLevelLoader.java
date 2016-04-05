package banvillet.moledrop.level;

import org.andengine.entity.IEntity;
import org.andengine.util.SAXUtils;
import org.andengine.util.level.IEntityLoader;
import org.andengine.util.level.LevelLoader;
import org.andengine.util.level.constants.LevelConstants;
import org.xml.sax.Attributes;

import banvillet.moledrop.GameActivity;

/**
 * Level loader.
 *
 * Created by lb185112 on 4/5/2016.
 */
public class CustomLevelLoader extends LevelLoader {

    // ===========================================================
    // Constants
    // ===========================================================
    private static final String TAG_ENTITY = "entity";

    // ===========================================================
    // Fields
    // ===========================================================
    private GameActivity gameActivity;

    // ===========================================================
    // Constructor
    // ===========================================================
    public CustomLevelLoader(GameActivity gameActivity){
        this.gameActivity = gameActivity;

        setAssetBasePath("level/");

        registerEntityLoader(LevelConstants.TAG_LEVEL, new IEntityLoader() {
            @Override
            public IEntity onLoadEntity(String pEntityName, Attributes pAttributes) {
                final int width = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_WIDTH);
                final int height = SAXUtils.getIntAttributeOrThrow(pAttributes, LevelConstants.TAG_LEVEL_ATTRIBUTE_HEIGHT);
                CustomLevelLoader.this.gameActivity.initialiseLevel(width, height);
                return CustomLevelLoader.this.gameActivity.scene;
            }
        });

        registerEntityLoader(TAG_ENTITY, new EntityTagLoader(this.gameActivity.gameFactory));
    }
}
