package com.app.zware.Controllers;

import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Repositories.UserRepository;
import com.app.zware.Service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserAvatarController {


  private static final String UPLOAD_DIR = "uploads/avatars/";
  @Autowired
  UserService userService;
  @Autowired
  private UserRepository userRepository;

  @PostMapping("/{userId}/avatars")
  public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file,
      @PathVariable("userId") int userId, HttpServletRequest request) {

    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization : Admin && user avatar do

    User userRequestMaker = userService.getRequestMaker(request);
    if (!userRequestMaker.getRole().equals("admin") && !userRequestMaker.getId().equals(userId)) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }
    User targetUser = userService.getById(userId);

    if (targetUser == null) {
      customResponse.setAll(false, "User not found", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);

    }
    if (userRequestMaker.getRole().equals("admin") && targetUser.getRole().equals("admin")
        && !userRequestMaker.getId().equals(userId)) {
      customResponse.setAll(false, "Admin cannot modifu other admins", null);
      return new ResponseEntity<>(customResponse, HttpStatus.FORBIDDEN);
    }

    if (file.isEmpty()) {
      customResponse.setAll(false, "Not found file", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }
//    if (!userRepository.existsById(userId)) {
//      customResponse.setAll(false, "Not found Id", null);
//      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
//    }

    try {
      // Tạo thư mục nếu chưa tồn tại
      Path uploadPath = Paths.get(UPLOAD_DIR);
      if (!Files.exists(uploadPath)) {
        Files.createDirectories(uploadPath);
      }

      // Delete existing avatar file if present
      String existingAvatar = targetUser.getAvatar();
      if (existingAvatar != null && !existingAvatar.isEmpty()) {
        File existingFile = new File(UPLOAD_DIR + existingAvatar);
        if (existingFile.exists()) {
          existingFile.delete();
        }
      }

      // Lưu file vào thư mục avatars
      String originalFileName = file.getOriginalFilename();
      String fileExtension = originalFileName.substring(originalFileName.lastIndexOf('.'));
      String newFileName =
          LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + fileExtension;
      Path filePath = uploadPath.resolve(newFileName);
      Files.copy(file.getInputStream(), filePath);

      // Lưu đường dẫn avatar vào cơ sở dữ liệu
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new RuntimeException("User not found"));
      user.setAvatar(newFileName);
      userRepository.save(user);

      customResponse.setAll(true, "You successfully uploaded " + newFileName, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    } catch (IOException e) {
      e.printStackTrace();
      customResponse.setAll(false, "Failed to upload", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/{userId}/avatars")
  public ResponseEntity<?> getAvatar(@PathVariable Integer userId) {
    //response
    CustomResponse customResponse = new CustomResponse();

    User user = userRepository.findById(userId).orElse(null);

    if (user == null) {
      customResponse.setAll(false, "User not found", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }
    String avatarFileName = user.getAvatar();
    if (avatarFileName == null || avatarFileName.isEmpty()) {
      customResponse.setAll(false, "User does not have an avatar", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }

    File file = new File(UPLOAD_DIR + user.getAvatar());

    if (file.exists()) {
      Resource resource = new FileSystemResource(file);

      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
          .body(resource);
    } else {
      customResponse.setAll(false, "Avatar file not found", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);

    }
  }

  @DeleteMapping("/{userId}/avatars")
  public ResponseEntity<?> deleteAvatar(@PathVariable Integer userId, HttpServletRequest request) {
    //Authorization : Admin && user avatar do

    //response
    CustomResponse customResponse = new CustomResponse();

    User userRequestMaker = userService.getRequestMaker(request);
    if (!userRequestMaker.getRole().equals("admin") && !userRequestMaker.getId().equals(userId)) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    User user = userRepository.findById(userId).orElse(null);

    if (user == null) {
      customResponse.setAll(false, "User not found", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }

    if (userRequestMaker.getRole().equals("admin") && user.getRole().equals("admin")) {
      customResponse.setAll(false, "Admins cannot modify other admins", null);
      return new ResponseEntity<>(customResponse, HttpStatus.FORBIDDEN);
    }

    String avatarFileName = user.getAvatar();
    if (avatarFileName != null && !avatarFileName.isEmpty()) {
      File file = new File(UPLOAD_DIR + avatarFileName);
      if (file.exists()) {
        if (file.delete()) {
          user.setAvatar(null);
          userRepository.save(user);
          customResponse.setAll(true, "Avatar deleted successfully", null);
          return new ResponseEntity<>(customResponse, HttpStatus.OK);
        } else {
          customResponse.setAll(false, "Failed to delete avatar", null);
          return new ResponseEntity<>(customResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
      } else {
        customResponse.setAll(false, "Avatar file not found", null);
        return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
      }
    } else {
      customResponse.setAll(false, "User has no avatar", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }
  }
}



