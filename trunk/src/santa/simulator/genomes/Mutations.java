/*
 * Mutations2.java
 *
 * (c) 2002-2005 BEAST Development Core Team
 *
 * This package may be distributed under the
 * Lesser Gnu Public Licence (LGPL)
 */
package santa.simulator.genomes;

import java.util.*;

/**
 * The intent with this class is to encapsulate the implementation of a mutation map which
 * stores mutations for a given site.
 * @author rambaut
 *         Date: Apr 22, 2005
 *         Time: 10:07:46 AM
 */
public class Mutations {

    public Mutations() {
    }

    public Mutations(Mutations source) {
        mutationMap.putAll(source.mutationMap);
    }

    public int mutationCount() {
        return mutationMap.size();
    }

    /**
     * Gets the state at a given position. If no mutation exists then the oldState is returned.
     * @param position the position in the sequence
     * @param ancestralState the state that the position would have if no mutation exists
     * @return the state
     */
    public byte getState(int position, byte ancestralState) {
        Byte state = mutationMap.get(positions[position]);
        if (state != null) {
            return state.byteValue();
        }
		return ancestralState;
    }

    /**
     * Returns a set containing all the positions for which mutations exist. These
     * are stored as Integer objects. They can be iterated over and used with getMutation.
     * @return the set of positions
     */
    public Set<Integer> getPositionSet() {
        return mutationMap.keySet();
    }

    /**
     * Gets the mutation state at a given position
     * @param position the position in the sequence
     * @return the state (null if no mutation exists)
     */
    public Byte getMutation(int position) {
        return mutationMap.get(positions[position]);
    }

    /**
     * Gets the mutation state at a given position
     * @param position the position in the sequence
     * @return the state (null if no mutation exists)
     */
    public Byte getMutation(Integer position) {
        return mutationMap.get(position);
    }

    /**
     * Adds a mutation to the map
     * @param position the position in the sequence
     * @param state the new state
     */
    public void addMutation(int position, byte state) {
        mutationMap.put(positions[position], states[state]);
    }

    /**
     * Adds a mutation to the map
     * @param position the position in the sequence
     * @param state the new state
     */
    public void addMutation(Integer position, Byte state) {
        mutationMap.put(position, state);
    }

    /**
     * Removes a mutation from the map
     * @param position the position in the sequence
     */
    public void removeMutation(int position) {
        mutationMap.remove(positions[position]);
    }

    /**
     * Removes a mutation from the map
     * @param position the position in the sequence
     */
    public void removeMutation(Integer position) {
        mutationMap.remove(position);
    }

    public void removeAll() {
        mutationMap.clear();
    }

    public void addAll(Mutations mutations) {
        mutationMap.putAll(mutations.mutationMap);
    }

    private Map<Integer, Byte> mutationMap = new HashMap<Integer, Byte>();

    private static Integer[] positions = null;
    private static Byte[] states = null;

    public static void initialize(int genomeLength, int stateSize) {
        positions = new Integer[genomeLength];
        for (int i = 0; i < genomeLength; i++) {
            positions[i] = new Integer(i);
        }

        states = new Byte[stateSize];
        for (byte i = 0; i < stateSize; i++) {
            states[i] = new Byte(i);
        }
    }

}
