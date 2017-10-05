package org.jusecase.ui.opengl;

import org.jusecase.Application;
import org.jusecase.ApplicationBackend;
import org.jusecase.inject.Component;
import org.jusecase.scenegraph.node2d.Image;
import org.jusecase.scenegraph.node2d.Image3Slice;
import org.jusecase.scenegraph.color.Color;
import org.jusecase.scenegraph.render.Renderer;
import org.jusecase.scenegraph.texture.Texture;
import org.jusecase.scenegraph.texture.TextureAtlas;
import org.jusecase.scenegraph.texture.TextureAtlasLoader;
import org.jusecase.ui.Ui;
import org.jusecase.ui.elements.Button;
import org.jusecase.ui.elements.Label;
import org.jusecase.ui.font.Align;
import org.jusecase.ui.font.Font;
import org.jusecase.ui.opengl.font.BitmapFontLoader;
import org.jusecase.ui.style.ButtonStyle;
import org.jusecase.ui.touch.TouchEvent;
import org.jusecase.ui.touch.TouchPhase;

import javax.inject.Inject;

@Component
public class Playground implements Application {

    @Inject
    private ApplicationBackend applicationBackend;

    @Inject
    private TextureAtlasLoader textureAtlasLoader;

    @Inject
    private BitmapFontLoader bitmapFontLoader;

    private Ui ui = new Ui();

    private Button button = new Button();
    private Image image;
    private TextureAtlas textureAtlas;
    private Font bitmapFont;
    private Button moveableButton;


    public static void main(String[] args) {
        new LwjglApplicationBackend(Playground.class).start();
    }

    @Override
    public void init() {
        initStyles();
        addSampleButtons();
        addSampleImages();
        addSampleTexts();
    }

    @Override
    public void process(TouchEvent touchEvent) {
        ui.process(touchEvent);
    }

    @Override
    public void update() {
        image.setRotation(image.getRotation() + 1);
    }

    @Override
    public void render(Renderer renderer) {
        renderer.render(ui);
    }

    private void initStyles() {
        ButtonStyle buttonStyle = new ButtonStyle();
        buttonStyle.active.color = new Color("#fff", 0.5f);
        buttonStyle.hovered.color = new Color("#f0f", 0.5f);
        buttonStyle.pressed.color = new Color("#fff", 0.5f);
        ui.setDefaultStyle(Button.class, buttonStyle);
    }

    private void addSampleImages() {
        textureAtlas = textureAtlasLoader.load("images/atlas.xml");

        Texture texture = textureAtlas.get("tower-inventory-potions-iu");
        image = new Image(texture);
        ui.addFirst(image.setX(200).setY(0).setPivot(0.5f, 0.5f));

        ButtonStyle style = new ButtonStyle();
        style.active.texture = texture;
        style.hovered.color = new Color("#0f0");
        style.pressed.color = new Color("#f00");

        for (int i = 0; i < 200; ++i) {
            Button textureButton = new Button();
            textureButton.onTouch.add(this::dragButton);
            textureButton.onClick.add(button -> button.setRotation(button.getRotation() + 10));

            textureButton.setStyle(style);
            ui.add(textureButton.setX(10 + Math.min(i, 200)).setY(300).setPivot(0.5f, 0.5f).setRotation((float) Math.random() * 45));
        }

        Image3Slice image3Slice = new Image3Slice(textureAtlas, "tower-skill-button-cooldown-left", "tower-skill-button-cooldown-center", "tower-skill-button-cooldown-right");
        ui.add(image3Slice.setPosition(400, 300).setWidth(200));
    }

    private void addSampleButtons() {
        button.setX(100).setY(100).setWidth(200).setHeight(50);
        button.onClick.add(e -> {
            Color color = new Color().random();
            button.getStyle().active.color = color;
            button.getStyle().hovered.color = color;
            System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));

            Button randomButton = new Button();
            button.add(randomButton.setPosition(800 * (float) Math.random(), 600 * (float) Math.random()).setSize(20, 20).setRotation(90 * (float) Math.random()));
        });
        ui.add(button);

        moveableButton = new Button();
        ui.add(moveableButton.setX(120).setY(120).setWidth(200).setHeight(200).setRotation(20));
        moveableButton.onTouch.add(this::dragButton);
        moveableButton.onClick.add(e -> System.out.println("Used memory: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())));

        Button moveableButtonChild = new Button();
        moveableButton.add(moveableButtonChild.setX(120).setY(120).setWidth(20).setHeight(20).setRotation(45).setScaleX(10).setScaleY(2));

        Button moveableButtonChild2 = new Button();
        moveableButton.add(moveableButtonChild2.setX(120).setY(120).setWidth(20).setHeight(20));
        moveableButtonChild2.onClick.add(b -> applicationBackend.exit());
    }

    private void addSampleTexts() {
        bitmapFont = bitmapFontLoader.load("fonts/font-comic.fnt");
        Label label = new Label(bitmapFont);
        label.setAlign(Align.CENTER);
        label.setText("Hello world!\nHere comes line two...");
        moveableButton.add(label);
    }

    private void dragButton(TouchEvent touchEvent) {
        Button button = (Button) touchEvent.element;

        if (button.isPressed() && touchEvent.phase == TouchPhase.Move) {
            button.setX(button.getX() + touchEvent.deltaX);
            button.setY(button.getY() + touchEvent.deltaY);
        }
    }

    @Override
    public void dispose() {
        textureAtlas.dispose();
        bitmapFont.dispose();
    }
}
