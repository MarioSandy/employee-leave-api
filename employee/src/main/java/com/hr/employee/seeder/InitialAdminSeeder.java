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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class InitialAdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();

        if (userRepository.findByUsername("admin").isPresent()) {
            return;
        }

        Department department = departmentRepository.findByCodeIgnoreCase("IT")
            .orElseThrow();

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
            .password(passwordEncoder.encode(adminPassword))
            .employeeId(employee.getId())
            .role(UserRoleEnum.ADMIN)
            .build();

        userRepository.save(admin);
    }

    private void seedDepartments() {
        Map<String, String> departments = Map.of(
            "IT", "Information Technology",
            "HR", "Human Resources",
            "FIN", "Finance",
            "MKT", "Marketing",
            "OPS", "Operations",
            "SAL", "Sales"
        );

        departments.forEach((code, name) ->
            departmentRepository.findByCodeIgnoreCase(code)
                .orElseGet(() -> {
                    Department department = new Department();
                    department.setCode(code);
                    department.setName(name);
                    return departmentRepository.save(department);
                })
        );
    }
}
