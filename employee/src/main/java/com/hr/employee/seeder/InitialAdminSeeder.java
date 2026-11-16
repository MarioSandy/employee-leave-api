package com.hr.employee.seeder;

import com.hr.employee.entities.Department;
import com.hr.employee.entities.Employee;
import com.hr.employee.entities.User;
import com.hr.employee.enums.EmploymentStatusEnum;
import com.hr.employee.enums.UserRoleEnum;
import com.hr.employee.repositories.DepartmentRepository;
import com.hr.employee.repositories.EmployeeRepository;
import com.hr.employee.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class InitialAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        Department department = departmentRepository.findByCodeIgnoreCase("IT")
            .orElseGet(() -> {
                Department newDepartment = new Department();
                newDepartment.setCode("IT");
                newDepartment.setName("Information Technology");
                return departmentRepository.save(newDepartment);
            });

        Employee employee = employeeRepository.findByEmployeeCode("EMP-0001")
            .orElseGet(() -> {
                Employee newEmployee = new Employee();
                newEmployee.setEmployeeCode("EMP-0001");
                newEmployee.setFullName("System Administrator");
                newEmployee.setEmail("admin@example.com");
                newEmployee.setDepartment(department);
                newEmployee.setEmploymentStatus(EmploymentStatusEnum.ACTIVE);
                return employeeRepository.save(newEmployee);
            });

        User admin = User.builder()
            .name("System Administrator")
            .username("admin")
            .password(passwordEncoder.encode("Admin123!"))
            .employeeId(employee.getId())
            .role(UserRoleEnum.ADMIN)
            .build();

        userRepository.save(admin);
    }
}
