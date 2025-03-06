package org.onelab;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onelab.model.Users;
import org.onelab.repository.UserRepo;
import org.onelab.serviceImpl.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private UserServiceImpl userService;

    private Users user;

    @BeforeEach
    public void setUp() {
        user = Users.builder()
                .id(1)
                .login("login1")
                .password("pass1")
                .firstname("name1")
                .lastname("surname1")
                .build();
    }

    @DisplayName("Тест метода save()")
    @Test
    public void givenUserObject_whenSaveUser_thenReturnUserObject() {
        when(userRepo.save(user)).thenReturn(user);

        Users savedUser = userService.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getFirstname()).isEqualTo("name1");
    }

    @DisplayName("Тест метода save() throw exception")
    @Test
    public void givenNullUser_whenSaveUser_thenThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> userService.save(null));

        verify(userRepo, never()).save(any(Users.class));
    }

    @DisplayName("Тест метода getUsers()")
    @Test
    public void givenUserList_whenGetUsers_thenReturnUserList() {
        Users user1 = Users.builder()
                .id(2)
                .login("login2")
                .password("pass2")
                .firstname("name2")
                .lastname("surname2")
                .build();

        when(userRepo.findAll()).thenReturn(List.of(user, user1));

        List<Users> usersList = userService.getUsers();
        assertThat(usersList).isNotNull();
        assertThat(usersList.size()).isEqualTo(2);
    }

    @DisplayName("Тест метода getUserById(int userId)")
    @Test
    public void givenUserId_whenGetUserById_thenReturnUserObject() {
        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        Optional<Users> foundUser = userService.getUserById(1);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.get().getFirstname()).isEqualTo("name1");
    }

    @DisplayName("Тест метода deleteById(int userId)")
    @Test
    public void givenUserId_whenDeleteById_thenNothing() {
        userService.deleteById(1);

        verify(userRepo).deleteById(1);
    }
}
