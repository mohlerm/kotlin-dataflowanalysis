package ch.mohlerm.dataflowanalysis

/**
 * This class defines a generic BackwardFlowAnalysis as a more specific DataFlowAnalysis
 * with two generic parameters
 * E.g. to use for Live Variables or Very busy expressions analysis
 * Define a subclass to write your own BackwardFlowAnalysis, see Example [DefinedFieldsAnalysis]
 *
 *
 * Created by marcel on 5/11/16.

 * @param  The instance we are working on, e.g. BasicBlocks
 * *
 * @param  The content of the sets, e.g. Strings or VariableSymbols
 */
abstract class BackwardFlowAnalysis<R, S> : DataFlowAnalysis<R, S>() {

    /**
     * This method needs to be defined to describe the successor relation
     * in the underlying data structure

     * @param in
     * *
     * @return
     */
    protected abstract fun successors(`in`: R): List<R>

    /**
     * Implement this to define the transport function
     * which defines how the in/out sets get updated based on localGen and localCut

     * @return true if something changed
     */
    override fun trans(): Boolean {
        val oldIn = inSetTotalSize()
        inSet = unionMap(minusMap(outSet, local_cut), local_gen)
        return oldIn != inSetTotalSize()
    }

    /**
     * Implement this to define the update function
     * which defines how the in/out sets get updated based on the predecessors/successors

     * @return true if something changed
     */
    override fun update(): Boolean {
        val oldOut = outSetTotalSize()
        for (curr in outSet.keys) {
            // either we have an empty hash set or we start with the meet of itself (to avoid an intersection clearing everything)
            var newSet: Set<S> = if (successors(curr).size > 0) (inSet[successors(curr)[0]] ?: setOf()) else setOf()
            for (succ in successors(curr)) {
                newSet = meetSet(newSet, inSet[succ] ?: setOf())
            }
            outSet.put(curr, newSet)
        }
        return oldOut != outSetTotalSize()
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
        outSet.put(initial, initialSet)
        iterate()
        return inSet[end] ?: setOf()
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
    override fun analyzeFull(data: List<R>, initial: R, end: R, initialSet: Set<S>): MutableMap<R, Set<S>> {
        init(data)
        outSet.put(initial, initialSet)
        iterate()
        val result = mutableMapOf<R, Set<S>>()
        for (r in data) {
            result.put(r, inSet[r] ?: setOf())
        }
        return result
    }

}