package org.jusecase.ui.opengl.texture.stbi;

import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class StbiTextureLoader implements TextureLoader {

    @Override
    public Texture load(InputStream inputStream) throws IOException {
        ByteBuffer buffer = from(inputStream);
        buffer.flip();

        return load(buffer);
    }

    @Override
    public Texture load(Path path) throws IOException {
        ByteBuffer buffer = from(path);
        buffer.flip();

        return load(buffer);
    }

    @Override
    public Texture load(String resource) throws IOException {
        return load(fromResource(resource));
    }

    private Path fromResource(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException("Resource " + resource + " not found.");
        }

        try {
            return Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Resource " + resource + " not found.", e);
        }
    }

    private Texture load(ByteBuffer buffer) {
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer components = BufferUtils.createIntBuffer(1);

        ByteBuffer data = stbi_load_from_memory(buffer, width, height, components, 0);

        return new org.jusecase.ui.opengl.texture.Texture(width.get(0), height.get(0), components.get(0), data);
    }

    private ByteBuffer from(InputStream inputStream) throws IOException {
        try (ReadableByteChannel byteChannel = Channels.newChannel(inputStream)) {
            if (byteChannel instanceof SeekableByteChannel) {
                return from((SeekableByteChannel) byteChannel);
            } else {
                return from(byteChannel);
            }
        }
    }

    private ByteBuffer from(Path path) throws IOException {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path)) {
            return from(byteChannel);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private ByteBuffer from(SeekableByteChannel byteChannel) throws IOException {
        ByteBuffer buffer = BufferUtils.createByteBuffer((int) byteChannel.size() + 1);
        while (byteChannel.read(buffer) != -1) ;

        return buffer;
    }

    private ByteBuffer from(ReadableByteChannel byteChannel) throws IOException {
        ByteBuffer buffer = BufferUtils.createByteBuffer(1024);
        while (true) {
            int bytes = byteChannel.read(buffer);
            if (bytes == -1) {
                break;
            }
            if (buffer.remaining() == 0) {
                buffer = resizeBuffer(buffer, buffer.capacity() * 2);
            }
        }
        return buffer;
    }

    private ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
