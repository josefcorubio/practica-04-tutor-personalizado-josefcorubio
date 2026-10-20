package com.curso.springai.tutor.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.curso.springai.tutor.exception.ApiExceptionHandler;
import com.curso.springai.tutor.exception.ServicioNoDisponibleException;
import com.curso.springai.tutor.model.RespuestaTutor;
import com.curso.springai.tutor.service.TutorService;

class TutorControllerTest {
	private TutorService tutorService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		tutorService = mock(TutorService.class);
		mockMvc = MockMvcBuilders.standaloneSetup(new TutorController(tutorService))
				.setControllerAdvice(new ApiExceptionHandler())
				.build();
	}

	@Test
	void devuelveExplicacionYFuentes() throws Exception {
		when(tutorService.explicar("mvc")).thenReturn(new RespuestaTutor("texto", List.of("spring_mvc.pdf")));

		mockMvc.perform(get("/tutor/explicar").param("tema", "mvc"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.explicacion").value("texto"))
				.andExpect(jsonPath("$.fuentes[0]").value("spring_mvc.pdf"));
	}

	@Test
	void temaInvalidoDevuelve400() throws Exception {
		when(tutorService.explicar(" ")).thenThrow(new IllegalArgumentException("El tema no puede estar vacío."));

		mockMvc.perform(get("/tutor/explicar").param("tema", " "))
				.andExpect(status().isBadRequest());
	}

	@Test
	void faltaElParametroDevuelve400() throws Exception {
		mockMvc.perform(get("/tutor/explicar")).andExpect(status().isBadRequest());
	}

	@Test
	void servicioCaidoDevuelve503() throws Exception {
		when(tutorService.explicar("mvc")).thenThrow(new ServicioNoDisponibleException("caído", new RuntimeException()));

		mockMvc.perform(get("/tutor/explicar").param("tema", "mvc"))
				.andExpect(status().isServiceUnavailable());
	}
}
