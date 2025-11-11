package ir.bahman.academic_lms.service;

import ir.bahman.academic_lms.dto.ChangePasswordRequest;
import ir.bahman.academic_lms.model.Account;

import java.security.Principal;

public interface AccountService extends BaseService<Account, Long> {
    void changeRole(String username, String roleName);

    void activateAccount(Long id);

    void deactivateAccount(Long id);

    void changePassword(ChangePasswordRequest dto, String username);
}
