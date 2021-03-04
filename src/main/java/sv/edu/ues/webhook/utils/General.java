package sv.edu.ues.webhook.utils;

import java.util.Map;

public class General {

    public static final Map<String, Integer> STATUSES = Map.of("pendiente", 1, "proceso", 2, "completado", 3, "rechazado", 4);
    public static final String[] STATUS_OPTIONS = {"Pendiente", "En proceso", "Completado", "Rechazado"};
    public static final String[] AREA_OPTIONS =
            {"Ingeniería", "Arquitectura", "Ciencias sociales", "Filosofía y letras", "Idiomas", "Medicina",
                    "Ciencias jurídicas", "Ciencias económicas", "Química", "Biología", "Física", "Matemática", "General"};
    
    public static final String[] DOCUMENT_OPTIONS =
            {"Formularios", "Carta para tutores", "Carta de compromiso", "Declaración jurada", "Cambio de tutor"};
}
