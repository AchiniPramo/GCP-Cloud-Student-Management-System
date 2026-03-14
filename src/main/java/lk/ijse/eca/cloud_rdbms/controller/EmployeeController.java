package lk.ijse.eca.cloud_rdbms.controller;

import lk.ijse.eca.cloud_rdbms.entity.Employee;
import lk.ijse.eca.cloud_rdbms.repository.EmployeeRepository;
import lk.ijse.eca.cloud_rdbms.service.GcsImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/employees")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired(required = false)
    private GcsImageService gcsImageService;

    /**
     * Get all employees
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();
        return ResponseEntity.ok(employees);
    }

    /**
     * Get employee by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        if (employee.isPresent()) {
            return ResponseEntity.ok(employee.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Employee not found with id: " + id));
        }
    }

    /**
     * Create new employee
     */
    @PostMapping
    public ResponseEntity<?> createEmployee(
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contact) {
        try {
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Employee name is required"));
            }
            if (address == null || address.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Employee address is required"));
            }
            if (contact == null || contact.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Employee contact is required"));
            }

            // Check if contact already exists
            if (employeeRepository.findByContact(contact).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new ErrorResponse("Contact number " + contact + " already exists"));
            }

            Employee employee = new Employee();
            employee.setName(name);
            employee.setAddress(address);
            employee.setContact(contact);

            // Upload image to GCS if provided
            if (image != null && !image.isEmpty()) {
                if (gcsImageService != null) {
                    try {
                        System.out.println("📸 Uploading image: " + image.getOriginalFilename());
                        String imageUrl = gcsImageService.uploadImage(image);
                        employee.setImageUrl(imageUrl);
                        System.out.println("✅ Image uploaded successfully: " + imageUrl);
                    } catch (Exception imageError) {
                        System.err.println("⚠️  Image upload failed, saving employee without image: " + imageError.getMessage());
                    }
                } else {
                    System.err.println("⚠️  GCS service unavailable, saving employee without image");
                }
            }

            Employee savedEmployee = employeeRepository.save(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to create employee: " + e.getMessage()));
        }
    }

    /**
     * Update employee
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String contact) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);

        if (!employeeOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Employee not found with id: " + id));
        }

        try {
            Employee employee = employeeOptional.get();

            if (name != null && !name.trim().isEmpty()) {
                employee.setName(name);
            }
            if (address != null && !address.trim().isEmpty()) {
                employee.setAddress(address);
            }
            if (contact != null && !contact.trim().isEmpty()) {
                // Check if new contact already exists (and it's different from current)
                if (!contact.equals(employee.getContact()) && 
                    employeeRepository.findByContact(contact).isPresent()) {
                    return ResponseEntity.badRequest()
                            .body(new ErrorResponse("Contact number " + contact + " already exists"));
                }
                employee.setContact(contact);
            }

            // Handle image upload
            if (image != null && !image.isEmpty()) {
                if (gcsImageService != null) {
                    try {
                        System.out.println("📸 Uploading image: " + image.getOriginalFilename());
                        // Delete old image if exists
                        if (employee.getImageUrl() != null) {
                            System.out.println("🗑️  Deleting old image: " + employee.getImageUrl());
                            gcsImageService.deleteImage(employee.getImageUrl());
                        }
                        // Upload new image
                        String imageUrl = gcsImageService.uploadImage(image);
                        employee.setImageUrl(imageUrl);
                        System.out.println("✅ Image uploaded successfully: " + imageUrl);
                    } catch (Exception imageError) {
                        System.err.println("⚠️  Image upload failed, keeping existing image: " + imageError.getMessage());
                    }
                } else {
                    System.err.println("⚠️  GCS service unavailable, keeping existing image");
                }
            }

            Employee updatedEmployee = employeeRepository.save(employee);
            return ResponseEntity.ok(updatedEmployee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to update employee: " + e.getMessage()));
        }
    }

    /**
     * Delete employee
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        Optional<Employee> employeeOptional = employeeRepository.findById(id);

        if (!employeeOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Employee not found with id: " + id));
        }

        try {
            Employee employee = employeeOptional.get();
            // Delete image from GCS if exists
            if (employee.getImageUrl() != null && gcsImageService != null) {
                gcsImageService.deleteImage(employee.getImageUrl());
            }
            employeeRepository.deleteById(id);
            return ResponseEntity.ok(new SuccessResponse("Employee deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse("Failed to delete employee: " + e.getMessage()));
        }
    }

    // Inner classes for response
    public static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class SuccessResponse {
        public String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
