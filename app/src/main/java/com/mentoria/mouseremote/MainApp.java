package com.mentoria.mouseremote;

import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainApp {

    private static final int HTTP_PORT = 8080;
    private static final int WS_PORT = 8081;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MainApp.class);

    private static Logger logger = Logger.getLogger(MainApp.class.getName());

    public static void main(String[] args) throws Exception {
        MouseController mouseController = new MouseController();

        // 1) Sobe o servidor que entrega a página HTML pro navegador do celular
        WebPageServer.start(HTTP_PORT);

        // 2) Sobe o servidor WebSocket que recebe os toques e move o mouse
        TouchWebSocketServer wsServer = new TouchWebSocketServer(WS_PORT, mouseController);
        wsServer.start();

        logger.info("\"=======================================================\"");
        logger.info("Tudo pronto! No navegador do celular, acesse:");
        logger.info(" http://" + descobrirIpLocal() + ":" + HTTP_PORT);
        logger.info("\"=======================================================\"");

    }

    /**
     * getLocalHost() não é confiável no Linux: em várias distros (Debian/Ubuntu),
     * o hostname da máquina resolve para 127.0.1.1 no /etc/hosts, que é um
     * endereço de LOOPBACK (só funciona "de dentro pra dentro" do próprio PC).
     * Por isso, aqui a gente varre as interfaces de rede de verdade e pega
     * o primeiro IPv4 "de rede local" (ex: 192.168.x.x ou 10.x.x.x).
     */
    private static String descobrirIpLocal() {
        try {
            List<NetworkInterface> candidatas = getNetworkInterfaces();

            // 1ª prioridade: interfaces de Wi-Fi (normalmente começam com "wl", ex: wlan0, wlp3s0)
            String ipWifi = buscarIPv4(candidatas, n -> n.startsWith("wl"));
            if (ipWifi != null) return ipWifi;

            // 2ª prioridade: interfaces Ethernet (normalmente começam com "en" ou "eth")
            String ipEthernet = buscarIPv4(candidatas, n -> n.startsWith("en") || n.startsWith("eth"));
            if (ipEthernet != null) return ipEthernet;

            // Por último, qualquer coisa que sobrou na lista filtrada
            String ipQualquer = buscarIPv4(candidatas, n -> true);
            if (ipQualquer != null) return ipQualquer;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Não consegui detectar o IP automaticamente: {0}", e.getMessage());
        }
        return "SEU_IP_AQUI (rode `hostname -I` e use o IP no formato 192.168.x.x)";
    }

    private static List<NetworkInterface> getNetworkInterfaces() throws SocketException {
        List<NetworkInterface> candidatas = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            String nome = iface.getName().toLowerCase();

            if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                continue;
            }
            if (nome.startsWith("docker") || nome.startsWith("br-") || nome.startsWith("veth")
                    || nome.startsWith("virbr") || nome.startsWith("vmnet") || nome.startsWith("tun")
                    || nome.startsWith("tap")) {
                continue;
            }
            candidatas.add(iface);
        }
        return candidatas;
    }

    private static String buscarIPv4(List<NetworkInterface> interfaces,
                                     Predicate<String> filtroNome) {
        for (NetworkInterface iface : interfaces) {
            if (!filtroNome.test(iface.getName().toLowerCase())) continue;

            Enumeration<InetAddress> enderecos = iface.getInetAddresses();
            while (enderecos.hasMoreElements()) {
                InetAddress endereco = enderecos.nextElement();
                if (endereco instanceof Inet4Address && endereco.isSiteLocalAddress()) {
                    return endereco.getHostAddress();
                }
            }
        }
        return null;
    }
}
