package com.doeja.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class GeoUtilsTest {

    @Test
    void distanciaDoMesmoPontoEZero() {
        assertEquals(0.0, GeoUtils.distanciaKm(-23.6256, -46.7918, -23.6256, -46.7918), 1e-9);
    }

    @Test
    void umGrauDeLatitudeTemCercaDe111Km() {
        assertEquals(111.19, GeoUtils.distanciaKm(0, 0, 1, 0), 0.05);
    }

    @Test
    void distanciaSaoPauloRioDeJaneiro() {
        // Praça da Sé (SP) até o centro do Rio: cerca de 360 km em linha reta
        double km = GeoUtils.distanciaKm(-23.5505, -46.6333, -22.9068, -43.1729);
        assertEquals(360.7, km, 0.5);
    }

    @Test
    void distanciaEntreDoisCentrosDeTaboaoDaSerra() {
        double km = GeoUtils.distanciaKm(-23.6256, -46.7918, -23.6122, -46.7805);
        assertEquals(1.88, km, 0.05);
    }

    @Test
    void caixaContemTodosOsPontosDentroDoRaio() {
        Random random = new Random(42);
        for (int i = 0; i < 300_000; i++) {
            double lat = -60 + random.nextDouble() * 120;      // de -60 a 60
            double lng = -170 + random.nextDouble() * 340;     // evita a virada do meridiano 180
            double raio = 0.5 + random.nextDouble() * 49.5;    // de 0,5 a 50 km

            GeoUtils.Limites caixa = GeoUtils.caixa(lat, lng, raio);

            // ponto aleatório numa região de ±1 grau ao redor do centro
            double pLat = lat + (random.nextDouble() * 2 - 1);
            double pLng = lng + (random.nextDouble() * 2 - 1);

            if (GeoUtils.distanciaKm(lat, lng, pLat, pLng) <= raio) {
                boolean dentroDaCaixa = pLat >= caixa.minLat() && pLat <= caixa.maxLat()
                        && pLng >= caixa.minLng() && pLng <= caixa.maxLng();
                assertTrue(dentroDaCaixa, "ponto a menos de " + raio + " km ficou fora da caixa: "
                        + lat + "," + lng + " -> " + pLat + "," + pLng);
            }
        }
    }

    @Test
    void caixaNuncaPassaDosLimitesDoGlobo() {
        GeoUtils.Limites polo = GeoUtils.caixa(89.9, 10.0, 50.0);
        assertTrue(polo.maxLat() <= 90.0);
        assertTrue(polo.minLng() >= -180.0 && polo.maxLng() <= 180.0);
    }
}
