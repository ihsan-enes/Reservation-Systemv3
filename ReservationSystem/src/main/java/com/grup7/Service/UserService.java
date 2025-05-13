package com.grup7.Service;

import com.grup7.Dto.UserDto;
import com.grup7.Dto.TableDto;
import com.grup7.Entity.User;
import com.grup7.Entity.Table;
import com.grup7.Repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private TableService tableService;

    // Table entity'sini TableDto'ya dönüştüren yardımcı metod
    private TableDto convertToTableDto(Table table, LocalDate reservationDate) {
        TableDto dto = new TableDto();
        dto.setId(table.getId());
        dto.setTableNumber(table.getTableNumber());
        dto.setReservationDate(reservationDate);
        return dto;
    }

    // UserDto'yu User entity'sine dönüştüren yardımcı metod
    private User convertToUser(UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setDate(userDto.getDate());
        return user;
    }

    // User entity'sini UserDto'ya dönüştüren yardımcı metod
    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());
        dto.setDate(user.getDate());
        dto.setReservationCode(user.getReservationCode());
        if (user.getReservedTable() != null) {
            dto.setReservedTable(convertToTableDto(user.getReservedTable(), user.getDate()));
        }
        return dto;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDto)
                .collect(Collectors.toList());
    }

    public UserDto addUser(UserDto userDto) {
        List<Table> availableTables = tableService.getAvailableTables(userDto.getDate());

        if (availableTables.isEmpty()) {
            throw new RuntimeException("Belirtilen tarih için uygun masa bulunmamaktadır");
        }

        Table selectedTable = availableTables.get(0);
        boolean reserved = tableService.reserveTable(selectedTable.getId(), userDto.getDate());

        if (!reserved) {
            throw new RuntimeException("Masa rezervasyonu yapılamadı");
        }

        User newUser = convertToUser(userDto);
        newUser.setReservedTable(selectedTable);

        User savedUser = userRepository.save(newUser);
        return convertToUserDto(savedUser);
    }

    public UserDto updateUser(Long id, UserDto userDto) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User updatedUser = userOptional.get();

            if (!updatedUser.getDate().equals(userDto.getDate())) {
                if (updatedUser.getReservedTable() != null) {
                    tableService.cancelReservation(
                            updatedUser.getReservedTable().getId(),
                            updatedUser.getDate()
                    );
                }

                List<Table> availableTables = tableService.getAvailableTables(userDto.getDate());

                if (availableTables.isEmpty()) {
                    throw new RuntimeException("Yeni tarih için uygun masa bulunmamaktadır");
                }

                Table newTable = availableTables.get(0);
                tableService.reserveTable(newTable.getId(), userDto.getDate());
                updatedUser.setReservedTable(newTable);
            }

            updatedUser.setName(userDto.getName());
            updatedUser.setSurname(userDto.getSurname());
            updatedUser.setDate(userDto.getDate());

            User savedUser = userRepository.save(updatedUser);
            return convertToUserDto(savedUser);
        }
        return null;
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        if (user.getReservedTable() != null) {
            tableService.cancelReservation(user.getReservedTable().getId(), user.getDate());
        }

        userRepository.deleteById(id);
    }
}