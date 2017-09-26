package org.jusecase.ui.opengl.texture.atlas;


import org.junit.Before;
import org.junit.Test;
import org.jusecase.inject.ComponentTest;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class StarlingTextureAtlasLoaderTest extends ComponentTest<StarlingTextureAtlasLoader> {

    public static final String XML = "images/atlas.xml";

    @Before
    public void setUp() {
        givenDependency(new StbiTextureLoader());
    }

    @Test
    public void load() {
        TextureAtlas atlas = getComponent().load(XML);

        Texture texture = atlas.get("player_level_up");
        assertThat(texture.getWidth()).isEqualTo(280);
        assertThat(texture.getHeight()).isEqualTo(286);
        assertThat(texture.getTexCoords().left).isEqualTo(0);
        assertThat(texture.getTexCoords().right).isEqualTo(0.546875f);
        assertThat(texture.getTexCoords().bottom).isEqualTo(0.638671875f);
        assertThat(texture.getTexCoords().top).isEqualTo(0.080078125f);
    }
}