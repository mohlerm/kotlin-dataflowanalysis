package ch.mohlerm.dataflowanalysis

import java.util.*

/**
 * This class defines a generic ForwardFlowAnalysis as a more specific DataFlowAnalysis
 * with two generic parameters
 * E.g. to use for Reaching Definitions or Available Expressions
 * Define a subclass to write your own ForwardFlowAnalysis, see Example [DefinedFieldsAnalysis]
 *
 *
 * Created by marcel on 5/11/16.

 * @param  The instance we are working on, e.g. BasicBlocks
 * *
 * @param  The content of the sets, e.g. Strings or VariableSymbols
 */
abstract class ForwardFlowAnalysis<R, S> : DataFlowAnalysis<R, S>() {

    /**
     * This method needs to be defined to describe the predecessor relation
     * in the underlying data structure

     * @param in
     * *
     * @return
     */
    protected abstract fun predecessors(`in`: R): List<R>

    /**
     * Implement this to define the transport function
     * which defines how the in/out sets get updated based on localGen and localCut

     * @return true if something changed
     */
    override protected fun trans(): Boolean {
        val oldOut = outSetTotalSize()
        outSet = unionMap(minusMap(inSet, local_cut), local_gen)
        return oldOut != outSetTotalSize()
    }

    /**
     * Implement this to define the update function
     * which defines how the in/out sets get updated based on the predecessors/successors

     * @return true if something changed
     */
    override  protected fun update(): Boolean {
        val oldIn = inSetTotalSize()
        for (curr in inSet.keys) {
            // either we have an empty hash set or we start with the meet of itself (to avoid an intersection clearing everything)
            var newSet: Set<S> = if (predecessors(curr).size > 0) (outSet.get(predecessors(curr)[0]) ?: setOf()) else setOf()
            for (pred in predecessors(curr)) {
                newSet = meetSet(newSet, outSet.get(pred) ?: setOf())
            }
            inSet.put(curr, newSet)
        }
        return oldIn != inSetTotalSize()
    }

    /**
     * This is the starting point of the analysis. A [DataFlowAnalysis] Subclass needs
     * to implement this and properly initialize the initial data
     * The parameter end is used to know which data should contain the final result
     * If you want to return the data of each R in data see [.analyzeFull]

     * @param data
     * *
     * @param initial
     * *
     * @param end
     * *
     * @param initialSet
     * *
     * @return A set with the property we are interested in for parameter end
     */
    override fun analyze(data: List<R>, initial: R, end: R, initialSet: Set<S>): Set<S> {
        init(data)
        inSet.put(initial, initialSet)
        iterate()
        return outSet.get(end) ?: setOf()
    }

    /**
     * This is the starting point of the analysis. A [DataFlowAnalysis] Subclass needs
     * to implement this and properly initialize the initial data
     * The parameter end is used to know which data should contain the final result
     * In comparison to [.analyze] this returns all out sets in a
     * map from R to Set of S

     * @param data
     * *
     * @param initial
     * *
     * @param end
     * *
     * @param initialSet
     * *
     * @return A map containing the outset of each entry in R
     */
    override fun analyzeFull(data: List<R>, initial: R, end: R, initialSet: Set<S>): Map<R, Set<S>> {
        init(data)
        inSet.put(initial, initialSet)
        iterate()
        val result = HashMap<R, Set<S>>()
        for (r in data) {
            result.put(r, outSet.get(r) ?: setOf())
        }
        return result
    }
}