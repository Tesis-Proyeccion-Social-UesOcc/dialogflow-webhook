package sv.edu.ues.webhook.utils;

import java.util.Map;

public class General {

    public static final Map<String, Integer> STATUSES = Map.of("pendiente", 1, "proceso", 2, "completado", 3, "rechazado", 4);
    public static final String[] AREAS = {"Ingenieria y arquitectura", "Ciencias sociales, filosofia y letras", "Idiomas", "Medicina",
                                        "Ciencias juridicas", "Ciencias economicas", "Quimica", "Biologia", "Fisica", "Matematica"};
}
