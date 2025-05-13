package com.grup7.Service;

import com.grup7.Entity.OrderLog;
import com.grup7.Entity.User;
import com.grup7.Repository.IOrderLogRepository;
import com.grup7.Repository.IUserRepository;
import com.grup7.Util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderLogService {

    @Autowired
    private IOrderLogRepository orderLogRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public OrderLog closeOrder(Long orderId) {
        // Kullanıcıyı bul
        User user = userRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Rezervasyon bulunamadı"));

        // Log kaydı oluştur
        OrderLog orderLog = new OrderLog();
        orderLog.setCustomerName(user.getName());
        orderLog.setCustomerSurname(user.getSurname());
        orderLog.setTableNumber(user.getReservedTable().getTableNumber());
        orderLog.setReservationCode(user.getReservationCode());
        orderLog.setReservationDate(user.getDate().atStartOfDay());
        orderLog.setClosedAt(LocalDateTime.now());

        // Dosyaya log yaz
        LogUtil.logToFile(
                user.getName(),
                user.getSurname(),
                user.getReservedTable().getTableNumber(),
                user.getReservationCode()
        );

        // Log'u veritabanına kaydet
        OrderLog savedLog = orderLogRepository.save(orderLog);

        return savedLog;
    }
}