package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.*;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {
    private final OrderDetailRepository orderDetailRepository;
    private final NotaVentaRepository notaVentaRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final LogsService logsService;

    @Override
    public List<OrderDetailResponseDto> getAllOrderDetails() {
        logsService.info("Fetching all order details");
        return orderDetailRepository.findAllOrderDetails();
    }

    @Override
    public OrderDetailResponseDto getOrderDetailById(Long id) {
        logsService.info("Fetching order detail with ID: " + id);
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Order detail not found with ID: " + id);
                    throw new RuntimeException("Order detail not found");
                });
        return convertToDto(orderDetail);
    }

    @Override
    public OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto) {
        NotaVenta notaVenta = notaVentaRepository.findById(orderDetailDto.getGroupId())
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + orderDetailDto.getGroupId());
                    throw new RuntimeException("Sale note not found");
                });

        Product product = productRepository.findById(orderDetailDto.getProductId())
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + orderDetailDto.getProductId());
                    throw new RuntimeException("Product not found");
                });

        User seller = userRepository.findById(orderDetailDto.getSellerId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + orderDetailDto.getSellerId());
                    throw new RuntimeException("User not found");
                });

        OrderDetail orderDetail = OrderDetail.builder()
                .group(notaVenta)
                .product(product)
                .seller(seller)
                .productName(orderDetailDto.getProductName())
                .quantity(orderDetailDto.getQuantity())
                .total(orderDetailDto.getTotal())
                .build();

        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        logsService.info("Order detail created with ID: " + savedOrderDetail.getId());
        return convertToDto(savedOrderDetail);
    }

    @Override
    public OrderDetailResponseDto updateOrderDetail(Long id, OrderDetailRequestDto orderDetailDto) {
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Order detail not found with ID: " + id);
                    throw new RuntimeException("Order detail not found");
                });

        NotaVenta notaVenta = notaVentaRepository.findById(orderDetailDto.getGroupId())
                .orElseThrow(() -> {
                    logsService.error("Sale note not found with ID: " + orderDetailDto.getGroupId());
                    throw new RuntimeException("Sale note not found");
                });

        Product product = productRepository.findById(orderDetailDto.getProductId())
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + orderDetailDto.getProductId());
                    throw new RuntimeException("Product not found");
                });

        User seller = userRepository.findById(orderDetailDto.getSellerId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + orderDetailDto.getSellerId());
                    throw new RuntimeException("User not found");
                });

        orderDetail.setGroup(notaVenta);
        orderDetail.setProduct(product);
        orderDetail.setSeller(seller);
        orderDetail.setProductName(orderDetailDto.getProductName());
        orderDetail.setQuantity(orderDetailDto.getQuantity());
        orderDetail.setTotal(orderDetailDto.getTotal());

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
        logsService.info("Order detail updated with ID: " + updatedOrderDetail.getId());
        return convertToDto(updatedOrderDetail);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        if (!orderDetailRepository.existsById(id)) {
            logsService.error("Order detail not found with ID: " + id);
            throw new RuntimeException("Order detail not found");
        }
        orderDetailRepository.deleteById(id);
        logsService.info("Order detail deleted with ID: " + id);
    }

    @Override
    public List<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId) {
        logsService.info("Fetching order details for sale note ID: " + notaVentaId);
        return orderDetailRepository.findByGroup_Id(notaVentaId);
    }

    private OrderDetailResponseDto convertToDto(OrderDetail orderDetail) {
        return OrderDetailResponseDto.builder()
                .id(orderDetail.getId())
                .groupId(orderDetail.getGroup().getId())
                .productId(orderDetail.getProduct().getProductId())
                .sellerId(orderDetail.getSeller().getId())
                .productName(orderDetail.getProductName())
                .quantity(orderDetail.getQuantity())
                .total(orderDetail.getTotal())
                .build();
    }
}