import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final HashMap<String, Document> documents;

    public DocumentManager() {
        documents = new HashMap<>();
    }

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document cannot be null");
        }

        String id = (document.getId() == null || document.getId().isEmpty())
                ? UUID.randomUUID().toString()
                : document.getId();

        Document savedDocument = Document.builder()
                .id(id)
                .title(document.getTitle())
                .author(Author.builder()
                        .id(document.getAuthor().getName())
                        .name(document.getAuthor().getName()
                        ).build())
                .content(document.getContent())
                .created(document.getCreated())
                .build();

        documents.put(id, savedDocument);
        return savedDocument;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request != null) {
            return documents.values().stream()
                    .filter(document -> matchesTitle(document, request.getTitlePrefixes()))
                    .filter(document -> matchesContent(document, request.getContainsContents()))
                    .filter(document -> matchesAuthorId(document, request.getAuthorIds()))
                    .filter(document -> matchesCreatedDate(
                            document,
                            request.getCreatedFrom(),
                            request.getCreatedTo())
                    ).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id));
    }

    private boolean matchesTitle(Document document, List<String> prefixes) {
        if (prefixes == null || prefixes.isEmpty()) return true;
        return prefixes.stream().anyMatch(prefix -> document.getTitle().startsWith(prefix));
    }

    private boolean matchesContent(Document document, List<String> contents) {
        if (contents == null || contents.isEmpty()) return true;
        return contents.stream().anyMatch(content -> document.getContent().contains(content));
    }

    private boolean matchesAuthorId(Document document, List<String> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) return true;
        return authorIds.stream().anyMatch(authorId -> document.getAuthor().getId().equals(authorId));
    }

    private boolean matchesCreatedDate(Document document, Instant from, Instant to) {
        Instant created = document.getCreated();
        if (from != null && created.isBefore(from)) {
            return false;
        }
        if (to != null && created.isAfter(to)) {
            return false;
        }
        return true;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}