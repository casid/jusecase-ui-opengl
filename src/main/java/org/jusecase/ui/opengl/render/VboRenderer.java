package org.jusecase.ui.opengl.render;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.Node;
import org.jusecase.scenegraph.Quad;
import org.jusecase.scenegraph.render.Renderer;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class VboRenderer implements Renderer {
    private int currentTextureId;

    private List<QuadBatch> batches = new ArrayList<>();
    private int currentBatchIndex = -1;

    @Override
    public void begin() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, 1, -1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        currentTextureId = -1;
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
