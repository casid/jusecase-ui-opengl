package org.jusecase.ui.opengl.render;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.Node;
import org.jusecase.scenegraph.Quad;
import org.jusecase.scenegraph.math.Matrix3x2;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.ui.opengl.shader.Shader;
import org.jusecase.ui.opengl.shader.stage.FragmentShader;
import org.jusecase.ui.opengl.shader.stage.VertexShader;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class VboRenderer implements Renderer {
    private int currentTextureId;

    private List<QuadBatch> batches = new ArrayList<>();
    private int currentBatchIndex = -1;

    private Shader quadShader;
    private Shader imageShader;

    private Matrix3x2 projection;

    @Override
    public void begin() {
        /*glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, 1, -1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();*/

        projection = Matrix3x2.orthoProjection(800, 600);

        currentTextureId = -1;

        if (quadShader == null) {
            quadShader = Shader.create()
                    .withVertexShader(new VertexShader("#version 330\n" +
                            "layout(location = 0) in vec2 vertex;\n" +
                            "uniform mat4 projection;\n" +
                            "void main() {\n" +
                            "  gl_Position = projection * vec4(vertex.xy, 0, 1);\n" +
                            "}"))
                    .withFragmentShader(new FragmentShader("#version 330\n" +
                            "out vec4 fragColor;\n" +
                            "void main(){\n" +
                            "  fragColor = vec4(1,1,1,1);\n" +
                            "}"))
                    .withAttributeLocation(0, "vertex")
                    .build();

            glBindAttribLocation(quadShader.getId(), 0, "vertex");

            quadShader.use();
            quadShader.setUniform("projection", projection);
        }

        if (imageShader == null) {
            imageShader = Shader.create()
                    .withVertexShader(new VertexShader("#version 330\n" +
                            "layout(location = 0) in vec2 vertex;\n" +
                            "layout(location = 1) in vec2 texcoord;\n" +
                            "out vec2 vTexcoord;\n" +
                            "uniform mat4 projection;\n" +
                            "void main() {\n" +
                            "  vTexcoord=texcoord;\n" +
                            "  gl_Position = projection * vec4(vertex, 0, 1);\n" +
                            "}"))
                    .withFragmentShader(new FragmentShader("#version 330\n" +
                            "in vec2 vTexcoord;\n" +
                            "out vec4 color;\n" +
                            "uniform sampler2D uTexture;\n" +
                            "void main(){\n" +
                            "  color = texture(uTexture, vTexcoord);\n" +
                            "}"))
                    .withAttributeLocation(0, "vertex")
                    .withAttributeLocation(1, "texcoord")
                    .build();

            imageShader.use();
            imageShader.setUniform("projection", projection);
        }
    }

    @Override
    public void render(Node node) {
        if (node instanceof Image) {
            renderImage((Image) node);
        } else if (node instanceof Quad) {
            renderQuad((Quad) node);
        }
    }

    @Override
    public void end() {
        for (QuadBatch batch : batches) {
            if (batch.getTextureId() > 0) {
                imageShader.use();
            } else {
                quadShader.use();
            }

            if (batch.isDirty()) {
                batch.upload();
            }

            bindTexture(batch.getTextureId());

            batch.draw();
        }

        if (batches.isEmpty()) {
            currentBatchIndex = -1;
        } else {
            currentBatchIndex = 0;
        }
    }

    private void renderQuad(Quad node) {
        QuadBatch batch = getBatch(node, 0);
        batch.addQuad(node);
    }

    private void renderImage(Image node) {
        QuadBatch batch = getBatch(node, node.getTexture().getId());
        batch.addQuad(node);
    }

    private QuadBatch getBatch(Quad quad, int textureId) {
        QuadBatch batch = getCurrentBatch();
        if (batch == null || batch.isStateChangeRequired(textureId)) {

            // try if next batch is available & compatible
            currentBatchIndex++;
            batch = getCurrentBatch();

            if (batch == null || batch.isStateChangeRequired(textureId)) {
                batch = new QuadBatch(textureId);
                batches.add(batch);
            }
        }

        return batch;
    }

    private QuadBatch getCurrentBatch() {
        if (currentBatchIndex >= 0 && currentBatchIndex < batches.size()) {
            return batches.get(currentBatchIndex);
        }
        return null;
    }

    private void bindTexture(int textureId) {
        if (textureId != currentTextureId) {
            glBindTexture(GL_TEXTURE_2D, textureId);
            currentTextureId = textureId;
        }
    }

}
