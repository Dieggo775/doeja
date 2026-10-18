package com.doeja.util;

/**
 * Cálculos geográficos simples, sem dependências (fáceis de testar).
 */
public final class GeoUtils {

    private static final double RAIO_TERRA_KM = 6371.0088;

    private GeoUtils() {
    }

    /** Retângulo de coordenadas (em graus) que contém um círculo. */
    public record Limites(double minLat, double maxLat, double minLng, double maxLng) {
    }

    /** Distância em km entre dois pontos da superfície da Terra (fórmula de Haversine). */
    public static double distanciaKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * RAIO_TERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(a)));
    }

    /**
     * Caixa de coordenadas que contém TODOS os pontos a até raioKm do centro.
     * Serve para o banco descartar rápido o que está longe, antes do cálculo exato.
     * Não trata a virada do meridiano 180 (irrelevante para o Brasil): só limita a [-180, 180].
     */
    public static Limites caixa(double lat, double lng, double raioKm) {
        double raioAngular = raioKm / RAIO_TERRA_KM;
        double deltaLat = Math.toDegrees(raioAngular);
        double cosLat = Math.cos(Math.toRadians(lat));
        double razao = Math.sin(raioAngular) / cosLat;
        double deltaLng = (cosLat <= 1e-9 || razao >= 1.0) ? 180.0 : Math.toDegrees(Math.asin(razao));

        return new Limites(
                Math.max(-90.0, lat - deltaLat),
                Math.min(90.0, lat + deltaLat),
                Math.max(-180.0, lng - deltaLng),
                Math.min(180.0, lng + deltaLng));
    }
}
