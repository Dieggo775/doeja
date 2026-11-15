package com.doeja.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.doeja.entity.CentroDoacao;

class CentroDoacaoMapperTest {

    @Test
    void cepSempreNoFormatoComHifen() {
        assertEquals("06763-015", CentroDoacaoMapper.formatarCep("06763015"));
        assertEquals("06763-015", CentroDoacaoMapper.formatarCep(" 06763-015 "));
        assertNull(CentroDoacaoMapper.formatarCep(""));
        assertNull(CentroDoacaoMapper.formatarCep(null));
    }

    @Test
    void copiarLimpaEspacosEPadronizaUf() {
        CentroDoacaoRequestDTO req = new CentroDoacaoRequestDTO(
                "  Centro Teste ", " ", "Rua A, 1", "Centro", "Taboão da Serra", "sp", "06750000",
                "(11) 90000-0000", "", "Seg a Sex", -23.6, -46.7);
        CentroDoacao centro = new CentroDoacao();

        CentroDoacaoMapper.copiar(req, centro);

        assertEquals("Centro Teste", centro.getNome());
        assertNull(centro.getDescricao());
        assertEquals("SP", centro.getEstado());
        assertEquals("06750-000", centro.getCep());
        assertNull(centro.getWhatsapp());
    }
}
