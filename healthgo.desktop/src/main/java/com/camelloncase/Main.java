package com.camelloncase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;


public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        Properties p = new Properties();
        try (var in = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in == null) throw new IllegalStateException("application.properties not found");
            p.load(in);
        }

        String baseUrl = p.getProperty("api.baseUrl");
        String loginUrl = baseUrl + p.getProperty("api.loginPath", "/auth/login");
        String ingestUrl = baseUrl + p.getProperty("api.ingestPath", "/api/patients/ingest");
        String username = p.getProperty("auth.username");
        String password = p.getProperty("auth.password");
        long intervalMs = Long.parseLong(p.getProperty("ingest.intervalMs", "250"));
        boolean failFast = Boolean.parseBoolean(p.getProperty("failFast", "false"));

        String[] names = Arrays.stream(p.getProperty("data.files","").split(","))
                .map(String::trim).filter(s -> !s.isBlank()).toArray(String[]::new);
        if (names.length == 0) {
            System.err.println("Configure data.files no application.properties (ex: dados_pac001.csv,...)");
            return;
        }
        log.info("loginUrl={} ingestUrl={} files={}", loginUrl, ingestUrl, Arrays.toString(names));


        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        String jwt = login(http, loginUrl, username, password);
        log.info("Login ok; jwt: {}", jwt);

        ExecutorService pool = Executors.newFixedThreadPool(names.length);
        List<Callable<Void>> tasks = new ArrayList<>();

        for (String name : names) {
            tasks.add(() -> {
                String resourcePath = "/data/" + name; // dentro de resources/data
                try (InputStream is = Main.class.getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        String msg = "Resource não encontrado: " + resourcePath;
                        if (failFast) throw new IllegalStateException(msg);
                        log.error(msg);
                        return null;
                    }
                    try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        Map<String,String> row;
                        long line = 0;
                        while ((row = reader.readMap()) != null) {
                            var payload = Map.of(
                                    "timestamp", row.getOrDefault("timestamp", ""),
                                    "patientId", row.getOrDefault("paciente_id", ""),
                                    "heartRate", safeInt(row.get("hr")),
                                    "spo2", safeD(row.get("spo2")),
                                    "systolicPressure", safeInt(row.get("pressao_sys")),
                                    "diastolicPressure", safeInt(row.get("pressao_dia")),
                                    "temperature", safeD(row.get("temp")),
                                    "respiratoryRate", safeD(row.get("resp_freq")),
                                    "status", row.getOrDefault("status", "NORMAL")
                            );

//                            log.info(payload.toString());

                            HttpRequest req = HttpRequest.newBuilder()
                                    .uri(URI.create(ingestUrl))
                                    .timeout(Duration.ofSeconds(5))
                                    .header("Content-Type", "application/json")
                                    .header("Authorization", "Bearer " + jwt)
                                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(payload)))
                                    .build();

                            try {
                                HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
                                if (resp.statusCode() / 100 != 2) {
                                    var msg = "[" + name + "] linha " + (line+1) + " -> HTTP " + resp.statusCode() + " body=" + resp.body();
                                    if (failFast) throw new RuntimeException(msg);
                                    log.warn(msg);
                                } else if ((line % 50) == 0) {
                                    log.info("[{}] enviado linha {}", name, line+1);
                                }
                            } catch (Exception e) {
                                var msg = "[" + name + "] erro linha " + (line+1) + ": " + e.getMessage();
                                if (failFast) throw e;
                                log.error(msg);
                            }

                            Thread.sleep(intervalMs);
                            line++;
                        }
                        log.info("[{}] concluído", name);
                    }
                }
                return null;
            });
        }

        pool.invokeAll(tasks);
        pool.shutdown();
        log.info("Todos os CSVs concluídos.");
    }

    private static String login(HttpClient http, String url, String user, String pass) throws Exception {
        var body = MAPPER.writeValueAsString(Map.of("username", user, "password", pass));
        var req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2)
            throw new IllegalStateException("Login falhou: HTTP " + resp.statusCode() + " body=" + resp.body());
        Map<?,?> map = MAPPER.readValue(resp.body(), Map.class);
        Object jwt = map.get("jwt");
        if (jwt == null) throw new IllegalStateException("Resposta de login sem 'jwt'");
        return jwt.toString();
    }

    private static Integer safeInt(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            s = s.trim().replace(',', '.');
            return (int) Math.round(Double.parseDouble(s));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Double safeD(String s) {
        try {
            if (s == null || s.isBlank()) return null;
            s = s.trim().replace(',', '.');
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}