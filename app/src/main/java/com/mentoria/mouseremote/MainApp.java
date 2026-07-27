package com.mentoria.mouseremote;

public class MainApp {

    private static final int HTTP_PORT = 8080;
    private static final int WS_PORT = 8081;

    public static void main(String[] args) throws Exception {
        MouseController mouseController = new MouseController();

        // 1) Sobe o servidor que entrega a página HTML pro navegador do celular
        WebPageServer.start(HTTP_PORT);

        // 2) Sobe o servidor WebSocket que recebe os toques e move o mouse
        TouchWebSocketServer wsServer = new TouchWebSocketServer(WS_PORT, mouseController);
        wsServer.start();

        System.out.println();
        System.out.println("=======================================================");
        System.out.println(" Tudo pronto! No navegador do celular, acesse:");
        System.out.println(" http://" + descobrirIpLocal() + ":" + HTTP_PORT);
        System.out.println("=======================================================");
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
            java.util.List<java.net.NetworkInterface> candidatas = new java.util.ArrayList<>();
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                String nome = iface.getName().toLowerCase();

                if (!iface.isUp() || iface.isLoopback() || iface.isVirtual()) {
                    continue;
                }
                // Ignora interfaces criadas por Docker, libvirt, VPNs, veths etc.
                // (no Linux elas costumam começar com esses prefixos)
                if (nome.startsWith("docker") || nome.startsWith("br-") || nome.startsWith("veth")
                        || nome.startsWith("virbr") || nome.startsWith("vmnet") || nome.startsWith("tun")
                        || nome.startsWith("tap")) {
                    continue;
                }
                candidatas.add(iface);
            }

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
            System.err.println("Não consegui detectar o IP automaticamente: " + e.getMessage());
        }
        return "SEU_IP_AQUI (rode `hostname -I` e use o IP no formato 192.168.x.x)";
    }

    private static String buscarIPv4(java.util.List<java.net.NetworkInterface> interfaces,
                                       java.util.function.Predicate<String> filtroNome) throws java.net.SocketException {
        for (java.net.NetworkInterface iface : interfaces) {
            if (!filtroNome.test(iface.getName().toLowerCase())) continue;

            java.util.Enumeration<java.net.InetAddress> enderecos = iface.getInetAddresses();
            while (enderecos.hasMoreElements()) {
                java.net.InetAddress endereco = enderecos.nextElement();
                if (endereco instanceof java.net.Inet4Address && endereco.isSiteLocalAddress()) {
                    return endereco.getHostAddress();
                }
            }
        }
        return null;
    }
}
