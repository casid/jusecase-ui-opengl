package org.jusecase.ui.opengl.shader.stage;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;

public class FragmentShader extends BaseShader {

    public FragmentShader(String source) {
        super(source);
    }

    @Override
    public int getType() {
        return GL_FRAGMENT_SHADER;
    }
}
