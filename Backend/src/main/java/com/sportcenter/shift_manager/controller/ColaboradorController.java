package com.sportcenter.shift_manager.controller;

import com.sportcenter.shift_manager.dto.ColaboradorDTO;
import com.sportcenter.shift_manager.model.Colaborador;
import com.sportcenter.shift_manager.service.ColaboradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/colaboradores")
public class ColaboradorController {
    private final ColaboradorService colaboradorService;

    public ColaboradorController(ColaboradorService colaboradorService) {
        this.colaboradorService = colaboradorService;
    }

    @PostMapping
    public ResponseEntity<ColaboradorDTO> saveColaborador(
            @Valid @RequestPart("colaborador") ColaboradorDTO colaboradorDTO,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String token) throws IOException {
        Colaborador colaborador = colaboradorService.saveColaborador(colaboradorDTO, file, token);
        ColaboradorDTO colaboradorResponse = colaboradorService.convertToDTO(colaborador);
        return ResponseEntity.ok(colaboradorResponse);
    }

    @GetMapping
    public ResponseEntity<List<ColaboradorDTO>> getAllColaboradores(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(colaboradorService.getAllColaboradores(token));
    }

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ColaboradorDTO>> getColaboradoresByEmpresa(@PathVariable Long empresaId,
                                                                          @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(colaboradorService.getColaboradoresByEmpresa(empresaId, token));
    }

    @GetMapping("/filtro")
    public ResponseEntity<List<ColaboradorDTO>> getColaboradoresPorHabilitacion(@RequestParam boolean habilitado,
                                                                                @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(colaboradorService.getColaboradoresPorHabilitacion(habilitado, token));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ColaboradorDTO> updateColaborador(
            @PathVariable Long id,
            @Valid @RequestPart("colaborador") ColaboradorDTO colaboradorDTO,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String token) throws IOException {
        Colaborador colaborador = colaboradorService.updateColaborador(id, colaboradorDTO, file, token);
        ColaboradorDTO dto = colaboradorService.convertToDTO(colaborador);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}/habilitacion")
    public ResponseEntity<ColaboradorDTO> toggleHabilitacionColaborador(
            @PathVariable Long id, @RequestParam boolean habilitado,
            @RequestHeader("Authorization") String token) {
        Colaborador colaborador = colaboradorService.toggleHabilitacionColaborador(id, habilitado, token);
        ColaboradorDTO dto = colaboradorService.convertToDTO(colaborador);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteColaborador(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        colaboradorService.deleteColaborador(id, token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ColaboradorDTO> getColaboradorById(@PathVariable Long id,
                                                             @RequestHeader("Authorization") String token) {
        Colaborador colaborador = colaboradorService.getColaboradorById(id, token);
        ColaboradorDTO dto = colaboradorService.convertToDTO(colaborador);
        return ResponseEntity.ok(dto);
    }
}