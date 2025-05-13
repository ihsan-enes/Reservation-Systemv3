package com.grup7.Service;

import com.grup7.Dto.OrderDto;
import com.grup7.Entity.Category;
import com.grup7.Entity.Order;
import com.grup7.Repository.IOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private IOrderRepository orderRepository;

    @Autowired
    private ExternalMenuService externalMenuService;

    public Order saveOrderWithCategories(OrderDto orderDto) {
        Order order = new Order();
        order.setReservationCode(orderDto.getReservationCode());
        order.setCategoryIds(orderDto.getCategoryIds());
        return orderRepository.save(order);
    }

    public List<String> getCategoryNamesByReservationCode(String reservationCode) {
        Order order = orderRepository.findByReservationCode(reservationCode)
                .orElseThrow(() -> new RuntimeException("Order not found with reservation code: " + reservationCode));

        List<Category> allCategories = externalMenuService.getCategories();

        return order.getCategoryIds().stream()
                .map(categoryId -> allCategories.stream()
                        .filter(category -> category.getIdCategory().equals(categoryId))
                        .map(Category::getStrCategory)
                        .findFirst()
                        .orElse("Unknown Category"))
                .collect(Collectors.toList());
    }
}