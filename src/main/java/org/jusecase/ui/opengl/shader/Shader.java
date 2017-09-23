package org.jusecase.ui.opengl.shader;

import org.jusecase.scenegraph.math.Matrix3x2;
import org.jusecase.ui.opengl.util.MatrixUtils;
import org.jusecase.ui.opengl.shader.stage.FragmentShader;
import org.jusecase.ui.opengl.shader.stage.VertexShader;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int id;

    public static ShaderBuilder create() {
        return new ShaderBuilder();
    }

    private Shader(int id) {
        this.id = id;
    }

    public void use() {
        glUseProgram(id);
    }

    public void setUniform(String name, Matrix3x2 matrix) {
        int location = glGetUniformLocation(id, name);
        glUniformMatrix4fv(location, false, MatrixUtils.toOpenGlMatrix4f(matrix));
    }

    public void dispose() {
        glUseProgram(0);
        glDeleteProgram(id);
    }

    public static class ShaderBuilder {
        private int id;
        private VertexShader vertexShader;
        private FragmentShader fragmentShader;
        private boolean disposeVertexShaderAfterLinking;
        private boolean disposeFragmentShaderAfterLinking;
        private Map<Integer, String> attributeLocations = new HashMap<>();

        private ShaderBuilder() {
        }

        public ShaderBuilder withVertexShader(VertexShader vertexShader) {
            return this.withVertexShader(vertexShader, true);
        }

        public ShaderBuilder withFragmentShader(FragmentShader fragmentShader) {
            return this.withFragmentShader(fragmentShader, true);
        }

        public ShaderBuilder withVertexShader(VertexShader vertexShader, boolean disposeAfterLinking) {
            this.vertexShader = vertexShader;
            this.disposeVertexShaderAfterLinking = disposeAfterLinking;
            return this;
        }

        public ShaderBuilder withFragmentShader(FragmentShader fragmentShader, boolean disposeAfterLinking) {
            this.fragmentShader = fragmentShader;
            this.disposeFragmentShaderAfterLinking = disposeAfterLinking;
            return this;
        }

        public ShaderBuilder withAttributeLocation(int location, String attribute) {
            attributeLocations.put(location, attribute);
            return this;
        }

        public Shader build() {
            create();
            link();
            disposeTemporaryResources();

            return new Shader(id);
        }

        private void create() {
            id = glCreateProgram();

            if (vertexShader != null) {
                glAttachShader(id, vertexShader.getId());
            }

            if (fragmentShader != null) {
                glAttachShader(id, fragmentShader.getId());
            }

            for (Map.Entry<Integer, String> entry : attributeLocations.entrySet()) {
                glBindAttribLocation(id, entry.getKey(), entry.getValue());
            }
        }

        private void link() {
            glLinkProgram(id);

            int linked = glGetProgrami(id, GL_LINK_STATUS);
            if (linked == 0) {
                String programLog = glGetProgramInfoLog(id);
                throw new AssertionError("Could not link program: " + programLog);
            }
        }

        private void disposeTemporaryResources() {
            if (disposeVertexShaderAfterLinking) {
                vertexShader.dispose();
            }
            if (disposeFragmentShaderAfterLinking) {
                fragmentShader.dispose();
            }
        }
    }
}
