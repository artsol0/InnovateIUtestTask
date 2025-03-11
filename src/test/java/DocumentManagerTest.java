import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();

        documentManager.save(DocumentManager.Document.builder()
                .id("1")
                .title("Doc1")
                .author(DocumentManager.Author.builder().id("Author1").name("Author1").build())
                .content("Content1")
                .created(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(3600))
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .id("2")
                .title("Doc2")
                .author(DocumentManager.Author.builder().id("Author2").name("Author2").build())
                .content("Content2")
                .created(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(3600))
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .id("3")
                .title("Doc3")
                .author(DocumentManager.Author.builder().id("Author1").name("Author1").build())
                .content("Content3")
                .created(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(7200))
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .id("4")
                .title("Doc4")
                .author(DocumentManager.Author.builder().id("Author4").name("Author4").build())
                .content("Content4")
                .created(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(5600))
                .build());
    }

    @Test
    void shouldGenerateIdWhenSavingNewDocument() {
        // Given
        DocumentManager.Document newDocument = DocumentManager.Document.builder()
                .title("New Document")
                .author(DocumentManager.Author.builder().name("John Doe").build())
                .content("Sample content")
                .created(Instant.now())
                .build();

        // When
        DocumentManager.Document savedDocument = documentManager.save(newDocument);

        // Then
        assertNotNull(savedDocument.getId(), "ID should be generated for a new document");
        assertEquals(newDocument.getTitle(), savedDocument.getTitle());
        assertEquals(newDocument.getContent(), savedDocument.getContent());
        assertEquals(newDocument.getCreated(), savedDocument.getCreated());
    }

    @Test
    void shouldPreserveIdWhenSavingExistingDocument() {
        // Given
        String existingId = UUID.randomUUID().toString();
        DocumentManager.Document existingDocument = DocumentManager.Document.builder()
                .id(existingId)
                .title("Existing Document")
                .author(DocumentManager.Author.builder().name("Alice").build())
                .content("Existing content")
                .created(Instant.now())
                .build();

        // When
        DocumentManager.Document savedDocument = documentManager.save(existingDocument);

        // Then
        assertEquals(existingId, savedDocument.getId(), "ID should be preserved for an existing document");
        assertEquals(existingDocument.getTitle(), savedDocument.getTitle());
        assertEquals(existingDocument.getCreated(), savedDocument.getCreated());
    }

    @Test
    void shouldReturnDocumentsThatMatchTitle() {
        // Given
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Doc1"))
                .build();

        // When
        List<DocumentManager.Document> result = documentManager.search(request);

        // Then
        assertEquals(1, result.size(), "There should be exactly 1 document matching the title prefix");
        assertEquals("Doc1", result.get(0).getTitle(), "The title should match the prefix");
    }

    @Test
    void shouldReturnDocumentsThatMatchContent() {
        // Given
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .containsContents(List.of("Content2"))
                .build();

        // When
        List<DocumentManager.Document> result = documentManager.search(request);

        // Then
        assertEquals(1, result.size(), "There should be exactly 1 document matching the content");
        assertEquals("Content2", result.get(0).getContent(), "The content should match the search term");
    }

    @Test
    void shouldReturnDocumentsThatMatchAuthorId() {
        // Given
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of("Author1"))
                .build();

        // When
        List<DocumentManager.Document> result = documentManager.search(request);

        // Then
        assertEquals(2, result.size(), "There should be exactly 2 documents with Author1");
    }

    @Test
    void shouldReturnDocumentsWithinCreatedDateRange() {
        // Given
        Instant now = Instant.now();
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(7199))
                .createdTo(Instant.parse("2025-03-11T00:00:00Z").minusSeconds(3601))
                .build();

        // When
        List<DocumentManager.Document> result = documentManager.search(request);

        // Then
        assertEquals(1, result.size(), "There should be exactly 1 document created within the time range");
    }

    @Test
    void shouldReturnEmptyListWhenNoDocumentsMatch() {
        // Given
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("NonExistent"))
                .build();

        // When
        List<DocumentManager.Document> result = documentManager.search(request);

        // Then
        assertTrue(result.isEmpty(), "The result should be empty when no documents match the search criteria");
    }

    @Test
    void testFindById_ExistingDocument_ReturnsDocument() {
        // Given
        String documentId = "1";

        // When
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(documentId);

        // Then
        assertTrue(foundDocument.isPresent(), "Document should be present");
        assertEquals("Doc1", foundDocument.get().getTitle(), "The title of the found document should be Doc1");
    }

    @Test
    void testFindById_NonExistingDocument_ReturnsEmpty() {
        // Given
        String documentId = "999";  // Non-existing ID

        // When
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(documentId);

        // Then
        assertFalse(foundDocument.isPresent(), "Document should not be found for a non-existing ID");
    }
}