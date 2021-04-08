package sv.edu.ues.webhook.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public class PayloadBuilder {

    public static Map<String, Object> buildForAttachment(JsonNode url){
        return Map.of("facebook",
                    Map.of("attachment",
                            Map.of("type", "file", "payload",
                                    Map.of("url", url))));
    }

    public static void buildForProjectInfo(JsonNode node, StringBuilder builder){
        builder.append(node.get("nombre").asText()).append("\n")
                .append("Duracion: ").append(node.get("duracion")).append(" horas\n")
                .append(node.get("interno").asBoolean()? "Proyecto de tipo interno\n":"Proyecto de tipo externo\n")
                .append("Tutor: ").append(node.get("personal").asText()).append("\n");
        builder.append("Estudiantes en el proyecto: ");
        var flag = false;
        for(var estudiantes: node.get("estudiantes")){
            if(flag) builder.append(", ");
            builder.append(estudiantes.get("carnet").asText());
            flag = true;
        }
        var docs = node.get("documentos");
        if(docs != null) {
            if(!docs.isEmpty()) {
                builder.append("\n");
                builder.append("Estado de los documentos requeridos:\n");
                for (var documento : docs) {
                    var msg1 = "";
                    if(documento.get("entregado").asBoolean()) {
                        var msg2 = documento.get("aprobado").asBoolean() ? "aprobado" : "sin aprobar";
                        msg1 = "entregado y "+msg2 ;
                    }
                    else msg1 = "sin entregar";

                    var formattedStr =
                            String.format("- %s, %s%n",
                                    documento.get("nombre").asText().toUpperCase(),
                                    msg1);
                    builder.append(formattedStr);

                }
            }
        }
        builder.append("\n\n");
    }

}
