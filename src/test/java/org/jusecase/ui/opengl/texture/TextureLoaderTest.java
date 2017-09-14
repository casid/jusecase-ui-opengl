package org.jusecase.ui.opengl.texture;

import org.junit.Test;
import org.jusecase.scenegraph.texture.TextureLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jusecase.Builders.*;

public abstract class TextureLoaderTest {
    public static final String REFERENCE_IMAGE = "images/0000_poisondagger_128.jpg";

    protected TextureLoader textureLoader;

    @Test
    public void loadStream() throws IOException {
        Texture texture = (Texture)textureLoader.load(an(inputStream().withResource(REFERENCE_IMAGE)));
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test
    public void loadFilePath() throws IOException, URISyntaxException {
        Texture texture = (Texture)textureLoader.load(a(path().withResource(REFERENCE_IMAGE)));
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test
    public void loadFileString() throws IOException, URISyntaxException {
        Texture texture = (Texture)textureLoader.load(REFERENCE_IMAGE);
        thenReferenceImageWasLoadedCorrectly(texture);
    }

    @Test(expected = NoSuchFileException.class)
    public void loadFile_notFound() throws IOException, URISyntaxException {
        textureLoader.load(Paths.get("images/unknown.jpg"));
    }

    private void thenReferenceImageWasLoadedCorrectly(Texture texture) {
        assertThat(texture.getWidth()).isEqualTo(128);
        assertThat(texture.getHeight()).isEqualTo(128);
        assertThat(texture.getComponents()).isEqualTo(3);
        assertThat(texture.getData()).isNotNull();
        assertThat(texture.getData().limit()).isEqualTo(49152);
    }
}