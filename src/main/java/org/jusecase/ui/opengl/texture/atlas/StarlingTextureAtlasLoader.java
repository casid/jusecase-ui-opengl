package org.jusecase.ui.opengl.texture.atlas;

import org.jusecase.inject.Component;
import org.jusecase.scenegraph.texture.*;
import org.jusecase.util.PathUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.inject.Inject;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;

@Component
public class StarlingTextureAtlasLoader implements TextureAtlasLoader {

    @Inject
    private TextureLoader textureLoader;

    @Override
    public TextureAtlas load(Path definitionPath) {
        Loader loader = new Loader(definitionPath.getParent());

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(definitionPath.toFile(), loader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return loader.textureAtlas;
    }

    @Override
    public TextureAtlas load(String definitionResource) {
        return load(PathUtils.fromResource(definitionResource));
    }

    private class Loader extends DefaultHandler {
        private final Path directory;
        private TextureAtlas textureAtlas;

        private Loader(Path directory) {
            this.directory = directory;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {

            if ("TextureAtlas".equals(qName)) {
                String imagePath = attributes.getValue("imagePath");
                Texture texture = textureLoader.load(directory.resolve(imagePath));
                textureAtlas = new TextureAtlas(texture);
            }

            if ("SubTexture".equals(qName)) {
                String name = attributes.getValue("name");
                int x = parseInteger(attributes, "x");
                int y = parseInteger(attributes, "y");
                int w = parseInteger(attributes, "width");
                int h = parseInteger(attributes, "height");

                int fx = parseInteger(attributes, "frameX");
                int fy = parseInteger(attributes, "frameY");
                int fw = parseInteger(attributes, "frameWidth");
                int fh = parseInteger(attributes, "frameHeight");

                if (fx == 0 && fy == 0 && fw == 0 && fh == 0) {
                    textureAtlas.put(name, x, y, w, h);
                } else {
                    textureAtlas.put(name, x, y, w, h, new TextureFrame(-fx, fh - h + fy, fw - w + fx, -fy));
                }

            }
        }

        private int parseInteger(Attributes attributes, String name) {
            String value = attributes.getValue(name);
            if (value == null) {
                return 0;
            } else {
                return Integer.parseInt(value);
            }
        }
    }
}
