package com.curso.springai.tutor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import com.curso.springai.tutor.exception.ServicioNoDisponibleException;
import com.curso.springai.tutor.model.RespuestaTutor;

class TutorServiceTest {
	private VectorStore vectorStore;
	private ChatClient chatClient;
	private TutorService service;

	@BeforeEach
	void setUp() {
		vectorStore = mock(VectorStore.class);
		chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
		ChatClient.Builder builder = mock(ChatClient.Builder.class);
		when(builder.build()).thenReturn(chatClient);
		service = new TutorService(vectorStore, builder);
	}

	@Test
	void sinChunksNoLlamaAlModeloNiDevuelveFuentes() {
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

		RespuestaTutor r = service.explicar("receta de paella");

		assertEquals(TutorService.SIN_INFORMACION, r.explicacion());
		assertEquals(List.of(), r.fuentes());
		verify(chatClient, never()).prompt();
	}

	@Test
	void fuentesSonLosFicherosDeLosChunksSinRepetir() {
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
				new Document("a", Map.of("file_name", "spring_mvc.pdf")),
				new Document("b", Map.of("file_name", "spring_mvc.pdf")),
				new Document("c", Map.of("file_name", "angular x.pdf"))));
		when(chatClient.prompt().system(any(String.class)).user(any(String.class)).call().content())
				.thenReturn("explicación");

		RespuestaTutor r = service.explicar("mvc");

		assertEquals("explicación", r.explicacion());
		assertEquals(List.of("spring_mvc.pdf", "angular x.pdf"), r.fuentes());
	}

	@Test
	void temaVacioOMuyLargoEsInvalido() {
		assertThrows(IllegalArgumentException.class, () -> service.explicar("   "));
		assertThrows(IllegalArgumentException.class, () -> service.explicar("x".repeat(TutorService.MAX_LONGITUD_TEMA + 1)));
	}

	@Test
	void falloDeQdrantOOllamaSeConvierteEnServicioNoDisponible() {
		when(vectorStore.similaritySearch(any(SearchRequest.class))).thenThrow(new RuntimeException("conexión rechazada"));

		assertThrows(ServicioNoDisponibleException.class, () -> service.explicar("mvc"));
	}
}
