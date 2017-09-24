package org.jusecase.ui.opengl;

import org.jusecase.scenegraph.Image;
import org.jusecase.scenegraph.Image3Slice;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.scenegraph.texture.TextureLoader;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.opengl.texture.atlas.StarlingTextureAtlasLoader;
import org.jusecase.ui.opengl.texture.stbi.StbiTextureLoader;
import org.jusecase.ui.style.ImageButtonStyle;
import org.jusecase.ui.style.QuadButtonStyle;
import org.jusecase.ui.touch.TouchEvent;
import org.jusecase.ui.touch.TouchPhase;

public class Playground extends LwjglApplication {

    private Button button = new Button();
    private Image image;

    public static void main(String[] args) {
        new Playground().start();
    }


    @Override
    protected void onStart() {
        initStyles();
        addSampleButtons();
        addSampleImages();
    }

    @Override
    protected void onUpdate() {
        image.setRotation(image.getRotation() + 1);
    }

    private void initStyles() {
        QuadButtonStyle buttonStyle = new QuadButtonStyle();
        buttonStyle.active.setColor(new Color("#fff", 0.5f));
        buttonStyle.hovered.setColor(new Color("#f0f", 0.5f));
        buttonStyle.pressed.setColor(new Color("#fff", 0.5f));
        ui.setDefaultStyle(Button.class, buttonStyle);
    }

    private void addSampleImages() {
        TextureLoader textureLoader = new StbiTextureLoader();
        StarlingTextureAtlasLoader textureAtlasLoader = new StarlingTextureAtlasLoader(textureLoader);

        TextureAtlas textureAtlas = textureAtlasLoader.load("images/atlas.xml");

        Texture texture = textureAtlas.get("tower-inventory-potions-iu");
        image = new Image(texture);
        ui.addFirst(image.setX(200).setY(200).setPivot(0.5f, 0.5f));

        ImageButtonStyle style = new ImageButtonStyle();
        style.active = new Image(texture);
        style.hovered = (Image)new Image(texture).setColor(new Color("#0f0"));
        style.pressed = (Image)new Image(texture).setColor(new Color("#f00"));

        for (int i = 0; i < 200; ++i) {
            Button textureButton = new Button();
            textureButton.onTouch.add(this::dragButton);
            textureButton.onClick.add(button -> button.setRotation(button.getRotation() + 10));

            textureButton.setStyle(style);
            ui.add(textureButton.setX(10 + Math.min(i, 200)).setY(300).setPivot(0.5f, 0.5f).setRotation((float)Math.random() * 45));
        }

        Image3Slice image3Slice = new Image3Slice(textureAtlas, "tower-skill-button-cooldown-left", "tower-skill-button-cooldown-center", "tower-skill-button-cooldown-right");
        ui.add(image3Slice.setPosition(400, 300).setWidth(200));
    }

    private void addSampleButtons() {
        button.setX(100).setY(100).setWidth(200).setHeight(50);
        button.onClick.add(e -> {
            QuadButtonStyle style = (QuadButtonStyle) button.getStyle();
            Color color = new Color().random();
            style.active.setColor(color);
            style.hovered.setColor(color);
            System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

            Button randomButton = new Button();
            button.add(randomButton.setPosition(800 * (float)Math.random(), 600 * (float)Math.random()).setSize(20, 20).setRotation(90 * (float)Math.random()));
        });
        ui.add(button);

        Button moveableButton = new Button();
        ui.add(moveableButton.setX(120).setY(120).setWidth(200).setHeight(200).setRotation(20));
        moveableButton.onTouch.add(this::dragButton);
        moveableButton.onClick.add(e -> {
            System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
        });

        Button moveableButtonChild = new Button();
        moveableButton.add(moveableButtonChild.setX(120).setY(120).setWidth(20).setHeight(20).setRotation(45).setScaleX(10).setScaleY(2));

        Button moveableButtonChild2 = new Button();
        moveableButton.add(moveableButtonChild2.setX(120).setY(120).setWidth(20).setHeight(20));
    }

    private void dragButton(TouchEvent touchEvent) {
        Button button = (Button) touchEvent.element;

        if (button.isPressed() && touchEvent.phase == TouchPhase.Move) {
            button.setX(button.getX() + touchEvent.deltaX);
            button.setY(button.getY() + touchEvent.deltaY);
        }
    }
}
