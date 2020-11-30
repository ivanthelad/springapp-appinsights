package com.microsoft.azure.springdemo.insights.web;

import com.microsoft.azure.springdemo.insights.repo.User;
import com.microsoft.azure.springdemo.insights.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;

@RestController
@Service
public class UserController {


    @GetMapping("/api/v1/users")
    public ResponseEntity<Iterable<User>> findAll() {
        //    this.messagingGateway.send("executing find");
        return ResponseEntity.ok(repository.findAll());
    }

    @PostMapping("/api/v1/users")
    public ResponseEntity create(@Valid @RequestBody User user) {
        Long id = randomLong();
        user.setUuid(UUID.randomUUID().toString());
        User i = repository.save(user);
        i.setStatus("OK");
        return ResponseEntity.ok(i);
    }
    public static long randomLong() {
        long leftLimit = 1L;
        long rightLimit = 9999999999999L;
       return leftLimit + (long) (Math.random() * (leftLimit - rightLimit));
    }

    @GetMapping("/api/v1/users/{id}")
    public @ResponseBody
    Optional<User> getUser(@PathVariable Long id) {
        return repository.findById(id);
    }

    @Autowired
    private UserRepository repository;

    @DeleteMapping("/api/v1/users/{id}")
    public @ResponseBody
    String deleteUser(@PathVariable Long id) {
        Optional<User> user = repository.findById(id);

        repository.delete(user.get());

        return "Deleted " + id;
    }

    @GetMapping(path = "/api/v1/users/page", produces = MediaType.APPLICATION_JSON_VALUE)
    Page<User> loadCharactersPage(Pageable pageable, PagedResourcesAssembler assembler) {
        return repository.findAll(pageable);
    }

}
