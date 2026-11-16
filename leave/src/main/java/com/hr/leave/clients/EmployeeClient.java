package com.hr.leave.clients;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class EmployeeClient {

    private final RestClient restClient;

    public EmployeeClient(RestClient employeeRestClient) {
        this.restClient = employeeRestClient;
    }

    public record EmployeeInfo(UUID id, String fullName, String employmentStatus) {
        public boolean isActive() { return "ACTIVE".equals(employmentStatus); }
    }

    /** Returns null if the employee does not exist. Forwards the caller's JWT. */
    public EmployeeInfo findById(UUID id, String bearerToken) {
        try {
            return restClient.get()
                    .uri("/employees/{id}", id)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                    .retrieve()
                    .body(EmployeeInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpStatusCodeException e) {
            throw new ResponseStatusException(e.getStatusCode(),
                    "Employee service returned an error");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Employee service is unavailable");
        }
    }
}