package com.pms.hospitalservice.grpc;

import com.pms.hospitalservice.dto.request.DoctorRequestDTO;
import com.pms.hospitalservice.service.factory.DoctorMapper;
import com.pms.hospitalservice.model.Department;
import com.pms.hospitalservice.model.Doctor;
import com.pms.hospitalservice.model.Hospital;
import com.pms.hospitalservice.repository.DepartmentRepository;
import com.pms.hospitalservice.repository.DoctorRepository;
import com.pms.hospitalservice.repository.HospitalRepository;
import com.pms.hospitalservice.service.util.IdGenerator;
import hospital.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@GrpcService
public class HospitalGrpcService extends HospitalServiceGrpc.HospitalServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(HospitalGrpcService.class);
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final HospitalRepository hospitalRepository;
    private final IdGenerator idGenerator;

    public HospitalGrpcService(DoctorRepository doctorRepository, DepartmentRepository departmentRepository, HospitalRepository hospitalRepository, IdGenerator idGenerator) {
        this.doctorRepository = doctorRepository;
        this.departmentRepository = departmentRepository;
        this.hospitalRepository = hospitalRepository;
        this.idGenerator = idGenerator;
    }

    @Override
    public void getHospitalById(HospitalByIdRequest request, StreamObserver<HospitalResponse> responseStreamObserver){
        log.info("gRPC getHospitalById: {}", request.getHospitalId());
        try{
            Hospital hospital = hospitalRepository.findByHospitalId(request.getHospitalId())
                    .orElseThrow(() -> {
                        log.warn("Hospital with id: {} not found", request.getHospitalId());
                        return Status.NOT_FOUND
                                .withDescription("Hospital with id: " + request.getHospitalId() + " not found")
                                .asRuntimeException();
                    });

            HospitalResponse response = HospitalResponse.newBuilder()
                    .setHospitalId(hospital.getHospitalId())
                    .setName(hospital.getName())
                    .setAddress(hospital.getAddress())
                    .setWebsite(hospital.getWebsite())
                    .setEmail(hospital.getEmail())
                    .setPhone(hospital.getPhone())
                    .build();

            responseStreamObserver.onNext(response);
            responseStreamObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getHospitalById : {}", e.getMessage());
            responseStreamObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getDoctorById(DoctorByIdRequest request, StreamObserver<DoctorResponse> responseObserver) {
        log.info("gRPC getDoctorById: {}", request.getDoctorId());
        try {
            Doctor doctor = doctorRepository.findByDoctorIdWithDepartment(request.getDoctorId())
                    .orElseThrow(() -> {
                        log.warn("Doctor not found: {}", request.getDoctorId());
                        return Status.NOT_FOUND.withDescription("Doctor not found: " + request.getDoctorId()).asRuntimeException();
                    });
            log.info("Doctor found: id={}, dept={}", doctor.getDoctorId(), doctor.getDepartment());
            String deptId = doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentId() : "NULL";
            log.info("DepartmentId: {}", deptId);
            DoctorResponse response = DoctorResponse.newBuilder()
                    .setDoctorId(doctor.getDoctorId())
                    .setName(doctor.getName() != null ? doctor.getName() : "")
                    .setEmail(doctor.getEmail() != null ? doctor.getEmail() : "")
                    .setDepartmentId(deptId)
                    .setPhone(doctor.getPhone() != null ? doctor.getPhone() : "")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getDoctorById: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getDepartmentById(DepartmentByIdRequest request, StreamObserver<DepartmentResponse> responseObserver) {
        log.info("gRPC getDepartmentById: {}", request.getDepartmentId());
        try {
            Department department = departmentRepository.findByDepartmentIdWithHospital(request.getDepartmentId())
                    .orElseThrow(() -> {
                        log.warn("Department not found: {}", request.getDepartmentId());
                        return Status.NOT_FOUND.withDescription("Department not found: " + request.getDepartmentId()).asRuntimeException();
                    });
            DepartmentResponse response = DepartmentResponse.newBuilder()
                    .setDepartmentId(department.getDepartmentId())
                    .setName(department.getName() != null ? department.getName() : "")
                    .setHospitalId(department.getHospital() != null ? department.getHospital().getHospitalId() : "")
                    .setHospitalName(department.getHospital() != null ? department.getHospital().getName() : "")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getDepartmentById: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    @Transactional
    public void createDoctor(CreateDoctorRequest request, StreamObserver<DoctorResponse> responseObserver) {
        log.info("gRPC createDoctor: {}", request.getName());
        try {
            Doctor doctor = DoctorMapper.createEntity(
                    new DoctorRequestDTO(
                            request.getName(), "", "", request.getDepartmentId(), request.getEmail(), request.getPhone()
                    )
            );
            doctor.setDoctorId(idGenerator.nextId("DOC", "doctor_seq"));
            if (!request.getDepartmentId().isEmpty()) {
                departmentRepository.findByDepartmentId(request.getDepartmentId())
                        .ifPresent(doctor::setDepartment);
            }
            doctor = doctorRepository.save(doctor);
            String deptId = doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentId() : "";
            DoctorResponse response = DoctorResponse.newBuilder()
                    .setDoctorId(doctor.getDoctorId())
                    .setName(doctor.getName() != null ? doctor.getName() : "")
                    .setDepartmentId(deptId)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in createDoctor: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllHospitals(Empty request, StreamObserver<HospitalListResponse> responseObserver) {
        log.info("gRPC getAllHospitals");
        try {
            List<com.pms.hospitalservice.model.Hospital> hospitals = hospitalRepository.findAll();
            HospitalListResponse.Builder listBuilder = HospitalListResponse.newBuilder();
            for (com.pms.hospitalservice.model.Hospital hospital : hospitals) {
                HospitalResponse protoHospital = HospitalResponse.newBuilder()
                        .setHospitalId(hospital.getHospitalId() != null ? hospital.getHospitalId() : "")
                        .setName(hospital.getName() != null ? hospital.getName() : "")
                        .setAddress(hospital.getAddress() != null ? hospital.getAddress() : "")
                        .build();
                listBuilder.addHospitals(protoHospital);
            }
            responseObserver.onNext(listBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getAllHospitals: {}", e.getMessage(), e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getAllDepartments(DepartmentByHospitalRequest request, StreamObserver<DepartmentListResponse> responseObserver) {
        log.info("gRPC getAllDepartments for hospital: {}", request.getHospitalId());
        try {
            List<Department> departments;
            if (request.getHospitalId() != null && !request.getHospitalId().isEmpty()) {
                        Hospital hospital = hospitalRepository.findByHospitalId(request.getHospitalId())
                        .orElseThrow(() -> {
                            log.warn("Hospital not found: {}", request.getHospitalId());
                            return Status.NOT_FOUND.withDescription("Hospital not found: " + request.getHospitalId()).asRuntimeException();
                        });
                departments = departmentRepository.findByHospital(hospital);
            } else {
                departments = departmentRepository.findAll();
            }
            DepartmentListResponse.Builder listBuilder = DepartmentListResponse.newBuilder();
            for (Department dept : departments) {
                listBuilder.addDepartments(DepartmentResponse.newBuilder()
                        .setDepartmentId(dept.getDepartmentId() != null ? dept.getDepartmentId() : "")
                        .setName(dept.getName() != null ? dept.getName() : "")
                        .setHospitalId(dept.getHospital() != null ? dept.getHospital().getHospitalId() : "")
                        .setHospitalName(dept.getHospital() != null ? dept.getHospital().getName() : "")
                        .build());
            }
            responseObserver.onNext(listBuilder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteDoctor(DeleteDoctorRequest request, StreamObserver<DeleteDoctorResponse> responseObserver) {
        log.info("gRPC deleteDoctor: {}", request.getDoctorId());
        try {
            Doctor doctor = doctorRepository.findByDoctorId(request.getDoctorId())
                    .orElseThrow(() -> {
                        log.warn("Doctor not found for deletion: {}", request.getDoctorId());
                        return Status.NOT_FOUND.withDescription("Doctor not found: " + request.getDoctorId()).asRuntimeException();
                    });
            doctorRepository.delete(doctor);
            DeleteDoctorResponse response = DeleteDoctorResponse.newBuilder()
                    .setSuccess(true)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in deleteDoctor: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
}
