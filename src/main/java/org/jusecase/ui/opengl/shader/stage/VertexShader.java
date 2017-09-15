package org.jusecase.ui.opengl.shader.stage;

import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class VertexShader extends BaseShader {

    public VertexShader(String source) {
        super(source);
    }

    @Override
    public int getType() {
        return GL_VERTEX_SHADER;
    }
}
