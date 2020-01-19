package com.castagnolofugale.videomanagementservice.service;

import com.castagnolofugale.videomanagementservice.model.User;
import com.castagnolofugale.videomanagementservice.repository.ReactiveUserRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Service
@Transactional
public class VMSUserService {

    @Autowired
    ReactiveUserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder passwordEncoder;

    public Mono<User> addUser(User user){
        user.setRoles(Collections.singletonList("USER"));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Flux<User> getAllUsers(){
        return userRepository.findAll();
    }

    public Mono<User> getByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public String deleteUser(ObjectId userId){
        userRepository.deleteById(userId);
        return "User with id: "+userId+" has been deleted";
    }
}
