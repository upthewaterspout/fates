package com.github.upthewaterspout.fates.core.states.tree;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A tree data structure used to keep track of possible decisions during the execution of a test.
 * <p>
 * Each {@link DecisionTree} represents a single decision point. That decision leads to more
 * decisions, which of which is represented by a {@link DecisionTree}. To get all of the decisions
 * that lead from this state, see {@link #getSubTrees()}
 * <p>
 * This data structure is designed to be explored and populated gradually, So each {@link
 * DecisionTree} through three possible states
 * <p>
 * {@link State#UNTESTED} -&gt; {@link State#PARTIALLY_TESTED} -&gt; {@link State#COMPLETELY_TESTED}
 * <p>
 * An untested {@link DecisionTree} has never been visited, and has unknown choices. A partially
 * tested Tree knows what possible choices should be made,  but some of them may be unexplored. A
 * completely tested Tree had all subtrees explored.
 */
public class DecisionTree<K> {
  private final DecisionTree<?> parent;
  private Map<Object, DecisionTree<?>> subTrees = Collections.emptyMap();

  /**
   * The decision that was made that lead to this DecisionTree
   */
  private final K decision;

  /**
   * The current state of this tree
   */
  private State state;

  /**
   * A description of this decision point
   */
  private Object label;

  /**
   * Create a new decision tree
   * @param parent The parent tree, or null if this is the very first decision to be made
   * @param decision The choice that lead to this tree, or null if this is the very first decision
   */
  public DecisionTree(DecisionTree parent, K decision) {
    this.state = State.UNTESTED;
    this.parent = parent;
    this.decision = decision;
  }

  /**
   * Set the choices that can be made at this decision point. When set, all subtrees will initially
   * be {@link State#UNTESTED}. If there are no options, then this is the end of a test
   * is there for marked as {@link State#COMPLETELY_TESTED}.
   * @param <N> The type of options
   * @param label A textual description of what this decision is, for debugging purposes
   * @param options The choices at this decision point
   */
  public <N> void setOptions(Object label, Set<N> options) {
    switch (getState()) {
      case UNTESTED:
        this.label = label;
        if (options.isEmpty()) {
          done();
        } else {
          subTrees = new LinkedHashMap<>(options.size());
          options.stream()
              .forEach(option -> subTrees.put(option, new DecisionTree<N>(this, option)));
          this.state = State.PARTIALLY_TESTED;
        }
        break;
      case PARTIALLY_TESTED:
        validateOptions(label, options);
        break;
      default:
        throw new IllegalStateException("Exploring a state that has already been visited" + null);
    }
  }

  /**
   * @return true if all subtrees of this tree have been completely explored
   */
  public boolean isCompletelyTested() {
    return getState() == State.COMPLETELY_TESTED;
  }

  /**
   * @return The choice that lead to this decision point
   */
  public K getDecision() {
    return decision;
  }

  /**
   * @return the state of this tree
   */
  public State getState() {
    return state;
  }

  /**
   * Get all decisions that follow from this one. Some of these may actually be the end
   * of the test, or completely unexplored states.
   *
   * @return the immediately following decisions
   */
  public Collection<DecisionTree<?>> getSubTrees() {
    return this.subTrees.values();
  }

  /**
   * Get the label for this tree
   */
  public Object getLabel() {
    return label;
  }

  public DecisionTree<?> getParent() {
    return parent;
  }

  /**
   * Mark this tree as completely tested. This also eagerly looks for parent decisions
   * that can be marked as completely tested and marks them as well.
   */
  private void done() {
    this.state = State.COMPLETELY_TESTED;

    //Recurse over previous states and see mark them
    //done if all child states have been tested
    DecisionTree ancestor = parent;
    while (ancestor != null && ancestor.checkIfComplete()) {
      ancestor = ancestor.parent;
    }
  }

  /**
   * Check if all immediate substrees are completely tested.
   */
  private boolean checkIfComplete() {
    boolean complete = subTrees.values().stream().allMatch(DecisionTree::isCompletelyTested);
    if (complete) {
      state = State.COMPLETELY_TESTED;
    }

    return complete;
  }

  /**
   * Validate the set of options matches the set we previously had to choose between
   */
  private void validateOptions(Object label, Set<?> options) {
    if (!options.equals(this.subTrees.keySet()) || !label.equals(this.label)) {
      throw new IllegalStateException(
          "System was not presented with the same options on the second run.\n"
              + "  Previously: at " + this.label + "\n"
              + "  Now: at " + label + "\n"
              + "  Previous choices: " + this.subTrees.keySet() + "\n"
              + "  New choices " + options);
    }
  }



  /**
   * The possible states for this {@link DecisionTree}
   */
  public enum State {
    /**
     * Indicates that we have tested all subtrees of this tree
     */
    COMPLETELY_TESTED,
    /**
     * Indicates that we know what choices arise this tree, but we have not yet explored all
     * subtrees fully
     */
    PARTIALLY_TESTED,
    /**
     * Indicates this tree has not been explored to find the next choice yet
     */
    UNTESTED;
  }
}