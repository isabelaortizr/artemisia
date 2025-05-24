package com.artemisia_corp.artemisia.service.impl;

import com.artemisia_corp.artemisia.entity.Address;
import com.artemisia_corp.artemisia.entity.NotaVenta;
import com.artemisia_corp.artemisia.entity.Users;
import com.artemisia_corp.artemisia.entity.dto.nota_venta.*;
import com.artemisia_corp.artemisia.entity.enums.VentaEstado;
import com.artemisia_corp.artemisia.repository.NotaVentaRepository;
import com.artemisia_corp.artemisia.service.NotaVentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotaVentaServiceImpl implements NotaVentaService {

    @Autowired
    private NotaVentaRepository notaVentaRepository;

    @Override
    public List<NotaVentaResponseWCustomerDto> listWithBuyer() {
        return notaVentaRepository.findWithBuyer();
    }

    @Override
    public List<NotaVentaResponseDto> listAll() {
        return notaVentaRepository.findAllNotaVentas();
    }

    @Override
    public void save(NotaVentaRequestDto notaVentaDto) {
        // Crear entidades relacionadas
        Users buyer = new Users();
        buyer.setId(notaVentaDto.getBuyerId());

        Address address = new Address();
        address.setAddressId(notaVentaDto.getBuyerAddressId());

        // Construir la entidad NotaVenta
        NotaVenta notaVenta = NotaVenta.builder()
                .buyer(buyer)
                .buyerAddress(address)
                .estadoVenta(VentaEstado.valueOf(notaVentaDto.getEstadoVenta()))
                .totalGlobal(notaVentaDto.getTotalGlobal())
                .date(notaVentaDto.getDate() != null ? notaVentaDto.getDate() : LocalDateTime.now())
                .build();

        notaVentaRepository.save(notaVenta);
    }

    @Override
    public void delete(NotaVentaDeleteDto notaVentaDto) {
        // Verificar existencia antes de eliminar
        if (!notaVentaRepository.existsById(notaVentaDto.getId())) {
            throw new RuntimeException("Nota de venta no encontrada con ID: " + notaVentaDto.getId());
        }
        notaVentaRepository.deleteById(notaVentaDto.getId());
    }

    @Override
    public void update(NotaVentaUpdateDto notaVentaDto) {
        NotaVenta notaVenta = notaVentaRepository.findById(notaVentaDto.getId())
                .orElseThrow(() -> new RuntimeException("Nota de venta no encontrada con ID: " + notaVentaDto.getId()));

        // Actualizar campos modificables
        if (notaVentaDto.getEstadoVenta() != null) {
            notaVenta.setEstadoVenta(VentaEstado.valueOf(notaVentaDto.getEstadoVenta()));
        }

        if (notaVentaDto.getTotalGlobal() != null) {
            notaVenta.setTotalGlobal(notaVentaDto.getTotalGlobal());
        }

        if (notaVentaDto.getBuyerAddress() != null) {
            Address address = new Address();
            address.setAddressId(notaVentaDto.getBuyerAddress());
            notaVenta.setBuyerAddress(address);
        }

        if (notaVentaDto.getDate() != null) {
            notaVenta.setDate(notaVentaDto.getDate());
        }

        notaVentaRepository.save(notaVenta);
    }
}