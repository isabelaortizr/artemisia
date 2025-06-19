package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import com.artemisia_corp.artemisia.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private NotaVentaRepository notaVentaRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogsService logsService;
    @Autowired
    private ProductService productService;

    @Override
    public Page<OrderDetailResponseDto> getAllOrderDetails(Pageable pageable) {
        logsService.info("Fetching all order details");
        return orderDetailRepository.findAllOrderDetails(pageable);
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
    public OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto,
                                                    NotaVenta notaVentaParam,
                                                    Product productParam) {

        NotaVenta notaVenta = notaVentaParam;
        if (notaVenta == null || orderDetailDto.getGroupId() != null) {
            notaVenta = notaVentaRepository.findById(orderDetailDto.getGroupId())
                    .orElseThrow(() -> {
                        logsService.error("Sale note not found with ID: " + orderDetailDto.getGroupId());
                        throw new RuntimeException("Sale note not found");
                    });
        }

        Product product = productParam;
        if (product == null || orderDetailDto.getProductId() != null) {
            product = productRepository.findById(orderDetailDto.getProductId())
                    .orElseThrow(() -> {
                        logsService.error("Product not found with ID: " + orderDetailDto.getProductId());
                        throw new RuntimeException("Product not found");
                    });
        }

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

        if (orderDetail.getQuantity() <= 0) {
            logsService.error("Quantity must be greater than 0");
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        if (orderDetail.getTotal() == null || orderDetail.getTotal() <= 0) {
            logsService.error("Total must be a positive value");
            throw new IllegalArgumentException("Total must be a positive value");
        }

        OrderDetail savedOrderDetail = orderDetailRepository.save(orderDetail);
        logsService.info("OrderDetail created successfully with ID: " + savedOrderDetail.getId());

        return OrderDetailResponseDto.builder()
                .id(savedOrderDetail.getId())
                .groupId(notaVenta.getId())
                .productId(product.getId())
                .sellerId(seller.getId())
                .productName(savedOrderDetail.getProductName())
                .quantity(savedOrderDetail.getQuantity())
                .total(savedOrderDetail.getTotal())
                .build();
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
    public void updateQuantityOrderDetail(UpdateQuantityDetailDto updateDetailDto) {
        long id = updateDetailDto.getOrderDetailId();
        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Order detail not found with ID: " + id);
                    throw new RuntimeException("Order detail not found");
                });

        productRepository.findById(updateDetailDto.getProductId())
                .orElseThrow(() -> {
                    logsService.error("Product not found with ID: " + updateDetailDto.getProductId());
                    throw new RuntimeException("Product not found");
                });

        orderDetail.setQuantity(updateDetailDto.getQuantity());

        productService.manageStock(new ManageProductDto(updateDetailDto.getProductId(),
                updateDetailDto.getQuantity()));

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
        logsService.info("Order detail updated with ID: " + updatedOrderDetail.getId());
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
    public Page<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId, Pageable pageable) {
        logsService.info("Fetching order details for sale note ID: " + notaVentaId);
        return orderDetailRepository.findByGroup_Id(notaVentaId, pageable);
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
                .productId(orderDetail.getProduct().getId())
                .sellerId(orderDetail.getSeller().getId())
                .productName(orderDetail.getProductName())
                .quantity(orderDetail.getQuantity())
                .total(orderDetail.getTotal())
                .build();
    }
}