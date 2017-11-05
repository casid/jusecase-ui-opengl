package org.jusecase.ui.opengl.texture;

import org.junit.jupiter.api.Test;
import org.jusecase.scenegraph.texture.TextureLoader;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.jusecase.Builders.*;

public abstract class TextureLoaderTest {
    public static final String REFERENCE_IMAGE = "images/0000_poisondagger_128.jpg";

    protected TextureLoader textureLoader;

    @Test
    public void loadStream() {
        Texture texture = (Texture)textureLoader.load(an(inputStream().withResource(REFERENCE_IMAGE)));
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test
    public void loadFilePath() {
        Texture texture = (Texture)textureLoader.load(a(path().withResource(REFERENCE_IMAGE)));
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test
    public void loadFileString() {
        Texture texture = (Texture)textureLoader.load(REFERENCE_IMAGE);
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test
    public void loadFile_notFound() {
        Throwable throwable = catchThrowable(() -> textureLoader.load(Paths.get("images/unknown.jpg")));
        assertThat(throwable).isInstanceOf(RuntimeException.class);
    }

    private void thenReferenceImageWasLoadedCorrectly(Texture texture) {
        assertThat(texture.getWidth()).isEqualTo(128);
        assertThat(texture.getHeight()).isEqualTo(128);
        assertThat(texture.getComponents()).isEqualTo(3);
        assertThat(texture.getData()).isNotNull();
        assertThat(texture.getData().limit()).isEqualTo(49152);
    }
}