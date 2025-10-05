package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.*;
import com.artemisia_corp.artemisia.entity.dto.order_detail.*;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.ManageProductDto;
import com.artemisia_corp.artemisia.exception.NotDataFoundException;
import com.artemisia_corp.artemisia.repository.*;
import com.artemisia_corp.artemisia.service.LogsService;
import com.artemisia_corp.artemisia.service.OrderDetailService;
import com.artemisia_corp.artemisia.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
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
        if (id == null || id <= 0) {
            logsService.error("Order detail ID must be greater than 0.");
            throw new IllegalArgumentException("Order detail ID must be greater than 0.");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Order detail not found with ID: " + id);
                    return new NotDataFoundException("Order detail not found with ID: " + id);
                });

        return convertToDto(orderDetail);
    }

    @Override
    public OrderDetailResponseDto createOrderDetail(OrderDetailRequestDto orderDetailDto, NotaVenta notaVentaParam, Product productParam) {
        if (orderDetailDto == null) {
            logsService.error("Order detail data is required.");
            throw new IllegalArgumentException("Order detail data is required.");
        }
        if (orderDetailDto.getGroupId() == null || orderDetailDto.getGroupId() <= 0) {
            logsService.error("Valid NotaVenta ID is required.");
            throw new IllegalArgumentException("Valid NotaVenta ID is required.");
        }
        if (orderDetailDto.getProductId() == null || orderDetailDto.getProductId() <= 0) {
            logsService.error("Valid Product ID is required.");
            throw new IllegalArgumentException("Valid Product ID is required.");
        }

        NotaVenta notaVenta = notaVentaRepository.findById(orderDetailDto.getGroupId())
                .orElseThrow(() -> {
                        logsService.error("User not found with ID: " + orderDetailDto.getSellerId());
                        return new NotDataFoundException("NotaVenta not found with ID: " + orderDetailDto.getGroupId());
                });

        Product product = productRepository.findById(orderDetailDto.getProductId())
                .orElseThrow(() -> {
                        logsService.error("User not found with ID: " + orderDetailDto.getSellerId());
                        return new NotDataFoundException("Product not found with ID: " + orderDetailDto.getProductId());
                });

        User seller = userRepository.findById(orderDetailDto.getSellerId())
                .orElseThrow(() -> {
                    logsService.error("User not found with ID: " + orderDetailDto.getSellerId());
                    return new NotDataFoundException("User not found");
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

        productService.manageStock(new ManageProductDto(product.getId(), orderDetail.getQuantity(), true));
        return convertToDto(savedOrderDetail);
    }

    @Override
    public OrderDetailResponseDto updateOrderDetail(Long id, OrderDetailRequestDto orderDetailDto) {
        if (id == null || id <= 0) {
            logsService.error("Order detail ID must exist or be greater than 0.");
            throw new IllegalArgumentException("Order detail ID must be greater than 0.");
        }

        if (orderDetailDto == null) {
            logsService.error("Order detail data is required.");
            throw new IllegalArgumentException("Order detail data is required.");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(id)
                .orElseThrow(() -> {
                    logsService.error("Order detail not found with ID: " + id);
                    return new NotDataFoundException("Order detail not found with ID: " + id);
                });

        if (orderDetailDto.getQuantity() != null && orderDetailDto.getQuantity() > 0) {
            this.updateQuantityOrderDetail(new UpdateQuantityDetailDto(
                    orderDetail.getId(),
                    orderDetailDto.getQuantity()));
        }

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
        return convertToDto(updatedOrderDetail);
    }

    @Override
    public void updateQuantityOrderDetail(UpdateQuantityDetailDto updateDetailDto) {
        if (updateDetailDto == null) {
            throw new IllegalArgumentException("Update detail data is required.");
        }

        if (updateDetailDto.getOrderDetailId() == null || updateDetailDto.getOrderDetailId() <= 0) {
            throw new IllegalArgumentException("Valid order detail ID is required.");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(updateDetailDto.getOrderDetailId())
                .orElseThrow(() -> new NotDataFoundException("Order detail not found with ID: " + updateDetailDto.getOrderDetailId()));

        if (updateDetailDto.getQuantity() == null || updateDetailDto.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0.");
        } else if (updateDetailDto.getQuantity() == 0) {
            productService.manageStock(new ManageProductDto(orderDetail.getProduct().getId(),
                    orderDetail.getQuantity(), false));
            this.deleteOrderDetail(orderDetail.getId());
            return;
        }

        productService.manageStock(new ManageProductDto(orderDetail.getProduct().getId(),
                orderDetail.getQuantity(), false));
        productService.manageStock(new ManageProductDto(orderDetail.getProduct().getId(),
                updateDetailDto.getQuantity()));

        NotaVenta notaVenta = orderDetail.getGroup();
        notaVenta.setTotalGlobal(notaVenta.getTotalGlobal() - orderDetail.getTotal());

        orderDetail.setQuantity(updateDetailDto.getQuantity());
        orderDetail.setTotal(orderDetail.getProduct().getPrice() * orderDetail.getQuantity());

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);
        logsService.info("Order detail updated with ID: " + updatedOrderDetail.getId());

        notaVenta.setTotalGlobal(notaVenta.getTotalGlobal() + orderDetail.getTotal());
        notaVentaRepository.save(notaVenta);
    }

    @Override
    public void deleteOrderDetail(Long id) {
        if (id == null || id <= 0) {
            logsService.error("Order detail ID must exist or be greater than 0.");
            throw new IllegalArgumentException("Order detail ID must be greater than 0.");
        }

        if (!orderDetailRepository.existsById(id)) {
            logsService.error("Order detail not found with ID: " + id);
            throw new NotDataFoundException("Order detail not found with ID: " + id);
        }

        orderDetailRepository.deleteById(id);
        logsService.info("Order detail deleted successfully with ID: " + id);
    }

    @Override
    public Page<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId, Pageable pageable) {
        if (notaVentaId == null || notaVentaId <= 0) {
            logsService.error("Valid NotaVenta ID is required.");
            throw new IllegalArgumentException("Valid NotaVenta ID is required.");
        }

        logsService.info("Fetching order details for sale note ID: " + notaVentaId);
        return orderDetailRepository.findByGroup_Id(notaVentaId, pageable);
    }

    @Override
    public List<OrderDetailResponseDto> getOrderDetailsByNotaVenta(Long notaVentaId) {
        if (notaVentaId == null || notaVentaId <= 0) {
            logsService.error("Valid NotaVenta ID is required.");
            throw new IllegalArgumentException("Valid NotaVenta ID is required.");
        }

        logsService.info("Fetching order details for sale note ID: " + notaVentaId);
        log.info("Fetching order details for sale note ID: " + notaVentaId);

        List<OrderDetailResponseDto> listOrderDetail = orderDetailRepository.findByGroup_Id(notaVentaId);

        verificarYActualizarPreciosNotaVenta(notaVentaId, listOrderDetail);

        return listOrderDetail;
    }

    @Override
    public Page<OrderDetailResponseDto> getOrderDetailsBySeller(Long sellerId, Pageable pageable) {
        return orderDetailRepository.findDtoBySellerId(sellerId, pageable);
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

    private void verificarYActualizarPreciosNotaVenta(Long notaVentaId, List<OrderDetailResponseDto> orderDetails) {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return;
        }

        NotaVenta notaVenta = notaVentaRepository.findById(notaVentaId)
                .orElseThrow(() -> {
                    logsService.error("NotaVenta not found with ID: " + notaVentaId);
                    return new NotDataFoundException("NotaVenta not found with ID: " + notaVentaId);
                });

        boolean necesitaActualizacion = false;
        Double nuevoTotalGlobal = 0.0;

        for (OrderDetailResponseDto orderDetail : orderDetails) {
            Product productoActual = productRepository.findById(orderDetail.getProductId())
                    .orElseThrow(() -> {
                        logsService.error("Product not found with ID: " + orderDetail.getProductId());
                        return new NotDataFoundException("Product not found with ID: " + orderDetail.getProductId());
                    });

            Double totalActual = productoActual.getPrice() * orderDetail.getQuantity();

            if (!orderDetail.getTotal().equals(totalActual)) {
                necesitaActualizacion = true;
                OrderDetail orderDetailEntity = orderDetailRepository.findById(orderDetail.getId())
                        .orElseThrow(() -> new NotDataFoundException("Order detail not found with ID: " + orderDetail.getId()));

                orderDetailEntity.setTotal(totalActual);
                orderDetailRepository.save(orderDetailEntity);

                logsService.info("Updated price for OrderDetail ID: " + orderDetail.getId() +
                        " from " + orderDetail.getTotal() + " to " + totalActual);

                orderDetail.setTotal(totalActual);
            }

            nuevoTotalGlobal += orderDetail.getTotal();
        }

        if (necesitaActualizacion || !notaVenta.getTotalGlobal().equals(nuevoTotalGlobal)) {
            Double totalGlobalAnterior = notaVenta.getTotalGlobal();
            notaVenta.setTotalGlobal(nuevoTotalGlobal);
            notaVentaRepository.save(notaVenta);

            logsService.info("Updated total global for NotaVenta ID: " + notaVentaId +
                    " from " + totalGlobalAnterior + " to " + nuevoTotalGlobal);
        }
    }
}