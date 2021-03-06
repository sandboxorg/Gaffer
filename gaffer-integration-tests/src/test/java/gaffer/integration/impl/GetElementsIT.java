/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gaffer.integration.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import gaffer.commonutil.TestGroups;
import gaffer.data.element.Edge;
import gaffer.data.element.Element;
import gaffer.data.element.Entity;
import gaffer.data.elementdefinition.view.View;
import gaffer.integration.AbstractStoreIT;
import gaffer.operation.GetOperation.IncludeEdgeType;
import gaffer.operation.GetOperation.IncludeIncomingOutgoingType;
import gaffer.operation.GetOperation.SeedMatchingType;
import gaffer.operation.OperationException;
import gaffer.operation.data.EdgeSeed;
import gaffer.operation.data.ElementSeed;
import gaffer.operation.data.EntitySeed;
import gaffer.operation.impl.get.GetElements;
import gaffer.operation.impl.get.GetElementsSeed;
import gaffer.operation.impl.get.GetRelatedElements;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GetElementsIT extends AbstractStoreIT {
    // ElementSeed Seeds
    protected static final List<ElementSeed> ENTITY_SEEDS_EXIST =
            Arrays.asList(
                    (ElementSeed) new EntitySeed(SOURCE_2),
                    new EntitySeed(DEST_3),
                    new EntitySeed(SOURCE_DIR_2),
                    new EntitySeed(DEST_DIR_3));

    protected static final List<Element> ENTITIES_EXIST =
            getElements(ENTITY_SEEDS_EXIST);

    protected static final List<ElementSeed> EDGE_SEEDS_EXIST =
            Collections.singletonList(
                    (ElementSeed) new EdgeSeed(SOURCE_1, DEST_1, false));

    protected static final List<Element> EDGES_EXIST =
            getElements(EDGE_SEEDS_EXIST);

    protected static final List<ElementSeed> EDGE_DIR_SEEDS_EXIST =
            Collections.singletonList(
                    (ElementSeed) new EdgeSeed(SOURCE_DIR_1, DEST_DIR_1, true));

    protected static final List<Element> EDGES_DIR_EXIST =
            getElements(EDGE_DIR_SEEDS_EXIST);

    protected static final List<ElementSeed> EDGE_SEEDS_DONT_EXIST =
            Arrays.asList(
                    (ElementSeed) new EdgeSeed(SOURCE_1, "dest2DoesNotExist", false),
                    new EdgeSeed("source2DoesNotExist", DEST_1, false),
                    new EdgeSeed(SOURCE_1, DEST_1, true));// does not exist

    protected static final List<ElementSeed> ENTITY_SEEDS_DONT_EXIST =
            Collections.singletonList(
                    (ElementSeed) new EntitySeed("idDoesNotExist"));

    protected static final List<ElementSeed> ENTITY_SEEDS = getEntitySeeds();
    protected static final List<ElementSeed> EDGE_SEEDS = getEdgeSeeds();
    protected static final List<ElementSeed> ALL_SEEDS = getAllSeeds();
    protected static final List<Object> ALL_SEED_VERTICES = getAllSeededVertices();


    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        addDefaultElements();
    }

    @Test
    public void shouldGetElements() throws Exception {
        for (boolean includeEntities : Arrays.asList(true, false)) {
            for (IncludeEdgeType includeEdgeType : IncludeEdgeType.values()) {
                if (!includeEntities && IncludeEdgeType.NONE == includeEdgeType) {
                    // Cannot query for nothing!
                    continue;
                }
                for (IncludeIncomingOutgoingType inOutType : IncludeIncomingOutgoingType.values()) {
                    try {
                        shouldGetElementsBySeed(includeEntities, includeEdgeType, inOutType);
                    } catch (AssertionError e) {
                        throw new AssertionError("GetElementsBySeed failed with parameters: includeEntities=" + includeEntities
                                + ", includeEdgeType=" + includeEdgeType.name() + ", inOutType=" + inOutType, e);
                    }

                    try {
                        shouldGetRelatedElements(includeEntities, includeEdgeType, inOutType);
                    } catch (AssertionError e) {
                        throw new AssertionError("GetRelatedElements failed with parameters: includeEntities=" + includeEntities
                                + ", includeEdgeType=" + includeEdgeType.name() + ", inOutType=" + inOutType, e);
                    }
                }
            }
        }
    }

    @Test
    public void shouldReturnEmptyIteratorIfNoSeedsProvidedForGetElementsBySeed() throws Exception {
        // Given
        final GetElementsSeed<ElementSeed, Element> op = new GetElementsSeed<>();

        // When
        final Iterable<? extends Element> results = graph.execute(op);

        // Then
        assertFalse(results.iterator().hasNext());
    }

    @Test
    public void shouldReturnEmptyIteratorIfNoSeedsProvidedForGetRelatedElements() throws Exception {
        // Given
        final GetRelatedElements<ElementSeed, Element> op = new GetRelatedElements<>();

        // When
        final Iterable<? extends Element> results = graph.execute(op);

        // Then
        assertFalse(results.iterator().hasNext());
    }

    private void shouldGetElementsBySeed(boolean includeEntities, final IncludeEdgeType includeEdgeType, final IncludeIncomingOutgoingType inOutType) throws Exception {
        final List<Element> expectedElements = new ArrayList<>();
        if (includeEntities) {
            expectedElements.addAll(ENTITIES_EXIST);
        }
        if (IncludeEdgeType.ALL == includeEdgeType || IncludeEdgeType.DIRECTED == includeEdgeType) {
            expectedElements.addAll(EDGES_DIR_EXIST);
        }
        if (IncludeEdgeType.ALL == includeEdgeType || IncludeEdgeType.UNDIRECTED == includeEdgeType) {
            expectedElements.addAll(EDGES_EXIST);
        }

        final List<ElementSeed> seeds;
        if (IncludeEdgeType.NONE != includeEdgeType) {
            if (includeEntities) {
                seeds = ALL_SEEDS;
            } else {
                seeds = EDGE_SEEDS;
            }
        } else if (includeEntities) {
            seeds = ENTITY_SEEDS;
        } else {
            seeds = new ArrayList<>();
        }

        shouldGetElements(expectedElements, SeedMatchingType.EQUAL, includeEdgeType, includeEntities, inOutType, seeds);
    }


    private void shouldGetRelatedElements(boolean includeEntities, final IncludeEdgeType includeEdgeType, final IncludeIncomingOutgoingType inOutType) throws Exception {
        final List<Element> expectedElements = new ArrayList<>();
        if (includeEntities) {
            for (Object identifier : ALL_SEED_VERTICES) {
                expectedElements.add(new Entity(TestGroups.ENTITY, identifier));
            }
        }

        if (IncludeEdgeType.ALL == includeEdgeType || IncludeEdgeType.DIRECTED == includeEdgeType) {
            expectedElements.addAll(Collections.singletonList(new Edge(TestGroups.EDGE, SOURCE_DIR_1, DEST_DIR_1, true)));

            if (IncludeIncomingOutgoingType.BOTH == inOutType || IncludeIncomingOutgoingType.OUTGOING == inOutType) {
                expectedElements.addAll(Collections.singletonList(new Edge(TestGroups.EDGE, SOURCE_DIR_2, DEST_DIR_2, true)));
            }

            if (IncludeIncomingOutgoingType.BOTH == inOutType || IncludeIncomingOutgoingType.INCOMING == inOutType) {
                expectedElements.addAll(Collections.singletonList(new Edge(TestGroups.EDGE, SOURCE_DIR_3, DEST_DIR_3, true)));
            }
        }

        if (IncludeEdgeType.ALL == includeEdgeType || IncludeEdgeType.UNDIRECTED == includeEdgeType) {
            expectedElements.addAll(Arrays.asList(new Edge(TestGroups.EDGE, SOURCE_1, DEST_1, false),
                    new Edge(TestGroups.EDGE, SOURCE_2, DEST_2, false),
                    new Edge(TestGroups.EDGE, SOURCE_3, DEST_3, false)));
        }

        shouldGetElements(expectedElements, SeedMatchingType.RELATED, includeEdgeType, includeEntities, inOutType, ALL_SEEDS);
    }

    private void shouldGetElements(final List<Element> expectedElements, final SeedMatchingType seedMatching,
                                   final IncludeEdgeType includeEdgeType,
                                   final Boolean includeEntities,
                                   final IncludeIncomingOutgoingType inOutType,
                                   final Iterable<ElementSeed> seeds) throws IOException, OperationException {
        // Given
        final GetElements<ElementSeed, Element> op;
        if (SeedMatchingType.EQUAL.equals(seedMatching)) {
            op = new GetElementsSeed<>();
        } else {
            op = new GetRelatedElements<>();
        }
        op.setSeeds(seeds);
        op.setIncludeEntities(includeEntities);
        op.setIncludeEdges(includeEdgeType);
        op.setIncludeIncomingOutGoing(inOutType);
        op.setView(new View.Builder()
                .entity(TestGroups.ENTITY)
                .edge(TestGroups.EDGE)
                .build());


        // When
        final Iterable<? extends Element> results = graph.execute(op);

        // Then
        final List<Element> expectedElementsCopy = Lists.newArrayList(expectedElements);
        for (Element result : results) {
            final ElementSeed seed = ElementSeed.createSeed(result);
            if (result instanceof Entity) {
                Entity entity = (Entity) result;
                assertTrue("Entity was not expected: " + entity, expectedElements.contains(entity));
            } else {
                Edge edge = (Edge) result;
                if (edge.isDirected()) {
                    assertTrue("Edge was not expected: " + edge, expectedElements.contains(edge));
                } else {
                    final Edge edgeReversed = new Edge(TestGroups.EDGE, edge.getDestination(), edge.getSource(), edge.isDirected());
                    expectedElementsCopy.remove(edgeReversed);
                    assertTrue("Edge was not expected: " + seed, expectedElements.contains(result) || expectedElements.contains(edgeReversed));
                }
            }
            expectedElementsCopy.remove(result);
        }

        assertEquals("The number of elements returned was not as expected. Missing elements: " + expectedElementsCopy, expectedElements.size(),
                Lists.newArrayList(results).size());
    }

    private static List<Element> getElements(final List<ElementSeed> seeds) {
        final List<Element> elements = new ArrayList<>(seeds.size());
        for (ElementSeed seed : seeds) {
            if (seed instanceof EntitySeed) {
                elements.add(new Entity(TestGroups.ENTITY, ((EntitySeed) seed).getVertex()));
            } else {
                elements.add(new Edge(TestGroups.EDGE, ((EdgeSeed) seed).getSource(), ((EdgeSeed) seed).getDestination(), ((EdgeSeed) seed).isDirected()));
            }
        }

        return elements;
    }

    private static List<ElementSeed> getEntitySeeds() {
        List<ElementSeed> allSeeds = new ArrayList<>();
        allSeeds.addAll(ENTITY_SEEDS_EXIST);
        allSeeds.addAll(ENTITY_SEEDS_DONT_EXIST);
        return allSeeds;
    }

    private static List<ElementSeed> getEdgeSeeds() {
        List<ElementSeed> allSeeds = new ArrayList<>();
        allSeeds.addAll(EDGE_SEEDS_EXIST);
        allSeeds.addAll(EDGE_DIR_SEEDS_EXIST);
        allSeeds.addAll(EDGE_SEEDS_DONT_EXIST);
        return allSeeds;
    }

    private static List<ElementSeed> getAllSeeds() {
        List<ElementSeed> allSeeds = new ArrayList<>();
        allSeeds.addAll(ENTITY_SEEDS);
        allSeeds.addAll(EDGE_SEEDS);
        return allSeeds;
    }

    private static List<Object> getAllSeededVertices() {
        List<Object> allSeededVertices = new ArrayList<>();
        for (ElementSeed elementSeed : ENTITY_SEEDS_EXIST) {
            allSeededVertices.add(((EntitySeed) elementSeed).getVertex());
        }

        for (ElementSeed elementSeed : EDGE_SEEDS_EXIST) {
            allSeededVertices.add(((EdgeSeed) elementSeed).getSource());
            allSeededVertices.add(((EdgeSeed) elementSeed).getDestination());
        }

        for (ElementSeed elementSeed : EDGE_DIR_SEEDS_EXIST) {
            allSeededVertices.add(((EdgeSeed) elementSeed).getSource());
            allSeededVertices.add(((EdgeSeed) elementSeed).getDestination());
        }

        return allSeededVertices;
    }
}