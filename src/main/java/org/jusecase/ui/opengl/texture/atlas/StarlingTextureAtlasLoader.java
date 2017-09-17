package org.jusecase.ui.opengl.texture.atlas;

import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.jusecase.ui.opengl.util.PathUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;

public class StarlingTextureAtlasLoader {

    private final TextureLoader textureLoader;

    public StarlingTextureAtlasLoader(TextureLoader textureLoader) {
        this.textureLoader = textureLoader;
    }

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
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            if ("TextureAtlas".equals(qName)) {
                String imagePath = attributes.getValue("imagePath");
                Texture texture = textureLoader.load(directory.resolve(imagePath));
                textureAtlas = new TextureAtlas(texture);
            }

            if ("SubTexture".equals(qName)) {
                String name = attributes.getValue("name");
                int x = Integer.parseInt(attributes.getValue("x"));
                int y = Integer.parseInt(attributes.getValue("y"));
                int w = Integer.parseInt(attributes.getValue("width"));
                int h = Integer.parseInt(attributes.getValue("height"));

                textureAtlas.put(name, x, y, w, h);
            }
        }
    }
}