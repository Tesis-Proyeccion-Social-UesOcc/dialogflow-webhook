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

    public static void buildForProjectInfo(JsonNode node, StringBuilder builder, String carnet, boolean many){

        var tempBuilder = new StringBuilder();
        tempBuilder.append(node.get("nombre").asText()).append("\n\n")
                .append("Duracion: ").append(node.get("duracion")).append(" horas\n")
                .append(node.get("interno").asBoolean()? "Proyecto de tipo interno\n":"Proyecto de tipo externo\n")
                .append("Tutor: ").append(node.get("personal").asText()).append("\n");
        tempBuilder.append("Estudiantes en el proyecto: ");

        var flag = false;
        for(var estudiantes: node.get("estudiantes")){
            var isActive = estudiantes.get("active").asBoolean();
            var responseCarnet = estudiantes.get("carnet").asText();

            if(responseCarnet.equalsIgnoreCase(carnet) && !isActive){
                tempBuilder.setLength(0);  // clears the builder
                if(many){
                    tempBuilder.append(node.get("nombre").asText()).append("\n\n");
                }
                tempBuilder.append("Estudiante actualmente retirado del proyecto, debe tramitar su nueva incorporación a este con los encargados de su sub-unidad");
                builder.append(tempBuilder);
                return;
            }

            if(flag)
                tempBuilder.append(", ");

            tempBuilder.append(responseCarnet);

            if(!isActive)
                tempBuilder.append(" (retirado del proyecto)");

            flag = true;
        }

        var docs = node.get("documentos");
        if(docs != null) {
            if(!docs.isEmpty()) {
                tempBuilder.append("\n");
                tempBuilder.append("Estado de los documentos requeridos:\n");
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
                    tempBuilder.append(formattedStr);

                }
            }
        }
        tempBuilder.append("\n\n");
        builder.append(tempBuilder);
    }

}
