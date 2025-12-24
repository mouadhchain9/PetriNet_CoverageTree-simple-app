package com.svs.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Marking {

    private final List<Integer> tokens;   // -1 represents ω
    private final Marking parent;         // null for root

    public Marking(List<Integer> tokens, Marking parent) {
        this.tokens = tokens;
        this.parent = parent;
    }

    public List<Integer> getTokens() {
        return tokens;
    }

    public Marking getParent() {
        return parent;
    }

    public boolean isOmega(int index) {
        return tokens.get(index) == -1;
    }

    public int getToken(int index) {
        return tokens.get(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        for (int t : tokens) {
            sb.append(t == -1 ? "ω" : t).append(" ");
        }
        return sb.append("}").toString();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Marking)) return false;
        Marking m = (Marking) o;
        return tokens.equals(m.tokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokens);
    }
}
