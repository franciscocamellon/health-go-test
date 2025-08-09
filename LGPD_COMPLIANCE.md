# Conformidade com a LGPD

Este documento descreve as práticas adotadas pelo projeto HealthGo para aderência à Lei Geral de Proteção de Dados (Lei nº 13.709/2018).

---

## 1. Dados Pessoais Coletados

- Nome do paciente (pseudonimizado na exibição pública)
- Identificador único do paciente (`patientId`)
- Dados vitais (sem informação de geolocalização ou endereço)
- Informações de autenticação (usuários médicos)

---

## 2. Princípios Atendidos

- **Finalidade**: coleta e tratamento exclusivamente para monitoramento de saúde
- **Necessidade**: somente dados estritamente necessários
- **Adequação**: tratamento compatível com a finalidade declarada
- **Transparência**: API documentada e controlada por autenticação

---

## 3. Medidas Técnicas

- **Pseudonimização**: nomes de pacientes ocultos no frontend, salvo para usuários autorizados
- **Criptografia**: dados sensíveis armazenados criptografados no banco
- **Controle de Acesso**: autenticação JWT + roles (`ROLE_DOCTOR`, `ROLE_VISITOR`)
- **Auditoria**: logs de acesso e modificações, sem PII em texto claro
- **Segurança em Trânsito**: tráfego exclusivamente HTTPS

---

## 4. Retenção e Exclusão

- Dados armazenados pelo período necessário ao tratamento clínico
- Exclusão definitiva sob solicitação e autorização do responsável legal

---

## 5. Responsabilidades

- **Controlador**: Organização mantenedora do HealthGo
- **Operadores**: Desenvolvedores e administradores autorizados
- **Encarregado de Dados**: Nomeado conforme exigência legal
