package com.mentoria.mouseremote;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

/**
 * Servidor WebSocket. Fica "escutando" na porta 8081 esperando o celular
 * se conectar e mandar mensagens de texto simples, tipo:
 *   "MOVE:12:-3"   -> mover o cursor 12px em x e -3px em y
 *   "CLICK"        -> clique esquerdo
 *
 * Escolhi um formato de texto bem simples (sem JSON) de propósito,
 * pra você não precisar de nenhuma lib de parsing agora. Dá pra evoluir depois.
 */
public class TouchWebSocketServer extends WebSocketServer {

    private final MouseController mouseController;

    public TouchWebSocketServer(int port, MouseController mouseController) {
        super(new InetSocketAddress(port));
        this.mouseController = mouseController;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("[WS] Celular conectado: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("[WS] Celular desconectado.");
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            if (message.startsWith("MOVE:")) {
                // Formato: MOVE:dx:dy
                String[] partes = message.split(":");
                int dx = Integer.parseInt(partes[1]);
                int dy = Integer.parseInt(partes[2]);
                mouseController.moveRelative(dx, dy);

            } else if (message.equals("CLICK")) {
                mouseController.clickLeft();
            } else if (message.equals("DOWN")) {
                mouseController.pressLeft();
            } else if (message.equals("UP")) {
                mouseController.releaseLeft();
            }
        } catch (Exception e) {
            System.err.println("[WS] Mensagem inválida recebida: " + message);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("[WS] Erro: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        System.out.println("[WS] Servidor WebSocket ouvindo na porta " + getPort());
    }
}
