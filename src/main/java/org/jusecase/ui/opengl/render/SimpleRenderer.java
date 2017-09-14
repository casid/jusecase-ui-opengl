package org.jusecase.ui.opengl.render;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.Node;
import org.jusecase.scenegraph.Node2d;
import org.jusecase.scenegraph.Quad;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.texture.TexCoords;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.ui.opengl.Matrix;
import org.jusecase.scenegraph.render.Renderer;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class SimpleRenderer implements Renderer {

    private int currentTextureId;
    private Color currentColor;
    private int amount;

    @Override
    public void begin() {
        amount = 0;

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, 800, 600, 0, 1, -1);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
    }

    @Override
    public void end() {
        System.out.println("Rendered " + amount + " nodes");
    }

    @Override
    public void render(Node node) {
        ++amount;

        if (node instanceof Image) {
            renderImage((Image)node);
        } else if (node instanceof Quad) {
            renderQuad((Quad)node);
        }
    }

    private void renderImage(Image node) {
        Texture texture = node.getTexture();
        if (texture != null) {
            loadMatrix(node);
            bindTexture(texture.getId());

            TexCoords texCoords = texture.getTexCoords();
            glBegin(GL_QUADS);
            setColor(node.getColor());
            glTexCoord2d(texCoords.left, texCoords.top);
            glVertex2d(0, 0);
            glTexCoord2d(texCoords.right, 0);
            glVertex2d(node.getWidth(), 0);
            glTexCoord2d(texCoords.right, texCoords.bottom);
            glVertex2d(node.getWidth(), node.getHeight());
            glTexCoord2d(texCoords.left, texCoords.bottom);
            glVertex2d(0, node.getHeight());
            glEnd();
        }
    }

    private void renderQuad(Quad node) {
        loadMatrix(node);
        bindTexture(0);

        glBegin(GL_QUADS);
        setColor(node.getColor());
        glVertex2d(0, 0);
        glVertex2d(node.getWidth(), 0);
        glVertex2d(node.getWidth(), node.getHeight());
        glVertex2d(0, node.getHeight());
        glEnd();
    }

    private void bindTexture(int textureId) {
        if (textureId != currentTextureId) {
            glBindTexture(GL_TEXTURE_2D, textureId);
            currentTextureId = textureId;
        }
    }

    private void loadMatrix(Node2d node) {
        GL11.glLoadMatrixd(Matrix.toOpenGlMatrix(node.getGlobalMatrix()));
    }

    private void setColor(Color color) {
        if (!color.equals(currentColor)) {
            glColor4d(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
            currentColor = color;
        }
    }
}
