package com.factura.sri.service;

import com.factura.sri.dto.CajaDTO;
import com.factura.sri.dto.EmpresaDTO;
import com.factura.sri.dto.SucursalDTO;
import com.factura.sri.model.Caja;
import com.factura.sri.model.Empresa;
import com.factura.sri.model.Sucursal;
import com.factura.sri.repository.CajaRepository;
import com.factura.sri.repository.EmpresaRepository;
import com.factura.sri.repository.SucursalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final EmpresaRepository empresaRepository;
    private final SucursalRepository sucursalRepository;
    private final CajaRepository cajaRepository;

    public OrganizationService(EmpresaRepository empresaRepo, SucursalRepository sucursalRepo,
            CajaRepository cajaRepo) {
        this.empresaRepository = empresaRepo;
        this.sucursalRepository = sucursalRepo;
        this.cajaRepository = cajaRepo;
    }

    // --- Empresa ---
    @Transactional
    public Empresa guardarEmpresa(EmpresaDTO dto) {
        Empresa empresa = new Empresa();
        if (dto.getId() != null) {
            empresa = empresaRepository.findById(dto.getId()).orElse(new Empresa());
        }
        empresa.setRuc(dto.getRuc());
        empresa.setRazonSocial(dto.getRazonSocial());
        empresa.setNombreComercial(dto.getNombreComercial());
        empresa.setDireccionMatriz(dto.getDireccionMatriz());
        empresa.setContribuyenteEspecial(dto.getContribuyenteEspecial());
        empresa.setObligadoContabilidad(dto.getObligadoContabilidad());
        empresa.setLogoUrl(dto.getLogoUrl());
        empresa.setAmbienteSri(dto.getAmbienteSri());
        return empresaRepository.save(empresa);
    }

    public List<Empresa> listarEmpresas() {
        return empresaRepository.findAll();
    }

    // --- Sucursal ---
    @Transactional
    public Sucursal guardarSucursal(SucursalDTO dto) {
        Sucursal sucursal = new Sucursal();
        if (dto.getId() != null) {
            sucursal = sucursalRepository.findById(dto.getId()).orElse(new Sucursal());
        }
        sucursal.setCodigo(dto.getCodigo());
        sucursal.setDireccion(dto.getDireccion());
        sucursal.setNombre(dto.getNombre());

        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));
        sucursal.setEmpresa(empresa);

        return sucursalRepository.save(sucursal);
    }

    public List<SucursalDTO> listarSucursalesPorEmpresa(Long empresaId) {
        return sucursalRepository.findByEmpresaId(empresaId).stream().map(s -> {
            SucursalDTO dto = new SucursalDTO();
            dto.setId(s.getId());
            dto.setCodigo(s.getCodigo());
            dto.setNombre(s.getNombre());
            dto.setDireccion(s.getDireccion());
            dto.setEmpresaId(s.getEmpresa().getId());
            return dto;
        }).collect(Collectors.toList());
    }

    // --- Caja ---
    @Transactional
    public Caja guardarCaja(CajaDTO dto) {
        Caja caja = new Caja();
        if (dto.getId() != null) {
            caja = cajaRepository.findById(dto.getId()).orElse(new Caja());
        }
        caja.setPuntoEmision(dto.getPuntoEmision());
        caja.setNombre(dto.getNombre());

        Sucursal sucursal = sucursalRepository.findById(dto.getSucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada"));
        caja.setSucursal(sucursal);

        return cajaRepository.save(caja);
    }

    public List<CajaDTO> listarCajasPorSucursal(Long sucursalId) {
        return cajaRepository.findBySucursalId(sucursalId).stream().map(c -> {
            CajaDTO dto = new CajaDTO();
            dto.setId(c.getId());
            dto.setPuntoEmision(c.getPuntoEmision());
            dto.setNombre(c.getNombre());
            dto.setSucursalId(c.getSucursal().getId());
            return dto;
        }).collect(Collectors.toList());
    }
}
