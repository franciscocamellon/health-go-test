package com.camelloncase.healthgo.backend;

import com.camelloncase.healthgo.backend.dto.SignupRequest;
import com.camelloncase.healthgo.backend.entities.Doctor;
import com.camelloncase.healthgo.backend.entities.HospitalUser;
import com.camelloncase.healthgo.backend.entities.Visitor;
import com.camelloncase.healthgo.backend.service.UserService;
import com.camelloncase.healthgo.backend.utils.UserRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
public class DataLoader implements ApplicationRunner {

    @Autowired
    UserService userService;

    protected InputStream getClassLoaderResourceAsStream() {
        return getClass().getClassLoader().getResourceAsStream("mockupData.txt");
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        try {
            InputStream inputStream = getClassLoaderResourceAsStream();

            if (inputStream == null) {
                throw new FileNotFoundException("Arquivo mockupData.txt não encontrado em resources.");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileLine;

            HospitalUser medico = new Doctor();
            medico.setRole(UserRoles.DOCTOR);
            medico.setUsername("medico");
            medico.setPassword("medico");

            HospitalUser author = new Visitor();
            author.setRole(UserRoles.VISITOR);
            author.setUsername("visitante");
            author.setPassword("visitante");

            while ((fileLine = reader.readLine()) != null) {
                String[] fields = fileLine.split(";");

                SignupRequest signupRequest = new SignupRequest();

                switch (fields[0]) {
                    case "doctor":
                        signupRequest.setUsername(fields[1]);
                        signupRequest.setPassword(fields[2]);
                        signupRequest.setRole(UserRoles.DOCTOR);

                        userService.createUser(signupRequest);

                        break;

                    case "visitor":
                        signupRequest.setUsername(fields[1]);
                        signupRequest.setPassword(fields[2]);
                        signupRequest.setRole(UserRoles.VISITOR);

                        userService.createUser(signupRequest);

                        break;

                    default:
                        System.out.println("Registro inválido: " + fields[0]);
                        break;
                }
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("[ERRO] " + e.getMessage());
        }

        System.out.println("Dados adicionados com sucesso!");

    }
}
