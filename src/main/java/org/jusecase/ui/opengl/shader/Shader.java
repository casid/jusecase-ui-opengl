package org.jusecase.ui.opengl.shader;

import org.jusecase.scenegraph.math.Matrix3x2;
import org.jusecase.ui.opengl.Matrix;
import org.jusecase.ui.opengl.shader.stage.FragmentShader;
import org.jusecase.ui.opengl.shader.stage.VertexShader;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int id;
    private final VertexShader vertexShader;
    private final FragmentShader fragmentShader;

    public static ShaderBuilder create() {
        return new ShaderBuilder();
    }

    private Shader(int id, VertexShader vertexShader, FragmentShader fragmentShader) {
        this.id = id;
        this.vertexShader = vertexShader;
        this.fragmentShader = fragmentShader;
    }

    public int getId() {
        return id;
    }

    public VertexShader getVertexShader() {
        return vertexShader;
    }

    public FragmentShader getFragmentShader() {
        return fragmentShader;
    }

    public void use() {
        glUseProgram(id);
    }

    public void setUniform(String name, Matrix3x2 matrix) {
        int location = glGetUniformLocation(id, name);
        glUniformMatrix4fv(location, false, Matrix.toOpenGlMatrix4f(matrix));
    }

    public static class ShaderBuilder {
        private int id;
        private VertexShader vertexShader;
        private FragmentShader fragmentShader;
        private Map<Integer, String> attributeLocations = new HashMap<>();

        private ShaderBuilder() {
        }

        public ShaderBuilder withVertexShader(VertexShader vertexShader) {
            this.vertexShader = vertexShader;
            return this;
        }

        public ShaderBuilder withFragmentShader(FragmentShader fragmentShader) {
            this.fragmentShader = fragmentShader;
            return this;
        }

        public ShaderBuilder withAttributeLocation(int location, String attribute) {
            attributeLocations.put(location, attribute);
            return this;
        }

        public Shader build() {
            create();
            link();

            return new Shader(id, vertexShader, fragmentShader);
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
            String programLog = glGetProgramInfoLog(id);
            if (programLog.trim().length() > 0) {
                System.err.println(programLog);
            }

            if (linked == 0) {
                throw new AssertionError("Could not link program");
            }
        }
    }
}
