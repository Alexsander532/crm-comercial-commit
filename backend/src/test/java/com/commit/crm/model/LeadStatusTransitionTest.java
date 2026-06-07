package com.commit.crm.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LeadStatusTransitionTest {

    @Test
    void shouldAllowNovoToContato() {
        assertTrue(LeadStatus.NOVO.canTransitionTo(LeadStatus.CONTATO));
    }

    @Test
    void shouldAllowNovoToArquivado() {
        assertTrue(LeadStatus.NOVO.canTransitionTo(LeadStatus.ARQUIVADO));
    }

    @Test
    void shouldNotAllowNovoToNegociacao() {
        assertFalse(LeadStatus.NOVO.canTransitionTo(LeadStatus.NEGOCIACAO));
    }

    @Test
    void shouldNotAllowNovoToGanho() {
        assertFalse(LeadStatus.NOVO.canTransitionTo(LeadStatus.GANHO));
    }

    @Test
    void shouldAllowContatoToNegociacao() {
        assertTrue(LeadStatus.CONTATO.canTransitionTo(LeadStatus.NEGOCIACAO));
    }

    @Test
    void shouldAllowContatoToNovo() {
        assertTrue(LeadStatus.CONTATO.canTransitionTo(LeadStatus.NOVO));
    }

    @Test
    void shouldAllowNegociacaoToGanho() {
        assertTrue(LeadStatus.NEGOCIACAO.canTransitionTo(LeadStatus.GANHO));
    }

    @Test
    void shouldAllowNegociacaoToPerdido() {
        assertTrue(LeadStatus.NEGOCIACAO.canTransitionTo(LeadStatus.PERDIDO));
    }

    @Test
    void shouldAllowNegociacaoToContato() {
        assertTrue(LeadStatus.NEGOCIACAO.canTransitionTo(LeadStatus.CONTATO));
    }

    @Test
    void shouldNotAllowTerminalStatesToTransition() {
        assertFalse(LeadStatus.GANHO.canTransitionTo(LeadStatus.NOVO));
        assertFalse(LeadStatus.GANHO.canTransitionTo(LeadStatus.ARQUIVADO));
        assertFalse(LeadStatus.PERDIDO.canTransitionTo(LeadStatus.NOVO));
        assertFalse(LeadStatus.PERDIDO.canTransitionTo(LeadStatus.ARQUIVADO));
    }

    @Test
    void shouldAllowArquivadoToNovo() {
        assertTrue(LeadStatus.ARQUIVADO.canTransitionTo(LeadStatus.NOVO));
    }

    @Test
    void shouldIdentifyTerminalStates() {
        assertTrue(LeadStatus.GANHO.isTerminal());
        assertTrue(LeadStatus.PERDIDO.isTerminal());
        assertFalse(LeadStatus.NOVO.isTerminal());
        assertFalse(LeadStatus.ARQUIVADO.isTerminal());
    }
}
