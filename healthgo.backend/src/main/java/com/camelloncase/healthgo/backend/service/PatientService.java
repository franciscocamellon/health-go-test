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
    private final EncryptionService encryptionService;
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
    public Patient create(PatientRequest patientRequest) {

        if (patientRepository.findByPatientId(patientRequest.getPatientId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "patientId already exists");
        }

        String cpfNorm = patientRequest.getCpf() == null ? null : patientRequest.getCpf().replaceAll("\\D+", "");
        String fullNameEnc = encryptionService.encrypt(patientRequest.getFullName());
        String cpfEnc = encryptionService.encrypt(cpfNorm);

        Patient newPatient = Patient.builder()
                .patientId(patientRequest.getPatientId())
                .fullNameEnc(fullNameEnc)
                .cpfEnc(cpfEnc)
                // snapshot default
                .heartRate(0)
                .spo2(0.0)
                .systolicPressure(0)
                .diastolicPressure(0)
                .temperature(0.0)
                .respiratoryRate(0.0)
                .status("NORMAL")
                .timestamp(LocalDateTime.now())
                .build();

        return patientRepository.save(newPatient);
    }

    public void ingest(VitalIngestRequest ingestRequest) {
        Patient observedPatient = patientRepository.findByPatientId(ingestRequest.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "patient not found"));

        LocalDateTime ts = normalizeTimestamp(ingestRequest.getTimestamp());

        observedPatient.setHeartRate(ingestRequest.getHeartRate());
        observedPatient.setSpo2(ingestRequest.getSpo2());
        observedPatient.setSystolicPressure(ingestRequest.getSystolicPressure());
        observedPatient.setDiastolicPressure(ingestRequest.getDiastolicPressure());
        observedPatient.setTemperature(ingestRequest.getTemperature());
        observedPatient.setRespiratoryRate(ingestRequest.getRespiratoryRate());
        observedPatient.setStatus(ingestRequest.getStatus());
        observedPatient.setTimestamp(ts);

        patientRepository.save(observedPatient);

        var payload = new PatientUpdateEvent(observedPatient.getId(), observedPatient.getPatientId(), observedPatient.getHeartRate(),
                observedPatient.getSpo2(), observedPatient.getSystolicPressure(), observedPatient.getDiastolicPressure(),
                observedPatient.getTemperature(), observedPatient.getRespiratoryRate(), observedPatient.getStatus(), observedPatient.getTimestamp());

        broadcast(payload);
    }

    // ---------- Queries ----------
    public List<PatientResponse> list(Authentication authentication) {
        boolean doctor = hasRole(authentication, "DOCTOR");
        return patientRepository.findAll().stream()
                .map(p -> mapToResponse(p, doctor))
                .toList();
    }

    public PatientResponse getOne(Long id, Authentication authentication) {
        boolean doctor = hasRole(authentication, "DOCTOR");
        Patient p = patientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return mapToResponse(p, doctor);
    }

    // ---------- Mapping / helpers ----------
    private PatientResponse mapToResponse(Patient p, boolean doctor) {
        String fullName;
        try {
            fullName = encryptionService.decrypt(p.getFullNameEnc());
        } catch (Exception e) {
            fullName = "—";
        }
        String display = doctor ? fullName : p.initialsFromDecrypted(fullName);

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

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
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

