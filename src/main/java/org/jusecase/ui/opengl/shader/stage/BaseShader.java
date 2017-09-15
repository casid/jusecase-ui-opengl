package org.jusecase.ui.opengl.shader.stage;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public abstract class BaseShader {
    private final int id;

    public BaseShader(String source) {
        id = glCreateShader(getType());
        compile(source);
    }

    public abstract int getType();

    public int getId() {
        return id;
    }

    public void dispose() {
        glDeleteShader(id);
    }

    private void compile(String source) {
        glShaderSource(id, source);

        glCompileShader(id);

        int compiled = glGetShaderi(id, GL_COMPILE_STATUS);
        if (compiled == GL_FALSE) {
            String shaderLog = glGetShaderInfoLog(id);

            dispose();

            throw new AssertionError("Could not compile shader: " + shaderLog);
        }
    }
}
