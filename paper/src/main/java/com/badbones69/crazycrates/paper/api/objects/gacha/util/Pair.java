package com.badbones69.crazycrates.paper.api.objects.gacha.util;

import java.io.Serial;
import java.io.Serializable;

public record Pair<A, B>(A first, B second) implements Serializable {
    @Serial
    private static final long serialVersionUID = 598769154417553L;
}
