package org.jusecase.ui.opengl.render;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.Quad;
import org.jusecase.scenegraph.texture.TexCoords;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

class QuadBatch {
    private List<Quad> quads = new ArrayList<>();
    private List<Integer> quadHashes = new ArrayList<>();
    private FloatBuffer vertexBuffer;
    private boolean dirty = true;
    private int nextIndex;
    private int vaoId;
    private int vboId;
    private final int textureId;

    private final float[][] quadVertices = {
            {0, 0},
            {0, 0},
            {0, 0},
            {0, 0}
    };

    QuadBatch(int textureId) {
        this.textureId = textureId;
    }

    void addQuad(Quad quad) {
        if (dirty) {
            quads.add(quad);
            quadHashes.add(hash(quad));
        } else {
            if (quads.size() > nextIndex && quads.get(nextIndex) == quad && quadHashes.get(nextIndex) == hash(quad)) {
                ++nextIndex; // no change
            } else {
                dirty = true;
                while (quads.size() > nextIndex) {
                    quads.remove(quads.size() - 1);
                    quadHashes.remove(quadHashes.size() - 1);
                }
                addQuad(quad);
            }
        }
    }

    private int hash(Quad quad) {
        return quad.getGlobalMatrix().hashCode();
    }

    private void addVertices(Quad quad) {
        transformQuadVertices(quad);

        vertexBuffer.put(quadVertices[0]);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadVertices[1]);

        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadVertices[2]);
    }

    private void transformQuadVertices(Quad quad) {
        quadVertices[0][0] = 0;
        quadVertices[0][1] = 0;
        quadVertices[1][0] = (float)quad.getWidth();
        quadVertices[1][1] = 0;
        quadVertices[2][0] = (float)quad.getWidth();
        quadVertices[2][1] = (float)quad.getHeight();
        quadVertices[3][0] = 0;
        quadVertices[3][1] = (float)quad.getHeight();

        for (float[] vertex : quadVertices) {
            quad.getGlobalMatrix().transformPoint(vertex);
        }
    }

    private void addVertices(Image image) {
        transformQuadVertices(image);

        TexCoords texCoords = image.getTexture().getTexCoords();

        vertexBuffer.put(quadVertices[0]);
        vertexBuffer.put((float)texCoords.left).put((float)texCoords.top);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put((float)texCoords.left).put((float)texCoords.bottom);
        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put((float)texCoords.right).put((float)texCoords.top);


        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put((float)texCoords.right).put((float)texCoords.top);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put((float)texCoords.left).put((float)texCoords.bottom);
        vertexBuffer.put(quadVertices[2]);
        vertexBuffer.put((float)texCoords.right).put((float)texCoords.bottom);
    }

    void draw() {
        glBindVertexArray(vaoId);

        if (textureId == 0) {
            glEnableVertexAttribArray(0);
            glDrawArrays(GL_TRIANGLES, 0, vertexBuffer.limit() / 2);
            glDisableVertexAttribArray(0);
        } else {
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glDrawArrays(GL_TRIANGLES, 0, vertexBuffer.limit() / 4);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
        }

        glBindVertexArray(0);

        nextIndex = 0;
    }

    boolean isDirty() {
        return dirty;
    }

    void upload() {
        int requiredCapacity = getRequiredCapacity();
        if (vertexBuffer == null) {
            System.out.println("Create buffer");
            vertexBuffer = BufferUtils.createFloatBuffer(requiredCapacity);
        } else {
            if (vertexBuffer.capacity() < requiredCapacity) {
                System.out.println("Resize buffer");
                vertexBuffer = BufferUtils.createFloatBuffer(requiredCapacity);
            } else {
                System.out.println("Uploading buffer");
                vertexBuffer.clear();
            }
        }

        if (textureId > 0) {
            for (Quad quad : quads) {
                addVertices((Image)quad);
            }
        } else {
            for (Quad quad : quads) {
                addVertices(quad);
            }
        }

        vertexBuffer.flip();

        if (vaoId == 0) {
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            vboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);
            if (textureId == 0) {
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
            } else {
                // 4 bytes per float
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*4, 0);
                glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*4, 2*4);
            }

            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        } else {
            glBindVertexArray(vaoId);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);

            glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_DYNAMIC_DRAW);

            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        dirty = false;
    }

    private int getRequiredCapacity() {
        int oneElement = 2 * 6;
        if (textureId > 0) {
            oneElement += 2 * 6;
        }
        return oneElement * quads.size();
    }

    void dispose() {
        if (vboId > 0) {
            glDeleteBuffers(vboId);
            vboId = 0;
        }

        if (vaoId > 0) {
            glDeleteVertexArrays(vaoId);
            vaoId = 0;
        }

        vertexBuffer = null;
    }

    public int getTextureId() {
        return textureId;
    }

    public boolean isStateChangeRequired(int textureId) {
        return this.textureId != textureId;
    }
}
