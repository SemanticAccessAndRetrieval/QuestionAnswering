package gr.forth.ics.isl.nlp.externalTools.models;

import com.google.gson.annotations.SerializedName;
import static gr.forth.ics.isl.nlp.externalTools.models.Constants.DBPEDIA;
import static gr.forth.ics.isl.nlp.externalTools.models.Constants.HTTP;
import static gr.forth.ics.isl.nlp.externalTools.models.Constants.SCHEMA;
import static gr.forth.ics.isl.nlp.externalTools.models.Prefixes.DBPEDIA_ONTOLOGY;
import static gr.forth.ics.isl.nlp.externalTools.models.Prefixes.SCHEMA_ONTOLOGY;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnnotationUnit {

    @SerializedName("@text")
    private String text;

    @SerializedName("@confidence")
    private String confidence;

    @SerializedName("@support")
    private String support;

    @SerializedName("@types")
    private String types;

    @SerializedName("@sparql")
    private String sparql;

    @SerializedName("@policy")
    private String policy;

    @SerializedName("Resources")
    private List<ResourceItem> resources;

    public Integer endIndex() {
        if (text != null) {
            return text.length();
        }
        return 0;
    }

    public String getTypes() {
        if (types != null && !types.isEmpty()) {
            return types.replace("Http", HTTP).
                    replace(DBPEDIA, DBPEDIA_ONTOLOGY).
                    replace(SCHEMA, SCHEMA_ONTOLOGY);
        }
        return types;
    }

    public Integer beginIndex() {
        return 1;
    }
}