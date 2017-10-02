package org.jusecase.ui.opengl.render;

import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.node2d.Image;
import org.jusecase.scenegraph.Node;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.math.Matrix3x2;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.ui.opengl.shader.Shader;
import org.jusecase.ui.opengl.shader.stage.FragmentShader;
import org.jusecase.ui.opengl.shader.stage.VertexShader;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

@Component
public class VboRenderer implements Renderer {

    @Inject
    private ApplicationBackend applicationBackend;

    private int currentTextureId;

    private final List<QuadBatch> batches = new ArrayList<>();
    private int currentBatchIndex = -1;

    private Shader quadShader;
    private Shader imageShader;

    private Matrix3x2 projection;

    public VboRenderer() {
        applicationBackend.onResize().add(this::onResize);
    }

    @Override
    public void begin() {
        currentTextureId = -1;

        if (quadShader == null) {
            quadShader = createShader(false);
        }

        if (imageShader == null) {
            imageShader = createShader(true);
        }

        if (projection == null) {
            projection = new Matrix3x2();
            onResize(applicationBackend.getWidth(), applicationBackend.getHeight());
        }
    }

    private Shader createShader(boolean texCoords) {
        return Shader.create()
                .withVertexShader(new VertexShader(createVertexShaderSource(texCoords)))
                .withFragmentShader(new FragmentShader(createFragmentShaderSource(texCoords)))
                .build();
    }

    private String createVertexShaderSource(boolean texCoords) {
        String source = "#version 330\n";
        source += "layout(location = 0) in vec2 vertex;\n";
        source += "layout(location = 1) in vec4 color;\n";
        source += "out vec4 vColor;\n";
        if (texCoords) {
            source += "layout(location = 2) in vec2 texcoord;\n";
            source += "out vec2 vTexcoord;\n";
        }
        source += "uniform mat4 projection;\n";
        source += "void main() {\n";
        source += "  vColor=color;\n";
        if (texCoords) {
            source += "  vTexcoord=texcoord;\n";
        }
        source += "  gl_Position = projection * vec4(vertex.xy, 0, 1);\n";
        source += "}";

        return source;
    }

    private String createFragmentShaderSource(boolean texCoords) {
        String source = "#version 330\n";
        source += "in vec4 vColor;\n";
        if (texCoords) {
            source += "in vec2 vTexcoord;\n";
            source += "uniform sampler2D uTexture;\n";
        }
        source += "out vec4 fragColor;\n";
        source += "void main() {\n";
        if (texCoords) {
            source += "  fragColor = vColor * texture(uTexture, vTexcoord);\n";
        } else {
            source += "  fragColor = vColor;\n";
        }
        source += "}";

        return source;
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
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

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

    @Override
    public void dispose() {
        applicationBackend.onResize().remove(this::onResize);

        if (quadShader != null) {
            quadShader.dispose();
        }

        if (imageShader != null) {
            imageShader.dispose();
        }

        for (QuadBatch batch : batches) {
            batch.dispose();
        }
        batches.clear();
    }

    private void renderQuad(Quad node) {
        QuadBatch batch = getBatch(node, 0);
        batch.addQuad(node);
    }

    private void renderImage(Image node) {
        QuadBatch batch = getBatch(node, node.getTexture().getId());
        batch.addQuad(node);
    }

    private void onResize(int width, int height) {
        Matrix3x2.orthoProjection(width, height, projection);

        if (quadShader != null) {
            quadShader.use();
            quadShader.setUniform("projection", projection);
        }

        if (imageShader != null) {
            imageShader.use();
            imageShader.setUniform("projection", projection);
        }
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
