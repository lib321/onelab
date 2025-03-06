package org.onelab.serviceImpl;

import org.onelab.model.Users;
import org.onelab.repository.UserRepo;
import org.onelab.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;

    public UserServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Users> getUsers() {
        return userRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Users> getUserById(int id) {
        return userRepo.findById(id);
    }

    @Override
    public Users save(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("Нельзя сохранять null!");
        }
        return userRepo.save(user);
    }

    @Override
    public void deleteById(int id) {
        userRepo.deleteById(id);
    }
}
