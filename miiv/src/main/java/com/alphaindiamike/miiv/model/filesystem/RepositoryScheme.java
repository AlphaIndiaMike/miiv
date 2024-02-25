package com.alphaindiamike.miiv.model.filesystem;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryScheme {
	@JsonProperty("name")
    private String name;
	@JsonProperty("schema_version_major")
    private Integer schema_version_major;
	@JsonProperty("schema_version_minor")
    private Integer schema_version_minor;
	@JsonProperty("purpose")
    private String purpose;
	@JsonProperty("preferredFileTypes")
    private List<String> preferredFileTypes;
	@JsonProperty("children")
    private List<RepositoryScheme> children;
	@JsonProperty("content")
    private ContentRoutine content; // Optional: Not all nodes have this field.
	@JsonProperty("template")
    private Template template; // Optional: Representing additional rules.
	@JsonProperty("type")
    private List<String> type;; // Optional: Representing the parameter types

    // Constructors
    public RepositoryScheme() {
    }

    // Getters
    public String getName() {
        return name;
    }

    public Integer getSchemaVersionMajor() {
        return schema_version_major;
    }

    public Integer getSchemaVersionMinor() {
        return schema_version_minor;
    }

    public String getPurpose() {
        return purpose;
    }

    public List<String> getPreferredFileTypes() {
        return preferredFileTypes;
    }

    public List<RepositoryScheme> getChildren() {
        return children;
    }

    public Template getTemplate() {
        return template;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setSchemaVersionMajor(Integer schema_version_major) {
        this.schema_version_major = schema_version_major;
    }

    public void setSchemaVersionMinor(Integer schema_version_minor) {
        this.schema_version_minor = schema_version_minor;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public void setPreferredFileTypes(List<String> preferredFileTypes) {
        this.preferredFileTypes = preferredFileTypes;
    }

    public void setChildren(List<RepositoryScheme> children) {
        this.children = children;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }
    
    public ContentRoutine getContent() {
        return content;
    }

    public void setContent(ContentRoutine content) {
        this.content = content;
    }
    
    public static ContentRoutine parseContentRoutine(String content) {
        switch (content) {
            case "version_controlled_by_date":
                return ContentRoutine.VERSION_CONTROLLED_BY_DATE;
            case "version_controlled_by_date_and_type":
                return ContentRoutine.VERSION_CONTROLLED_BY_DATE_AND_TYPE;
            case "version_controlled_by_date_and_title":
                return ContentRoutine.VERSION_CONTROLLED_BY_DATE_AND_TITLE;
            default:
                throw new IllegalArgumentException("Unknown content routine: " + content);
        }
    }


    /**
     * Inner class to represent the template object for nodes that have specific structuring rules
     * like "version_controlled_by_date_and_type" and a list of types for categorization.
     */
    public static class Template {
        private List<String> type;

        public Template() {
        }

        public List<String> getType() {
            return type;
        }

        public void setType(List<String> type) {
            this.type = type;
        }
    }
}
