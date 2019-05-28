package com.github.upthewaterspout.fates.core.states.tree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Static utility methods for manipulating instances of {@link DecisionTree}
 */
public class Trees {
  private Trees() {
    // Private for static class
  }

  /**
   * Estimate the number of nodes in a tree.
   *
   * Currently this is a very basic implementation that
   * just follows one path through the tree to a leaf and
   * assumes that the size of the tree is the product
   * of the choices at each visited node.
   * @param tree The tree to size
   * @return the estimated size of the tree
   */
  public static long estimateSize(DecisionTree<?> tree) {
    if (tree.getState() == DecisionTree.State.UNTESTED) {
      return -1;
    }

    long remainingStates = 1;
    Optional<DecisionTree<?>> optionalTree = Optional.of(tree);
    while (optionalTree.isPresent()) {
      DecisionTree<?> subTree = optionalTree.get();
      long untestedChoices = subTree.getSubTrees().size();
      remainingStates = remainingStates * untestedChoices;
      optionalTree = subTree.getSubTrees().stream()
          .filter(state -> state.getState() != DecisionTree.State.UNTESTED
              && state.getSubTrees().size() > 0)
          .findFirst();
    }

    return remainingStates;
  }

  /**
   * Display the history of a given DecisionTree in a human readable format.
   *
   *
   * This displays all of the decision points in the history of this point
   * where there was more than one choice to be made.
   * @param tree the tree to examine
   * @return a human readable view of the history of decisions that lead to this tree
   */
  public static String showHistory(DecisionTree<?> tree) {

    LinkedList<String> decisionChanges = new LinkedList<>();

    while(tree.getParent() != null) {
      if(tree.getParent().getSubTrees().size() > 1) {
        decisionChanges.addFirst(tree.getParent().getLabel().toString() + ": " + tree.getDecision());
      }
      tree = tree.getParent();
    }


    return "\n========================================" +
        "\nTest Choices:" +
        "\n========================================" +
        "\n" + String.join("\n", decisionChanges) +
        "\n========================================";

  }
}
