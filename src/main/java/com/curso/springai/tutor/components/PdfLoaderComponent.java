package com.curso.springai.tutor.components;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PdfLoaderComponent {
	//caracteres aproximados que se agrupan de páginas consecutivas antes de trocear
	private static final int CARACTERES_POR_BLOQUE = 3000;

	private final Path rootPath;
	private final TokenTextSplitter splitter;

	public PdfLoaderComponent(@Value("${app.docs.path}") String rootPath) {
		this.rootPath = Path.of(rootPath);
		this.splitter = TokenTextSplitter.builder()
				.withChunkSize(600) //tamaño en tokens de cada chunk
				.withMaxNumChunks(100000) //sin límite práctico de chunks por documento
				.build();
	}

	//nombres de los pdf disponibles en la carpeta de documentos
	public List<String> listarPdfs() {
		try (Stream<Path> paths = Files.walk(rootPath)) {
			return paths
					.filter(Files::isRegularFile)
					.filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
					.map(p -> p.getFileName().toString())
					.sorted()
					.toList();
		} catch (IOException e) {
			throw new RuntimeException("Error listando PDFs en " + rootPath, e);
		}
	}

	//devuelve los chunks de un pdf con metadatos file_name, page_start y page_end
	public List<Document> obtenerChunks(String fileName) {
		Path pdf = rootPath.resolve(fileName);
		return splitter.split(agruparPaginas(pdf));
	}

	//lee el pdf página a página y agrupa páginas consecutivas en bloques de texto
	private List<Document> agruparPaginas(Path pdfPath) {
		String fileName = pdfPath.getFileName().toString();
		List<Document> bloques = new ArrayList<>();
		try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
			PDFTextStripper stripper = new PDFTextStripper();
			StringBuilder buffer = new StringBuilder();
			int inicio = 1;
			for (int pagina = 1; pagina <= doc.getNumberOfPages(); pagina++) {
				stripper.setStartPage(pagina);
				stripper.setEndPage(pagina);
				String texto = stripper.getText(doc).trim();
				if (texto.isEmpty()) {
					continue;
				}
				if (buffer.isEmpty()) {
					inicio = pagina;
				}
				buffer.append(texto).append("\n\n");
				if (buffer.length() >= CARACTERES_POR_BLOQUE) {
					bloques.add(crearBloque(buffer, fileName, inicio, pagina));
					buffer.setLength(0);
				}
			}
			if (!buffer.isEmpty()) {
				bloques.add(crearBloque(buffer, fileName, inicio, doc.getNumberOfPages()));
			}
		} catch (IOException e) {
			throw new RuntimeException("Error leyendo PDF: " + pdfPath, e);
		}
		return bloques;
	}

	private Document crearBloque(StringBuilder texto, String fileName, int inicio, int fin) {
		return new Document(texto.toString(),
				Map.of("file_name", fileName, "page_start", inicio, "page_end", fin));
	}
}
