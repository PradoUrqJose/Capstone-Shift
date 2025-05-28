package com.sportcenter.shift_manager.controller;

import com.sportcenter.shift_manager.dto.PuestoDTO;
import com.sportcenter.shift_manager.service.PuestoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puestos")
public class PuestoController {
    private final PuestoService puestoService;

    public PuestoController(PuestoService puestoService) {
        this.puestoService = puestoService;
    }

    @PostMapping
    public ResponseEntity<PuestoDTO> savePuesto(@Valid @RequestBody PuestoDTO puestoDTO,
                                                @RequestHeader("Authorization") String token) {
        PuestoDTO savedPuesto = puestoService.savePuesto(puestoDTO, token);
        return ResponseEntity.ok(savedPuesto);
    }

    @GetMapping
    public ResponseEntity<List<PuestoDTO>> getAllPuestos(@RequestHeader("Authorization") String token) {
        List<PuestoDTO> puestos = puestoService.getAllPuestos(token);
        return ResponseEntity.ok(puestos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuestoDTO> getPuestoById(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        PuestoDTO puesto = puestoService.getPuestoById(id, token);
        return ResponseEntity.ok(puesto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PuestoDTO> updatePuesto(@PathVariable Long id, @Valid @RequestBody PuestoDTO puestoDTO,
                                                  @RequestHeader("Authorization") String token) {
        PuestoDTO updatedPuesto = puestoService.updatePuesto(id, puestoDTO, token);
        return ResponseEntity.ok(updatedPuesto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePuesto(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        puestoService.deletePuesto(id, token);
        return ResponseEntity.noContent().build();
    }
}