package com.curso.springai.tutor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.curso.springai.tutor.service.IngestionService;

@RestController
@RequestMapping("rag")
public class IngestionController {
	private final IngestionService ingestionService;

	public IngestionController(IngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	@PostMapping("ingest")
	public ResponseEntity<String> ingest() {
		int chunks = ingestionService.ingestarDocumentos();
		return ResponseEntity.ok("Ingesta completada: " + chunks + " chunks");
	}

	@DeleteMapping("clear")
	public ResponseEntity<String> clear() {
		ingestionService.eliminarDocumentos();
		return ResponseEntity.ok("Documentos eliminados");
	}
}
