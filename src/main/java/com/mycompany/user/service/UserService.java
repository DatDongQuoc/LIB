package com.mycompany.user.service;

import com.mycompany.user.dto.UserDto;
import com.mycompany.user.dto.request.UserPageRequest;
import com.mycompany.user.dto.request.UserUpdateRequest;
import com.mycompany.user.dto.response.UserPageResponse;
import com.mycompany.user.entity.User;
import com.mycompany.user.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {
    List<UserDto> listAll();
    UserPageResponse getUserListByPage(UserPageRequest userPageRequest);
    Page<UserDto> listAll(UserPageRequest request, Pageable pageable);
    String save(UserDto userDTO);
    UserDto get(Long id) throws UserNotFoundException;
    User update(UserUpdateRequest userUpdateRequest) throws UserNotFoundException;
    void delete(Long id) throws UserNotFoundException;
    Integer uploadUser(MultipartFile file) throws IOException;
    void exportUsersToCSV(HttpServletResponse response) throws IOException;
}
