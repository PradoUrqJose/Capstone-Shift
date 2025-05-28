package com.sportcenter.shift_manager.controller;

import com.sportcenter.shift_manager.dto.TiendaDTO;
import com.sportcenter.shift_manager.service.TiendaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tiendas")
public class TiendaController {
    private final TiendaService tiendaService;

    public TiendaController(TiendaService tiendaService) {
        this.tiendaService = tiendaService;
    }

    @PostMapping
    public ResponseEntity<TiendaDTO> createTienda(@Valid @RequestBody TiendaDTO tiendaDTO,
                                                  @RequestHeader("Authorization") String token) {
        TiendaDTO savedTienda = tiendaService.saveTienda(tiendaDTO, token);
        return ResponseEntity.ok(savedTienda);
    }

    @GetMapping
    public ResponseEntity<List<TiendaDTO>> getAllTiendas(@RequestHeader("Authorization") String token) {
        List<TiendaDTO> tiendas = tiendaService.getAllTiendas(token);
        return ResponseEntity.ok(tiendas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TiendaDTO> getTiendaById(@PathVariable Long id,
                                                   @RequestHeader("Authorization") String token) {
        TiendaDTO tienda = tiendaService.getTiendaById(id, token);
        return ResponseEntity.ok(tienda);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TiendaDTO> updateTienda(@PathVariable Long id, @Valid @RequestBody TiendaDTO tiendaDTO,
                                                  @RequestHeader("Authorization") String token) {
        TiendaDTO updatedTienda = tiendaService.updateTienda(id, tiendaDTO, token);
        return ResponseEntity.ok(updatedTienda);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTienda(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        tiendaService.deleteTienda(id, token);
        return ResponseEntity.noContent().build();
    }
}