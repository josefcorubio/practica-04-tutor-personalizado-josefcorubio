package com.curso.springai.tutor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.curso.springai.tutor.model.RespuestaTutor;
import com.curso.springai.tutor.service.TutorService;

@RestController
@RequestMapping("tutor")
public class TutorController {
	private final TutorService tutorService;

	public TutorController(TutorService tutorService) {
		this.tutorService = tutorService;
	}

	@GetMapping("explicar")
	public ResponseEntity<RespuestaTutor> explicar(@RequestParam String tema) {
		return ResponseEntity.ok(tutorService.explicar(tema));
	}
}
