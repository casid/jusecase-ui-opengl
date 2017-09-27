package org.jusecase.ui.opengl.texture.stbi;

import org.junit.jupiter.api.BeforeEach;
import org.jusecase.ui.opengl.texture.TextureLoaderTest;

public class StbiTextureLoaderTest extends TextureLoaderTest {
    @BeforeEach
    public void setUp() {
        textureLoader = new StbiTextureLoader();
    }
}