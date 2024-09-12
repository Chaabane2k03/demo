package com.example.demo.appuser;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.registration.token.ConfirmationToken;
import com.example.demo.registration.token.ConfirmationTokenService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;


@Service
@AllArgsConstructor
@Transactional
public class AppUserService implements UserDetailsService{

    private final static String USER_NOT_FOUND_MSG =
            "user with email %s not found";
    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;
    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        return appUserRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                String.format(USER_NOT_FOUND_MSG, email)));
    }


    public String signUpUser(AppUser user) {
        boolean userExists = appUserRepository
                .findByEmail(user.getEmail())
                .isPresent();
        if (userExists) {
            throw new IllegalStateException("email already taken");
        }
        
        String hashedPassword = bCryptPasswordEncoder
                .encode(user.getPassword());
        user.setPassword(hashedPassword);
        
        appUserRepository.save(user);

        String token = UUID.randomUUID().toString();
        
        ConfirmationToken confirmationToken = new ConfirmationToken(
            token,
            LocalDateTime.now(),
            LocalDateTime.now().plusMinutes(15),
            user
        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        return "user created";
    }


    public int enableAppUser(String email) {
        return appUserRepository.enableAppUser(email);
    }


}
