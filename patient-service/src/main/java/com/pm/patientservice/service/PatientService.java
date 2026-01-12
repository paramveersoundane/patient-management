package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private final PatientRepository patientRepository;  // immutability, accidental rea ,dependency injection
    // grpc code
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;


    public PatientService(PatientRepository patientRepository,
                          BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer){
        this.patientRepository=patientRepository;
        this.billingServiceGrpcClient=billingServiceGrpcClient;
        this.kafkaProducer=kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients = patientRepository.findAll();
        List<PatientResponseDTO> patientResponseDTOs=
                patients.stream().map(PatientMapper::toDTO).toList();//patients.stream().map(patient-> PatientMapper.getDTO(patient)).toList();

        return patientResponseDTOs;
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            // Creating custom exception
            throw new EmailAlreadyExistException("A patient with this email " +patientRequestDTO.getEmail()+" already exists");
        }
        Patient patient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));
        // Creating Billing account for new patient
        billingServiceGrpcClient.createBillingAccount(patient.getId().toString(),patient.getName(),patient.getEmail());
        // Send Patient created event to kafka
        kafkaProducer.sendEvent(patient);
        return PatientMapper.toDTO(patient);

    }

    public PatientResponseDTO updatePatient(UUID id,  PatientRequestDTO patientRequestDTO){

        Patient patient = patientRepository.findById(id).orElseThrow(
                ()->new PatientNotFoundException("Patient not found with ID: " + id )
        );

        // Email already exist check
        if(patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)){
            // Creating custom exception
            throw new EmailAlreadyExistException("A patient with this email " +patientRequestDTO.getEmail()+" already exists");
        }


        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatepatient = patientRepository.save(patient);

        return PatientMapper.toDTO(updatepatient);


    }

    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }
}
