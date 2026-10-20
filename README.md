# Práctica 4: Tutor personalizado

## Enunciado

Partiendo de una serie de tutoriales en formato PDF (Incluidos en el repositorio), desarrolla una aplicación RAG que, a partir de una temática/tecnología proporcionada por el usuario, le responda con una explicación sobre ese concepto o tecnología.

Se utilizará como fuente de información única y exclusivamente los documentos proporcionados. Además de la explicación, en la respuesta se incluirán los nombres de los documentos de los que se ha obtenido esta información para que el usuario pueda profundizar sobre ello.

**Desarrollo de la práctica**: al finalizar el Tema 6.

## Uso del servicio

Requisitos: Ollama con `llama3` y `bge-m3`, y Docker para Qdrant.

```bash
docker compose up -d       # Qdrant
./mvnw spring-boot:run     # http://localhost:8004
```

| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/rag/ingest` | Indexa los PDFs de `docs/` en Qdrant (se puede repetir sin duplicar). |
| DELETE | `/rag/clear` | Borra todo lo indexado. |
| GET | `/tutor/explicar?tema=...` | Explica el tema y lista los PDFs usados. |

```bash
curl -X POST http://localhost:8004/rag/ingest
curl -G http://localhost:8004/tutor/explicar --data-urlencode "tema=Qué es la arquitectura hexagonal"
```

Respuesta: `{"explicacion": "...", "fuentes": ["arquitectura hexagonal.pdf"]}`.
Si el tema no está en los documentos, `fuentes` viene vacío. Errores: `400` (tema vacío o de más de 300 caracteres) y `503` (Qdrant u Ollama caídos).

## Cómo empezar

1. Pulsa el botón **`Use this template`** (arriba a la derecha de este repositorio) → **`Create a new repository`**.
2. Elige **tu cuenta personal** como propietario (no la organización del curso).
3. Nombra tu repositorio como: `practica-04-tutor-personalizado-<tu-nombre>`.
4. Clona tu nuevo repositorio y desarrolla la práctica sobre él.
5. Haz commits descriptivos a medida que avanzas.

## Entrega

Copia el enlace de tu repositorio y pégalo en la entrega correspondiente de la plataforma.

Este ejercicio es voluntario y está pensado para que pongas en práctica los conocimientos adquiridos y consolides lo aprendido en los módulos teóricos.

- 📌 La entrega de estos ejercicios es completamente opcional y no influye en la nota final del curso.
- 📌 Si decides realizarlos, el profesor los corregirá y te dará feedback personalizado sobre tu trabajo.
- 📌 Estos ejercicios tampoco afectan a la bonificación del curso a través de Fundae.
- 📌 En caso de completar los ejercicios voluntarios y obtener una calificación igual o superior a 5, TrainingIT emitirá un certificado adicional e independiente al de Fundae, reflejando una evaluación positiva de las acciones prácticas realizadas durante el curso.
