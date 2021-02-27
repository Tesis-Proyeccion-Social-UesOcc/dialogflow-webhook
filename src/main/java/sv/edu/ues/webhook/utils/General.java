package sv.edu.ues.webhook.utils;

import java.util.Map;

public class General {

    public static final Map<String, Integer> STATUSES = Map.of("pendiente", 1, "proceso", 2, "completado", 3, "rechazado", 4);
    public static final String[] STATUS_OPTIONS = {"Pendiente", "En proceso", "Completado", "Rechazado"};
    public static final String[] AREA_OPTIONS =
            {"Ingenieria y arquitectura", "Ciencias sociales, filosofia y letras", "Idiomas", "Medicina",
                    "Ciencias juridicas", "Ciencias economicas", "Quimica", "Biologia", "Fisica", "Matematica", "General"};
    
    public static final String[] DOCUMENT_OPTIONS =
            {"Formularios generales", "Carta para tutores", "Carta de compromiso", "Declaración jurada", "Solicitud de cambio de tutor"};
}
