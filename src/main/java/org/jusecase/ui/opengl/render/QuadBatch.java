package org.jusecase.ui.opengl.render;

import org.jusecase.scenegraph.node2d.Image;
import org.jusecase.scenegraph.node2d.Quad;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.texture.TexCoords;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureFrame;
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
    private boolean unused;
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

    private final float[] quadColor = {0, 0, 0, 0};

    private static final int FLOAT_BYTES = 4;

    QuadBatch(int textureId) {
        this.textureId = textureId;
    }

    void addQuad(Quad quad) {
        unused = false;

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
        int result = quad.getGlobalMatrix().hashCode();
        result = 31 * result + quad.getColor().hashCode();
        return result;
    }

    private void addVertices(Quad quad) {
        fillQuadVertices(quad, null);
        fillQuadColor(quad);

        vertexBuffer.put(quadVertices[0]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put(quadColor);

        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(quadVertices[2]);
        vertexBuffer.put(quadColor);
    }

    private void fillQuadVertices(Quad quad, Texture texture) {
        if (texture == null || texture.getFrame() == null) {
            quadVertices[0][0] = 0;
            quadVertices[0][1] = 0;
            quadVertices[1][0] = quad.getWidth();
            quadVertices[1][1] = 0;
            quadVertices[2][0] = quad.getWidth();
            quadVertices[2][1] = quad.getHeight();
            quadVertices[3][0] = 0;
            quadVertices[3][1] = quad.getHeight();
        } else {
            TextureFrame frame = texture.getFrame();
            float left = frame.left;
            float top = frame.top;
            float right = texture.getWidth() - frame.right;
            float bottom = texture.getHeight() - frame.bottom;

            quadVertices[0][0] = left;
            quadVertices[0][1] = top;
            quadVertices[1][0] = right;
            quadVertices[1][1] = top;
            quadVertices[2][0] = right;
            quadVertices[2][1] = bottom;
            quadVertices[3][0] = left;
            quadVertices[3][1] = bottom;
        }

        for (float[] vertex : quadVertices) {
            quad.getGlobalMatrix().transformPoint(vertex);
        }
    }

    private void fillQuadColor(Quad quad) {
        Color color = quad.getColor();
        quadColor[0] = color.r;
        quadColor[1] = color.g;
        quadColor[2] = color.b;
        quadColor[3] = color.a;
    }

    private void addVertices(Image image) {
        fillQuadVertices(image, image.getTexture());
        fillQuadColor(image);

        TexCoords texCoords = image.getTexture().getTexCoords();

        vertexBuffer.put(quadVertices[0]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.left).put(texCoords.top);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.left).put(texCoords.bottom);
        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.right).put(texCoords.top);

        vertexBuffer.put(quadVertices[1]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.right).put(texCoords.top);
        vertexBuffer.put(quadVertices[3]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.left).put(texCoords.bottom);
        vertexBuffer.put(quadVertices[2]);
        vertexBuffer.put(quadColor);
        vertexBuffer.put(texCoords.right).put(texCoords.bottom);
    }

    void draw() {
        glBindVertexArray(vaoId);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        if (textureId == 0) {
            glDrawArrays(GL_TRIANGLES, 0, quads.size() * 6);
        } else {
            glEnableVertexAttribArray(2);
            glDrawArrays(GL_TRIANGLES, 0, quads.size() * 6);
            glDisableVertexAttribArray(2);
        }
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        glBindVertexArray(0);

        nextIndex = 0;
    }

    boolean isDirty() {
        return dirty;
    }

    void upload() {
        int requiredCapacity = getRequiredCapacity();
        if (vertexBuffer == null) {
            vertexBuffer = BufferUtils.createFloatBuffer(requiredCapacity);
        } else {
            if (vertexBuffer.capacity() < requiredCapacity) {
                vertexBuffer = BufferUtils.createFloatBuffer(requiredCapacity);
            } else {
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
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 6 * FLOAT_BYTES, 0);
                glVertexAttribPointer(1, 4, GL_FLOAT, false, 6 * FLOAT_BYTES, 2 * FLOAT_BYTES);
            } else {
                glVertexAttribPointer(0, 2, GL_FLOAT, false, 8 * FLOAT_BYTES, 0);
                glVertexAttribPointer(1, 4, GL_FLOAT, false, 8 * FLOAT_BYTES, 2 * FLOAT_BYTES);
                glVertexAttribPointer(2, 2, GL_FLOAT, false, 8 * FLOAT_BYTES, 6 * FLOAT_BYTES);
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
        int oneElement = 2 + 4;
        if (textureId > 0) {
            oneElement += 2;
        }
        return oneElement * 6 * quads.size();
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

    public boolean isUnused() {
        return unused;
    }

    public void setUnused(boolean unused) {
        this.unused = unused;
    }
}
