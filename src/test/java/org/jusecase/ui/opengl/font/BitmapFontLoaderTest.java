package org.jusecase.ui.opengl.font;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jusecase.inject.ComponentTest;
import org.jusecase.ui.font.BitmapFont;
import org.jusecase.ui.font.BitmapFontCharacter;
import org.jusecase.ui.font.BitmapFontLoader;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;

import static org.assertj.core.api.Assertions.assertThat;

class BitmapFontLoaderTest implements ComponentTest {
    BitmapFontLoader loader;

    BitmapFont font;

    @BeforeEach
    void setUp() {
        givenDependency(new StbiTextureLoader());
        loader = new BitmapFontLoader();
    }

    @Test
    void loadFromResource() {
        font = loader.load("fonts/font-comic.fnt");

        assertThat(font.getLineHeight()).isEqualTo(14);

        BitmapFontCharacter a = font.getCharacter('a');
        assertThat(a).isNotNull();
        assertThat(a.texture.getWidth()).isEqualTo(11);
        assertThat(a.texture.getHeight()).isEqualTo(13);
        assertThat(a.advanceX).isEqualTo(9);
        assertThat(a.offsetX).isEqualTo(-1);
        assertThat(a.offsetY).isEqualTo(+1);

        BitmapFontCharacter space = font.getCharacter(' ');
        assertThat(space).isNotNull();
        assertThat(space.texture.getWidth()).isEqualTo(3);
        assertThat(space.texture.getHeight()).isEqualTo(3);
        assertThat(space.advanceX).isEqualTo(5);

        assertThat(font.getKerning((char)108, (char)34)).isEqualTo(-4);
    }
}