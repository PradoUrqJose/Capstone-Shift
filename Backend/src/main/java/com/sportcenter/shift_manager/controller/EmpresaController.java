package com.sportcenter.shift_manager.controller;

import com.sportcenter.shift_manager.dto.EmpresaDTO;
import com.sportcenter.shift_manager.model.Empresa;
import com.sportcenter.shift_manager.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {
    private final EmpresaService empresaService;

    public EmpresaController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @PostMapping
    public ResponseEntity<EmpresaDTO> saveEmpresa(@Valid @RequestBody Empresa empresa,
                                                  @RequestHeader("Authorization") String token) {
        Empresa savedEmpresa = empresaService.saveEmpresa(empresa, token);
        return ResponseEntity.ok(empresaService.convertToDTO(savedEmpresa));
    }

    @GetMapping
    public ResponseEntity<List<EmpresaDTO>> getAllEmpresas(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(empresaService.getAllEmpresas(token));
    }

    @GetMapping("/{id}/numero-empleados")
    public ResponseEntity<Integer> getNumeroDeEmpleados(@PathVariable Long id,
                                                        @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(empresaService.getNumeroDeEmpleados(id, token));
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<EmpresaDTO>> getEmpresasPorHabilitacion(@RequestParam boolean habilitada,
                                                                       @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(empresaService.getEmpresasPorHabilitacion(habilitada, token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpresaDTO> updateEmpresa(@PathVariable Long id, @Valid @RequestBody Empresa empresaDetails,
                                                    @RequestHeader("Authorization") String token) {
        Empresa updatedEmpresa = empresaService.updateEmpresa(id, empresaDetails, token);
        return ResponseEntity.ok(empresaService.convertToDTO(updatedEmpresa));
    }

    @PutMapping("/{id}/habilitacion")
    public ResponseEntity<EmpresaDTO> toggleHabilitacionEmpresa(@PathVariable Long id, @RequestParam boolean habilitada,
                                                                @RequestHeader("Authorization") String token) {
        Empresa toggledEmpresa = empresaService.toggleHabilitacionEmpresa(id, habilitada, token);
        return ResponseEntity.ok(empresaService.convertToDTO(toggledEmpresa));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmpresa(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        empresaService.deleteEmpresa(id, token);
        return ResponseEntity.noContent().build();
    }
}