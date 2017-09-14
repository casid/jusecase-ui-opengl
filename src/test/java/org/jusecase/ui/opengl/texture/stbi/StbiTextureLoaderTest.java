package org.jusecase.ui.opengl.texture.stbi;

import org.junit.Before;
import org.jusecase.ui.opengl.texture.TextureLoaderTest;

public class StbiTextureLoaderTest extends TextureLoaderTest {
    @Before
    public void setUp() {
        textureLoader = new StbiTextureLoader();
    }
}