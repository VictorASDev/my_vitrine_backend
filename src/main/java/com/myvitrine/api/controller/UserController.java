package com.myvitrine.api.controller;

import com.myvitrine.api.dto.request.UserRequest;
import com.myvitrine.api.dto.response.UserResponse;
import com.myvitrine.api.security.CurrentUser;
import com.myvitrine.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Cadastro e identidade dos usuarios da plataforma")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Cadastra um novo usuario")
    @ApiResponse(responseCode = "201", description = "Usuario criado com sucesso")
    @ApiResponse(responseCode = "409", description = "E-mail ja cadastrado")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuarios")
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um usuario pelo id")
    @ApiResponse(responseCode = "404", description = "Usuario nao encontrado")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza os dados de um usuario")
    @ApiResponse(responseCode = "403", description = "Tentativa de editar outro usuario")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserRequest request,
                                               HttpServletRequest httpRequest) {
        CurrentUser.requireOwner(httpRequest, id);
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um usuario")
    @ApiResponse(responseCode = "403", description = "Tentativa de remover outro usuario")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        CurrentUser.requireOwner(httpRequest, id);
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
