package ir.bahman.academic_lms.controller;

import ir.bahman.academic_lms.dto.ChangePasswordRequest;
import ir.bahman.academic_lms.dto.ChangeRoleRequest;
import ir.bahman.academic_lms.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PutMapping("/activate/{id}")
    public ResponseEntity<Void> activateAccount(@PathVariable Long id) {
        accountService.activateAccount(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
        accountService.deactivateAccount(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-role")
    public ResponseEntity<Void> changeRole(@Valid @RequestBody ChangeRoleRequest request, Principal principal) {
        accountService.changeRole(principal.getName(), request.getRole());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        accountService.changePassword(request, principal.getName());
        return ResponseEntity.ok().build();
    }
}
