package org.jusecase.ui.opengl.texture;

import org.jusecase.scenegraph.math.DrawHash;
import org.jusecase.scenegraph.texture.TexCoords;
import org.jusecase.scenegraph.texture.TextureFrame;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture implements org.jusecase.scenegraph.texture.Texture {
    private final int width;
    private final int height;
    private final int components;
    private final ByteBuffer data;

    private int id;

    public Texture(int width, int height, int components, ByteBuffer data) {
        this.width = width;
        this.height = height;
        this.components = components;
        this.data = data;
    }

    @Override
    public int getId() {
        if (id == 0) {
            glEnable(GL_TEXTURE_2D);
            id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            data.flip();
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexImage2D(GL_TEXTURE_2D, 0, getFormat(), width, height, 0, getFormat(), GL_UNSIGNED_BYTE, data);
        }
        return id;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public TexCoords getTexCoords() {
        return TexCoords.DEFAULT;
    }

    @Override
    public TextureFrame getFrame() {
        return null;
    }

    private int getFormat() {
        switch (components) {
            case 3:
                return GL_RGB;
            case 4:
                return GL_RGBA;
        }
        throw new RuntimeException("Failed to map amount of components to texture format: " + components);
    }

    @Override
    public void dispose() {
        // TODO
    }

    public int getComponents() {
        return components;
    }

    public ByteBuffer getData() {
        return data;
    }

    @Override
    public void hash(DrawHash hash) {
        hash.add(id);
    }
}
