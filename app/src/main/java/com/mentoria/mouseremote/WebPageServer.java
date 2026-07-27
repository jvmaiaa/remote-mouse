package com.mentoria.mouseremote;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Servidor HTTP bem simples. Sua única função é: quando o navegador do
 * celular acessar http://IP-DO-PC:8080/, devolver o arquivo index.html
 * que está em src/main/resources/web/index.html.
 */
public class WebPageServer {

    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {
            byte[] html = readIndexHtml();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
        });

        server.setExecutor(null); // usa uma thread simples padrão
        server.start();
        System.out.println("[HTTP] Página disponível na porta " + port);
    }

    private static byte[] readIndexHtml() throws IOException {
        // Lê o arquivo de dentro do "jar"/classpath (resources/web/index.html)
        try (InputStream is = WebPageServer.class.getResourceAsStream("/web/index.html")) {
            if (is == null) {
                throw new IOException("index.html não encontrado em resources/web/");
            }
            return is.readAllBytes();
        }
    }
}
