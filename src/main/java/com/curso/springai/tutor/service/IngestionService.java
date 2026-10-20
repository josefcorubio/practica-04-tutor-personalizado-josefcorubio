package com.curso.springai.tutor.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import com.curso.springai.tutor.components.PdfLoaderComponent;

@Service
public class IngestionService {
	private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

	private final PdfLoaderComponent pdfLoaderComponent;
	private final VectorStore vectorStore;

	public IngestionService(PdfLoaderComponent pdfLoaderComponent, VectorStore vectorStore) {
		this.pdfLoaderComponent = pdfLoaderComponent;
		this.vectorStore = vectorStore;
	}

	public int ingestarDocumentos() {
		int total = 0;
		for (String fileName : pdfLoaderComponent.listarPdfs()) {
			List<Document> chunks = pdfLoaderComponent.obtenerChunks(fileName);
			vectorStore.delete("file_name == '" + fileName.replace("'", "\\'") + "'");
			if (!chunks.isEmpty()) {
				vectorStore.add(chunks);
			}
			log.info("Ingestado {}: {} chunks", fileName, chunks.size());
			total += chunks.size();
		}
		return total;
	}

	public void eliminarDocumentos() {
		vectorStore.delete("file_name != ''");
	}
}
