package org.jusecase.ui.opengl.texture.atlas;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.inject.ComponentTest;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class StarlingTextureAtlasLoaderTest implements ComponentTest {

    public static final String XML = "images/atlas.xml";

    private StarlingTextureAtlasLoader loader;

    @BeforeEach
    public void setUp() {
        givenDependency(new StbiTextureLoader());
        loader = new StarlingTextureAtlasLoader();
    }

    @Test
    public void load() {
        TextureAtlas atlas = loader.load(XML);

        Texture texture = atlas.get("player_level_up");
        assertThat(texture.getWidth()).isEqualTo(280);
        assertThat(texture.getHeight()).isEqualTo(286);
        assertThat(texture.getTexCoords().left).isEqualTo(0);
        assertThat(texture.getTexCoords().right).isEqualTo(0.546875f);
        assertThat(texture.getTexCoords().bottom).isEqualTo(0.638671875f);
        assertThat(texture.getTexCoords().top).isEqualTo(0.080078125f);
    }

    @Test
    void frame() {
        TextureAtlas atlas = loader.load(XML);

        Texture texture = atlas.get("tower-build-button-iu");

        assertThat(texture.getWidth()).isEqualTo(77);
        assertThat(texture.getHeight()).isEqualTo(77);
        assertThat(texture.getTexCoords().left).isEqualTo(0.80859375f);
        assertThat(texture.getTexCoords().right).isEqualTo(0.9453125f);
        assertThat(texture.getTexCoords().bottom).isEqualTo(0.7246094f);
        assertThat(texture.getTexCoords().top).isEqualTo(0.5859375f);
        assertThat(texture.getFrame().left).isEqualTo(4);
        assertThat(texture.getFrame().top).isEqualTo(4);
        assertThat(texture.getFrame().right).isEqualTo(3);
        assertThat(texture.getFrame().bottom).isEqualTo(2);
    }
}