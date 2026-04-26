package com.badbones69.crazycrates.paper.api.objects.gacha.util;

import com.badbones69.crazycrates.paper.api.objects.Crate;
import com.badbones69.crazycrates.paper.api.objects.gacha.data.CrateSettings;
import com.badbones69.crazycrates.paper.tasks.crates.CrateManager;

import java.util.*;

public class LinkedCrates {
    public static void detect(CrateManager crateManager) {
        final Map<String, List<String>> linkGraph = new HashMap<>();

        for (final Crate crate : crateManager.getCrates()) {
            final String source = crate.getFileName();
            final List<String> linkedNames = Optional.ofNullable(crate.getCrateSettings())
                    .map(CrateSettings::getLinkedCrates)
                    .orElse(Collections.emptyList());

            final List<String> resolved = new ArrayList<>();

            for (final String linked : linkedNames) {
                String candidate = linked;
                Crate target = crateManager.getCrateFromName(candidate);

                // Try with .yml extension if not found
                if (target == null && !candidate.endsWith(".yml")) {
                    candidate = candidate + ".yml";
                    target = crateManager.getCrateFromName(candidate);
                }

                if (target == null) {
                    System.out.printf("Crate %s has invalid linked crate name: %s", source, linked);
                    continue;
                }

                resolved.add(target.getFileName());
            }

            linkGraph.put(source, resolved);
        }

        final Set<String> visited = new HashSet<>();
        final Set<String> stackSet = new HashSet<>();
        final Deque<String> stack = new ArrayDeque<>();
        final List<List<String>> cycles = new ArrayList<>();

        for (final String node : linkGraph.keySet()) {
            if (!visited.contains(node)) {
                dfsDetect(node, linkGraph, visited, stackSet, stack, cycles);
            }
        }

        for (final List<String> cycle : cycles) {
            System.out.printf("Linked crates cycle detected: %s", String.join(" -> ", cycle));
        }
    }


    private static void dfsDetect(final String node,
                                  final Map<String, List<String>> graph,
                                  final Set<String> visited,
                                  final Set<String> inStack,
                                  final Deque<String> path,
                                  final List<List<String>> cycles) {
        visited.add(node);
        inStack.add(node);
        path.push(node);

        final List<String> neighbors = graph.getOrDefault(node, Collections.emptyList());
        for (final String neigh : neighbors) {
            if (!visited.contains(neigh)) {
                dfsDetect(neigh, graph, visited, inStack, path, cycles);
            } else if (inStack.contains(neigh)) {
                final List<String> cycle = new ArrayList<>();
                for (String n : path) {
                    cycle.add(n);
                    if (n.equals(neigh)) break;
                }
                Collections.reverse(cycle);
                cycles.add(cycle);
            }
        }

        path.pop();
        inStack.remove(node);
    }
}
