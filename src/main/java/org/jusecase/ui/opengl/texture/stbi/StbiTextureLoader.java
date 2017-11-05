package org.jusecase.ui.opengl.texture.stbi;

import org.jusecase.inject.Component;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.jusecase.util.PathUtils;
import org.lwjgl.BufferUtils;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;

import static org.jusecase.ui.opengl.util.ByteBufferUtils.from;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

@Component
public class StbiTextureLoader implements TextureLoader {

    @Override
    public Texture load(InputStream inputStream) {
        ByteBuffer buffer = from(inputStream);
        buffer.flip();

        return load(buffer);
    }

    @Override
    public Texture load(Path path) {
        ByteBuffer buffer = from(path);
        buffer.flip();

        return load(buffer);
    }

    @Override
    public Texture load(String resource) {
        return load(PathUtils.fromResource(resource));
    }

    private Texture load(ByteBuffer buffer) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer components = BufferUtils.createIntBuffer(1);

        ByteBuffer data = stbi_load_from_memory(buffer, width, height, components, 0);

        return new org.jusecase.ui.opengl.texture.Texture(width.get(0), height.get(0), components.get(0), data);
    }
}
