plugins {
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    // Única dependência externa: uma libzinha pequena e famosa que já
    // implementa o protocolo WebSocket pra gente. Sem ela, teríamos que
    // implementar o "handshake" do WebSocket na mão (chato e fora do escopo).
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    // Classe com o método main() que vamos criar no próximo passo
    mainClass.set("com.mentoria.mouseremote.MainApp")
}
