package com.mentoria.mouseremote;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

/**
 * Classe responsável por, de fato, mexer no cursor do sistema operacional.
 * Tudo aqui usa java.awt.Robot, que já vem no JDK — nenhuma dependência externa.
 */
public class MouseController {

    private final Robot robot;

    public MouseController() throws AWTException {
        // Robot é a classe do JDK que "finge" ser um usuário mexendo mouse/teclado
        this.robot = new Robot();
    }

    /**
     * Move o cursor de forma RELATIVA (modo trackpad).
     * dx/dy são o quanto o dedo andou desde o último evento, em pixels.
     */
    public void moveRelative(int dx, int dy) {
        Point atual = MouseInfo.getPointerInfo().getLocation();
        robot.mouseMove(atual.x + dx, atual.y + dy);
    }

    /** Simula um clique esquerdo simples (pressionar + soltar). */
    public void clickLeft() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void pressLeft() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    }

    public void releaseLeft() {
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
