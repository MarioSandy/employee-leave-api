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

import java.util.Map;

@Component
@RequiredArgsConstructor
public class InitialAdminSeeder implements CommandLineRunner {

    private static final String DEMO_USER_PASSWORD = "Demo123!";

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();
        seedDemoEmployeesAndUsers();

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
            .password(passwordEncoder.encode("Admin123!"))
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

    private void seedDemoEmployeesAndUsers() {
        seedDemoAccount(
            "EMP-0003", "Alice Johnson", "alice.johnson@example.com",
            "HR", "alice.johnson", UserRoleEnum.MANAGER
        );
        seedDemoAccount(
            "EMP-0004", "Robert Chen", "robert.chen@example.com",
            "FIN", "robert.chen", UserRoleEnum.EMPLOYEE
        );
        seedDemoAccount(
            "EMP-0005", "Emily Davis", "emily.davis@example.com",
            "MKT", "emily.davis", UserRoleEnum.EMPLOYEE
        );
        seedDemoAccount(
            "EMP-0006", "Daniel Wilson", "daniel.wilson@example.com",
            "OPS", "daniel.wilson", UserRoleEnum.MANAGER
        );
        seedDemoAccount(
            "EMP-0007", "Olivia Martinez", "olivia.martinez@example.com",
            "SAL", "olivia.martinez", UserRoleEnum.EMPLOYEE
        );
    }

    private void seedDemoAccount(
        String employeeCode,
        String fullName,
        String email,
        String departmentCode,
        String username,
        UserRoleEnum role
    ) {
        Department department = departmentRepository.findByCodeIgnoreCase(departmentCode)
            .orElseThrow();

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
            .orElseGet(() -> {
                Employee newEmployee = new Employee();
                newEmployee.setEmployeeCode(employeeCode);
                newEmployee.setFullName(fullName);
                newEmployee.setEmail(email);
                newEmployee.setDepartment(department);
                newEmployee.setEmploymentStatus(EmploymentStatusEnum.ACTIVE);
                return employeeRepository.save(newEmployee);
            });

        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        User user = User.builder()
            .name(fullName)
            .username(username)
            .password(passwordEncoder.encode(DEMO_USER_PASSWORD))
            .employeeId(employee.getId())
            .role(role)
            .build();

        userRepository.save(user);
    }
}
