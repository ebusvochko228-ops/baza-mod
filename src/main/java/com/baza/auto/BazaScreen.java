package com.baza.auto;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

public class BazaScreen extends Screen {
    private boolean listening = false;
    private ButtonWidget toggleButton;
    private ButtonWidget keyButton;

    public BazaScreen() {
        super(Text.literal("Baza Mod — меню"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2 - 100;

        toggleButton = ButtonWidget.builder(Text.literal(toggleLabel()), b -> {
            BazaModClient.active = !BazaModClient.active;
            toggleButton.setMessage(Text.literal(toggleLabel()));
        }).dimensions(cx, this.height / 2 - 40, 200, 20).build();
        addDrawableChild(toggleButton);

        keyButton = ButtonWidget.builder(Text.literal(keyLabel()), b -> {
            listening = true;
            keyButton.setMessage(Text.literal("Нажми любую клавишу..."));
        }).dimensions(cx, this.height / 2 - 10, 200, 20).build();
        addDrawableChild(keyButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), b -> close())
                .dimensions(cx, this.height / 2 + 20, 200, 20).build());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening && keyCode != 256) { // 256 = Esc
            BazaModClient.bindKey = keyCode;
            keyButton.setMessage(Text.literal(keyLabel()));
            listening = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title,
                this.width / 2, this.height / 2 - 60, 0xFFFFFF);
    }

    private String toggleLabel() {
        return "Мод: " + (BazaModClient.active ? "ВКЛЮЧЁН" : "ВЫКЛЮЧЕН");
    }

    private String keyLabel() {
        return "Клавиша меню: " + InputUtil.fromKeyCode(BazaModClient.bindKey, 0)
                .getLocalizedText().getString();
    }
}
