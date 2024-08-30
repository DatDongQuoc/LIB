package com.mycompany.user.service.impl;

import com.mycompany.user.dto.UserCsvRepresentationDto;
import com.mycompany.user.dto.UserDto;
import com.mycompany.user.dto.request.UserPageRequest;
import com.mycompany.user.dto.request.UserUpdateRequest;
import com.mycompany.user.dto.response.UserPageResponse;
import com.mycompany.user.entity.Role;
import com.mycompany.user.entity.User;
import com.mycompany.user.entity.UserCsvRepresentation;
import com.mycompany.user.exception.CustomException;
import com.mycompany.user.exception.UserNotFoundException;
import com.mycompany.user.repository.RoleRepository;
import com.mycompany.user.repository.UserRepository;
import com.mycompany.user.service.UserService;
import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public List<UserDto> listAll() {
        List<User> users = repo.findAll();
        return users.stream()
                .map(user -> {
                    UserDto dto = convertToDTO(user);
                    dto.setRoleNames(user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserPageResponse getUserListByPage(UserPageRequest userPageRequest) {
        int currentPage = userPageRequest.getPageNumber() < 1 ? 1 : userPageRequest.getPageNumber();
        int pageSize = userPageRequest.getPageSize() < 1 ? 5 : userPageRequest.getPageSize();

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);
        Page<UserDto> page = listAll(userPageRequest, pageable);
        long totalItems = page.getTotalElements();
        int totalPages = page.getTotalPages();

        return new UserPageResponse(page.getContent(), currentPage, totalItems, totalPages);
    }

    @Override
    public Page<UserDto> listAll(UserPageRequest request, Pageable pageable) {
        Page<User> usersPage = repo.findByCriteria(request, pageable);

        // Convert each User entity to UserDto and manually populate roleNames
        List<UserDto> usersDTOs = usersPage.getContent().stream()
                .map(user -> {
                    UserDto dto = convertToDTO(user);
                    dto.setRoleNames(user.getRoles().stream()
                            .map(Role::getName)
                            .collect(Collectors.toSet()));
                    return dto;
                })
                .collect(Collectors.toList());

        // Return a Page object with UserDto
        return new PageImpl<>(usersDTOs, pageable, usersPage.getTotalElements());
    }

    @Transactional
    @Override
    public String save(UserDto userDTO) {
        // Check if email already exists
        if (repo.existsByEmail(userDTO.getEmail())) {
            throw new CustomException("DUPLICATE_EMAIL", "Email already exists.");
        }
        User user = convertToEntity(userDTO);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Handle roles
        Set<Role> roles = new HashSet<>();
        if (userDTO.getRoleNames() != null) {
            for (String roleName : userDTO.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new CustomException("ROLE_NOT_FOUND", "Role " + roleName + " not found."));
                roles.add(role);
            }
        }
        user.setRoles(roles);
        repo.save(user);
        return "SUCCESS";
    }

    @Override
    public UserDto get(Long id) throws UserNotFoundException {
        Optional<User> result = repo.findById(Long.valueOf(id));
        if (result.isPresent()) {
            return convertToDTO(result.get());
        }
        throw new UserNotFoundException("Could not find any user with ID " + id);
    }

    @Transactional
    @Override
    public User update(UserUpdateRequest userUpdateRequest) throws UserNotFoundException, CustomException {
        User currentUser = getCurrentUser();  // Get the authenticated user

        // Ensure that the current user is either updating their own data or has the admin role
        if (!userUpdateRequest.getId().equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new CustomException("ACCESS_DENIED", "You do not have permission to update this user.");
        }

        User existingUser = repo.findById(Long.valueOf(userUpdateRequest.getId()))
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userUpdateRequest.getId() + " not found"));


        // Handle email update
        String newEmail = userUpdateRequest.getEmail();
        if (newEmail != null) {
            if (newEmail.trim().isEmpty()) {
                newEmail = existingUser.getEmail();
            } else if (!newEmail.equals(existingUser.getEmail()) && repo.existsByEmail(newEmail)) {
                throw new CustomException("DUPLICATE_EMAIL", "Email already exists.");
            }
            existingUser.setEmail(newEmail);
        }

        // Handle firstName update
        String newFirstName = userUpdateRequest.getFirstName();
        if (newFirstName != null) {
            if (newFirstName.trim().isEmpty()) {
                newFirstName = existingUser.getFirstName();
            } else if (newFirstName.length() < 2) {
                throw new CustomException("INVALID_FIRSTNAME", "First name must be at least 2 characters long.");
            }
            existingUser.setFirstName(newFirstName);
        }

        // Handle lastName update
        String newLastName = userUpdateRequest.getLastName();
        if (newLastName != null) {
            if (newLastName.trim().isEmpty()) {
                newLastName = existingUser.getLastName();
            } else if (newLastName.length() < 2) {
                throw new CustomException("INVALID_LASTNAME", "Last name must be at least 2 characters long.");
            }
            existingUser.setLastName(newLastName);
        }

        // Handle password update
        String newPassword = userUpdateRequest.getPassword();
        if (newPassword != null) {
            if (newPassword.trim().isEmpty()) {
                newPassword = existingUser.getPassword();
            } else if (newPassword.length() < 6) {
                throw new CustomException("INVALID_PASSWORD", "Password must be at least 6 characters long.");
            } else {
                PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
                newPassword = passwordEncoder.encode(newPassword);
            }
            existingUser.setPassword(newPassword);
        }

        // Handle roles update
        Set<Role> roles = new HashSet<>();
        if (userUpdateRequest.getRoleNames() != null) {
            for (String roleName : userUpdateRequest.getRoleNames()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new CustomException("ROLE_NOT_FOUND", "Role " + roleName + " not found."));
                roles.add(role);
            }
        }
        existingUser.setRoles(roles);


        repo.save(existingUser);
        return existingUser;
    }

    @Transactional
    @Override
    public void delete(Long id) throws UserNotFoundException, CustomException {
        User currentUser = getCurrentUser();  // Get the authenticated user

        // Ensure that the current user is either deleting their own data or has the admin role
        if (!id.equals(currentUser.getId()) && !isAdmin(currentUser)) {
            throw new CustomException("ACCESS_DENIED", "You do not have permission to delete this user.");
        }

        Long count = repo.countById(Math.toIntExact(id));
        if (count == null || count == 0) {
            throw new UserNotFoundException("Could not find any users with ID " + id);
        }

        repo.deleteById(id);
    }

    // Helper methods
    private boolean isAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> Role.ADMIN.equals(role.getName()));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Transactional
    @Override
    public Integer uploadUser(MultipartFile file) throws IOException {
        List<UserCsvRepresentationDto> invalidEntries = new ArrayList<>();
        List<UserCsvRepresentationDto> allEntries = new ArrayList<>();
        Set<User> users = parseCsv(file, invalidEntries, allEntries);

        if (!invalidEntries.isEmpty()) {
            writeEntriesToCsv(file, allEntries);
            System.out.println("Invalid entries updated in original file.");
            return 0;
        }

        // Encode passwords before saving users
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        users.forEach(user -> user.setPassword(passwordEncoder.encode(user.getPassword())));

        repo.saveAll(users);
        return users.size();
    }

    private Set<User> parseCsv(MultipartFile file, List<UserCsvRepresentationDto> invalidEntries, List<UserCsvRepresentationDto> allEntries) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            HeaderColumnNameMappingStrategy<UserCsvRepresentation> strategy = new HeaderColumnNameMappingStrategy<>();
            strategy.setType(UserCsvRepresentation.class);
            CsvToBean<UserCsvRepresentation> csvToBean = new CsvToBeanBuilder<UserCsvRepresentation>(reader)
                    .withMappingStrategy(strategy)
                    .withIgnoreEmptyLine(true)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            // Set to track emails within the CSV file to check for duplicates
            Set<String> emailSet = new HashSet<>();
            // Set to track emails already in the database
            Set<String> dbEmails = repo.findAll().stream().map(User::getEmail).collect(Collectors.toSet());

            csvToBean.parse().forEach(entity -> {
                UserCsvRepresentationDto dto = UserCsvRepresentationDto.fromEntity(entity);
                allEntries.add(dto);

                // Check for null or empty email
                if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                    dto.setReason("Email is null or empty");
                    invalidEntries.add(dto);
                    // Check for duplicate email in the database
                } else if (dbEmails.contains(dto.getEmail())) {
                    dto.setReason("Duplicate email in database");
                    invalidEntries.add(dto);
                    // Check for duplicate email in the CSV file
                } else if (!emailSet.add(dto.getEmail())) {
                    dto.setReason("Duplicate email in file");
                    invalidEntries.add(dto);
                }
            });
            // Collect valid users
            return allEntries.stream()
                    .filter(dto -> dto.getReason() == null)
                    .map(dto -> User.builder()
                            .email(dto.getEmail())
                            .firstName(dto.getFname())
                            .lastName(dto.getLname())
                            .password(dto.getPassword())
                            .build())
                    .collect(Collectors.toSet());
        }
    }

    private void writeEntriesToCsv(MultipartFile file, List<UserCsvRepresentationDto> allEntries) throws IOException {
        Path tempFile = Files.createTempFile(null, ".csv");

        try (Writer writer = Files.newBufferedWriter(tempFile)) {
            StatefulBeanToCsv<UserCsvRepresentationDto> beanToCsv = new StatefulBeanToCsvBuilder<UserCsvRepresentationDto>(writer)
                    .withApplyQuotesToAll(false)
                    .build();
            beanToCsv.write(allEntries);
        } catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            throw new RuntimeException(e);
        }

        // Replace the original file with the temporary file
        Files.move(tempFile, Paths.get(file.getOriginalFilename()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    @Transactional
    @Override
    public void exportUsersToCSV(HttpServletResponse response) throws IOException {
        // Set the content type and attachment header
        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);

        List<UserDto> listUsers = listAll(); // Fetch user data

        // Use try-with-resources to ensure proper resource management
        try (ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE)) {
            String[] csvHeader = {"User ID", "E-mail", "First Name", "Last Name", "Password"};
            String[] nameMapping = {"id", "email", "firstName", "lastName", "password"};

            csvWriter.writeHeader(csvHeader);

            for (UserDto userDTO : listUsers) {
                csvWriter.write(userDTO, nameMapping);
            }
        } catch (Exception e) {
            // Handle exceptions related to CSV writing
            throw new IOException("Error occurred while exporting users to CSV", e);
        }
    }

    private UserDto convertToDTO(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    private User convertToEntity(UserDto userDTO) {
        return modelMapper.map(userDTO, User.class);
    }
}
