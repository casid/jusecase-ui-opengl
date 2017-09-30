package org.jusecase.ui.opengl.font;

import org.jusecase.inject.Component;
import org.jusecase.scenegraph.texture.SubTexture;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.jusecase.ui.font.BitmapFont;
import org.jusecase.ui.font.BitmapFontCharacter;
import org.jusecase.ui.opengl.util.PathUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.inject.Inject;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BitmapFontLoader {

    @Inject
    private TextureLoader textureLoader;

    public BitmapFont load(String resourcePath) {
        return load(PathUtils.fromResource(resourcePath));
    }

    public BitmapFont load(Path path) {
        Loader loader = new Loader(path.getParent());

        try {
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(path.toFile(), loader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new BitmapFont(loader.characters, loader.kernings, loader.pages.values());
    }

    private class Loader extends DefaultHandler {
        private final Path directory;

        private final Map<String, Texture> pages = new HashMap<>();
        private final List<BitmapFontCharacter> characters = new ArrayList<>();
        private final Map<String, Integer> kernings = new HashMap<>();

        private Loader(Path directory) {
            this.directory = directory;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("page".equals(qName)) {
                addPage(attributes);
            }

            if ("char".equals(qName)) {
                addChar(attributes);
            }

            if ("kerning".equals(qName)) {
                addKerning(attributes);
            }
        }

        private void addPage(Attributes attributes) {
            String id = attributes.getValue("id");
            Path file = directory.resolve(attributes.getValue("file"));
            pages.put(id, textureLoader.load(file));
        }

        private void addChar(Attributes attributes) {
            BitmapFontCharacter character = new BitmapFontCharacter();
            character.id = (char)Integer.parseInt(attributes.getValue("id"));

            String page = attributes.getValue("page");
            int x = Integer.parseInt(attributes.getValue("x"));
            int y = Integer.parseInt(attributes.getValue("y"));
            int w = Integer.parseInt(attributes.getValue("width"));
            int h = Integer.parseInt(attributes.getValue("height"));
            character.texture = new SubTexture(pages.get(page), x, y, w, h);

            character.offsetX = Integer.parseInt(attributes.getValue("xoffset"));
            character.offsetY = Integer.parseInt(attributes.getValue("yoffset"));
            character.advanceX = Integer.parseInt(attributes.getValue("xadvance"));

            characters.add(character);
        }

        private void addKerning(Attributes attributes) {
            char first = (char)Integer.parseInt(attributes.getValue("first"));
            char second = (char)Integer.parseInt(attributes.getValue("second"));
            int amount = Integer.parseInt(attributes.getValue("amount"));

            kernings.put("" + first + second, amount);
        }
    }
}
