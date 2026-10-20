package com.curso.springai.tutor.service;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.curso.springai.tutor.model.RespuestaTutor;

@Service
public class TutorService {
	private static final String SIN_INFORMACION = "No he encontrado información sobre este tema en los documentos disponibles.";

	private final String system = """
			Eres un tutor experto que explica conceptos y tecnologías a estudiantes.
			Responde SIEMPRE en español y basándote única y exclusivamente en el contexto proporcionado.
			No utilices conocimiento externo ni inventes información.
			Explica el tema de forma clara y didáctica, con ejemplos del contexto si los hay.
			Si el contexto no contiene información suficiente sobre el tema, responde exactamente: "%s"
			No menciones nombres de documentos en tu respuesta, se añadirán aparte.
			CONTEXTO:
			%%s
			""".formatted(SIN_INFORMACION);

	private final VectorStore vectorStore;
	private final ChatClient chatClient;

	public TutorService(VectorStore vectorStore, ChatClient.Builder chatClientBuilder) {
		this.vectorStore = vectorStore;
		this.chatClient = chatClientBuilder.build();
	}

	public RespuestaTutor explicar(String tema) {
		List<Document> chunks = vectorStore.similaritySearch(SearchRequest.builder()
				.query(tema)
				.topK(5)
				.similarityThreshold(0.5)
				.build());
		if (chunks.isEmpty()) {
			return new RespuestaTutor(SIN_INFORMACION, List.of());
		}
		String contexto = String.join("\n\n---\n\n", chunks.stream().map(Document::getText).toList());
		String explicacion = chatClient.prompt()
				.system(system.formatted(contexto))
				.user("Explícame: " + tema)
				.call()
				.content();
		//las fuentes salen de la metadata de los chunks recuperados, no del modelo
		List<String> fuentes = chunks.stream()
				.map(d -> d.getMetadata().get("file_name").toString())
				.distinct()
				.toList();
		return new RespuestaTutor(explicacion, fuentes);
	}
}
