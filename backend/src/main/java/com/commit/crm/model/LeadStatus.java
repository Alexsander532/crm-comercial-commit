package com.commit.crm.model;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public enum LeadStatus {
    NOVO,
    CONTATO,
    NEGOCIACAO,
    GANHO,
    PERDIDO,
    ARQUIVADO;

    private static final Map<LeadStatus, Set<LeadStatus>> TRANSITIONS = new EnumMap<>(LeadStatus.class);

    static {
        TRANSITIONS.put(NOVO, Set.of(CONTATO, ARQUIVADO));
        TRANSITIONS.put(CONTATO, Set.of(NEGOCIACAO, NOVO, ARQUIVADO));
        TRANSITIONS.put(NEGOCIACAO, Set.of(GANHO, PERDIDO, CONTATO, ARQUIVADO));
        TRANSITIONS.put(GANHO, Set.of());
        TRANSITIONS.put(PERDIDO, Set.of());
        TRANSITIONS.put(ARQUIVADO, Set.of(NOVO));
    }

    public boolean canTransitionTo(LeadStatus target) {
        return TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isTerminal() {
        return this == GANHO || this == PERDIDO;
    }
}
