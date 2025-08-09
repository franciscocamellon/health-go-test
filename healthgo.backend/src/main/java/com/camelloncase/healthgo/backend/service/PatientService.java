package com.camelloncase.healthgo.backend.service;

import com.camelloncase.healthgo.backend.dto.PatientRequest;
import com.camelloncase.healthgo.backend.dto.PatientResponse;
import com.camelloncase.healthgo.backend.dto.PatientUpdateEvent;
import com.camelloncase.healthgo.backend.dto.VitalIngestRequest;
import com.camelloncase.healthgo.backend.entities.Patient;
import com.camelloncase.healthgo.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        // 30 minutos
        SseEmitter em = new SseEmitter(30 * 60_000L);
        emitters.add(em);
        em.onCompletion(() -> emitters.remove(em));
        em.onTimeout(() -> emitters.remove(em));
        // heartbeat inicial (evita 503 em alguns proxies)
        try { em.send(SseEmitter.event().name("heartbeat").data("ok")); } catch (IOException ignored) {}
        return em;
    }

    private void broadcast(Object payload) {
        for (SseEmitter em : List.copyOf(emitters)) {
            try {
                em.send(SseEmitter.event()
                        .name("patient-update")
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException e) {
                emitters.remove(em);
            }
        }
    }

    // ---------- Commands ----------
    public Patient create(PatientRequest req) {
        if (patientRepository.findByPatientId(req.getPatientId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "patientId already exists");
        }
        Patient p = Patient.builder()
                .patientId(req.getPatientId())
                .fullName(req.getFullName())
                .cpf(req.getCpf())
                // snapshot default
                .heartRate(0).spo2(0)
                .systolicPressure(0).diastolicPressure(0)
                .temperature(0.0).respiratoryRate(0)
                .status("NORMAL")
                .timestamp(LocalDateTime.now())
                .build();
        return patientRepository.save(p);
    }

    public void ingest(VitalIngestRequest req) {
        Patient p = patientRepository.findByPatientId(req.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "patient not found"));

        LocalDateTime ts = normalizeTimestamp(req.getTimestamp());

        p.setHeartRate(req.getHeartRate());
        p.setSpo2(req.getSpo2());
        p.setSystolicPressure(req.getSystolicPressure());
        p.setDiastolicPressure(req.getDiastolicPressure());
        p.setTemperature(req.getTemperature());
        p.setRespiratoryRate(req.getRespiratoryRate());
        p.setStatus(req.getStatus());
        p.setTimestamp(ts);

        patientRepository.save(p);

        var payload = new PatientUpdateEvent(p.getId(), p.getPatientId(), p.getHeartRate(),
                p.getSpo2(), p.getSystolicPressure(), p.getDiastolicPressure(),
                p.getTemperature(), p.getRespiratoryRate(), p.getStatus(), p.getTimestamp());

        broadcast(payload);
    }

    // ---------- Queries ----------
    public List<PatientResponse> list(Authentication auth) {
        boolean doctor = hasRole(auth, "DOCTOR");
        return patientRepository.findAll().stream()
                .map(p -> toResponse(p, doctor))
                .toList();
    }

    public PatientResponse getOne(Long id, Authentication auth) {
        boolean doctor = hasRole(auth, "DOCTOR");
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return toResponse(p, doctor);
    }

    // ---------- Mapping / helpers ----------
    private PatientResponse toResponse(Patient p, boolean doctor) {
        String display = doctor ? p.getFullName() : p.getInitials();
        return PatientResponse.builder()
                .id(p.getId())
                .patientId(p.getPatientId())
                .displayName(display)
                .heartRate(p.getHeartRate())
                .spo2(p.getSpo2())
                .systolicPressure(p.getSystolicPressure())
                .diastolicPressure(p.getDiastolicPressure())
                .temperature(p.getTemperature())
                .respiratoryRate(p.getRespiratoryRate())
                .status(p.getStatus())
                .timestamp(p.getTimestamp())
                .build();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private LocalDateTime normalizeTimestamp(String raw) {
        // Aceita ISO-8601 direto
        try { return LocalDateTime.parse(raw); } catch (Exception ignore) {}

        // Aceita "HH:mm:ss.SS" do CSV (e usa a data de hoje)
        try {
            var base = LocalDate.now();
            var fmt = DateTimeFormatter.ofPattern("HH:mm:ss[.SSS][.SS]");
            LocalTime lt = LocalTime.parse(raw.replace(',', '.'), fmt);
            return LocalDateTime.of(base, lt);
        } catch (Exception e) {
            // fallback: agora
            return LocalDateTime.now();
        }
    }
}

