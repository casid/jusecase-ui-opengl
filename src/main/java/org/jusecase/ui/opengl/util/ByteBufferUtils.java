package org.jusecase.ui.opengl.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class ByteBufferUtils {
    public static ByteBuffer from(InputStream inputStream) {
        try (ReadableByteChannel byteChannel = Channels.newChannel(inputStream)) {
            if (byteChannel instanceof SeekableByteChannel) {
                return from((SeekableByteChannel) byteChannel);
            } else {
                return from(byteChannel);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer from(Path path) {
        try (SeekableByteChannel byteChannel = Files.newByteChannel(path)) {
            return from(byteChannel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static ByteBuffer from(SeekableByteChannel byteChannel) {
        try {
            ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer((int) byteChannel.size() + 1);
            while (byteChannel.read(buffer) != -1) ;
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer from(ReadableByteChannel byteChannel) {
        return from(byteChannel, 1024 * 128);
    }

    public static ByteBuffer from(ReadableByteChannel byteChannel, int initialCapacity) {
        try {
            ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(initialCapacity);
            while (true) {
                int bytes = 0;
                bytes = byteChannel.read(buffer);
                if (bytes == -1) {
                    break;
                }
                if (buffer.remaining() == 0) {
                    buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = org.lwjgl.BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
