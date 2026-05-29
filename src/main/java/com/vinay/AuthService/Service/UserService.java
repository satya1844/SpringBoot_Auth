package com.vinay.AuthService.Service;

import com.vinay.AuthService.Dto.RegisterRequest;
import com.vinay.AuthService.Dto.UserResponse;
import com.vinay.AuthService.Dto.LoginResponse;
import com.vinay.AuthService.Entity.User;
import com.vinay.AuthService.Repository.UserRepository;
import com.vinay.AuthService.Utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    //use dto to register user
     public UserResponse register(RegisterRequest request){
         //check if user already exists
            if(userRepository.findByEmail(request.getEmail()).isPresent()){
                throw new RuntimeException("User already exists with email: "+ request.getEmail());
            }
            //create user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setRole("USER");

            User savedUser = userRepository.save(user);
            //return the response
            return new UserResponse(savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole(),
                    savedUser.getCreatedAt());

     }

     //implement login method
    //nah implement the jwt token generation logic in the login method and return the token as response
     public LoginResponse login(String email, String password){
         User user = userRepository.findByEmail(email)
                 .orElseThrow(()-> new RuntimeException("User not found with email: "+ email));
         if(!passwordEncoder.matches(password, user.getPassword())){
             throw new RuntimeException("Invalid password");
         }

         // generate access token (short-lived)
         String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());

         // create refresh token and persist it
         var refreshToken = refreshTokenService.createRefreshToken(user.getId());

         return new LoginResponse(accessToken, refreshToken.getToken(), "Bearer");

     }


     public UserResponse getUserById(Long id){
         User user = userRepository.findById(id)
                 .orElseThrow(()-> new RuntimeException("User not found with id: "+ id));
         return new UserResponse(user.getId(),
                 user.getEmail(),
                 user.getRole(),
                 user.getCreatedAt());
     }

      // Expose JwtUtil to controller for token generation in refresh flow
      public JwtUtil getJwtUtil() {
          return jwtUtil;
      }

      // Expose RefreshTokenService for controller to look up tokens
      public RefreshTokenService getRefreshTokenService(){
          return refreshTokenService;
      }

     public List<UserResponse> getAllUsers(){
         List<User> users = userRepository.findAll();
         return users.stream()
                 .map(user -> new UserResponse(user.getId(),
                         user.getEmail(),
                         user.getRole(),
                         user.getCreatedAt()))
                 .collect(Collectors.toList());
     }

     public void deleteUser(Long id){
         User user = userRepository.findById(id)
                 .orElseThrow(()-> new RuntimeException("User not found with id: "+ id));
         userRepository.delete(user);
     }
}
