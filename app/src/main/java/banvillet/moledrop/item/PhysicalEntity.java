package banvillet.moledrop.item;

import com.badlogic.gdx.physics.box2d.Body;

import org.andengine.entity.Entity;

/**
 * Created by lb185112 on 4/9/2016.
 */
public class PhysicalEntity {

    private Entity entity;
    private Body body;

    public PhysicalEntity(Entity entity, Body body){
        this.entity = entity;
        this.body = body;
    }

    public Entity getEntity(){
        return this.entity;
    }

    public Body getBody(){
        return this.body;
    }
}
