package com.vinay.AuthService.Controller;

import java.util.List;

import com.vinay.AuthService.Dto.LoginRequest;
import com.vinay.AuthService.Dto.LoginResponse;
import com.vinay.AuthService.Dto.RegisterRequest;
import com.vinay.AuthService.Dto.UserResponse;
import com.vinay.AuthService.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }



    //implement login endpoint
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request){
        // delegate to service to authenticate and create tokens
        LoginResponse resp = userService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(resp);
//
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody java.util.Map<String, String> body){
        String refreshToken = body.get("refreshToken");
        // validate
        if(refreshToken == null) return ResponseEntity.badRequest().build();

        // find associated refresh token and user
        var tokenEntity = userService.getRefreshTokenService().findByToken(refreshToken);
        if(tokenEntity == null || !userService.getRefreshTokenService().validateRefreshToken(refreshToken)){
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        // build new access token
        var user = userService.getUserById(tokenEntity.getUserId());
        String accessToken = userService.getJwtUtil().generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, "Bearer"));
    }








    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}