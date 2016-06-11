package ch.mohlerm.dataflowanalysis

/**
 * This class defines a generic DataFlow Analysis
 * with two generic parameters
 * Usually this class should NOT be subclassed
 * Subclass BackwardFlowAnalysis or ForwardFlowAnalysis instead which correctly implement many methods already
 *
 *
 *
 *
 * Created by marcel on 5/11/16.

 * @param  The instance we are working on, e.g. BasicBlocks
 * *
 * @param  The content of the sets, e.g. Strings or VariableSymbols
 */
abstract class DataFlowAnalysis<R, S> {
    protected var inSet: MutableMap<R, Set<S>> = mutableMapOf()
    protected var outSet: MutableMap<R, Set<S>> = mutableMapOf()
    protected var local_cut: MutableMap<R, Set<S>> = mutableMapOf()
    protected var local_gen: MutableMap<R, Set<S>> = mutableMapOf()
    protected val data: MutableList<R> = mutableListOf()

    /**
     * meetSet needs to be defined by the type of Analysis.
     * Usually one can simply delegate the call to
     * [.intersectionSet] intersectionSet} or [.unionSet] unionSet}

     * @param setA
     * *
     * @param setB
     * *
     * @return
     */
    protected abstract fun meetSet(setA: Set<S>, setB: Set<S>): Set<S>

    /**
     * Implement this to define the transport function
     * which defines how the in/out sets get updated based on localGen and localCut

     * @return true if something changed
     */
    protected abstract fun trans(): Boolean

    /**
     * Implement this to define the update function
     * which defines how the in/out sets get updated based on the predecessors/successors

     * @return true if something changed
     */
    protected abstract fun update(): Boolean

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
    abstract fun analyze(data: List<R>, initial: R, end: R, initialSet: Set<S>): Set<S>

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
    abstract fun analyzeFull(data: List<R>, initial: R, end: R, initialSet: Set<S>): Map<R, Set<S>>

    /**
     * This method needs to be implemented by a specific Analysis.
     * localGen describes the generative effect of analysis data (usually a basic block)
     * It needs to update the localGen map according to the given data

     * @param data
     */
    protected abstract fun updateLocalGen(data: R)

    /**
     * This method needs to be implemented by a specific Analysis.
     * localCut describes the destructive effect of analysis data (usually a basic block)
     * It needs to update the localCut map according to the given data

     * @param data
     */
    protected abstract fun updateLocalCut(data: R)

    /**
     * A generic minus operation on a map of sets
     * This will execute a set minus operation on each entry of the map

     * @param mapA
     * *
     * @param mapB
     * *
     * @return
     */
    protected fun minusMap(mapA: MutableMap<R, Set<S>>, mapB: MutableMap<R, Set<S>>): MutableMap<R, Set<S>> {
        val minusMap = mutableMapOf<R, Set<S>>()
        for (key in mapA.keys) {
            val minus : Set<S> = (mapA.get(key) ?: setOf()).minus(mapB.get(key) ?: setOf())
            minusMap.put(key, minus)
        }
        return minusMap
    }

    /**
     * A generic union operation on a map of sets
     * This will execute a set union operation on each entry of the map

     * @param mapA
     * *
     * @param mapB
     * *
     * @return
     */
    protected fun unionMap(mapA: MutableMap<R, Set<S>>, mapB: MutableMap<R, Set<S>>): MutableMap<R, Set<S>> {
        val unionMap = mutableMapOf<R, Set<S>>()
        for (key in mapA.keys) {
            val union : Set<S> = (mapA.get(key) ?: setOf()).union(mapB.get(key) ?: setOf())
            unionMap.put(key, union)
        }
        return unionMap
    }

    /**
     * A generic intersection operation on a map of sets
     * This will execute a set intersection operation on each entry of the map

     * @param mapA
     * *
     * @param mapB
     * *
     * @return
     */
    protected fun intersectionMap(mapA: MutableMap<R, Set<S>>, mapB: MutableMap<R, Set<S>>): MutableMap<R, Set<S>> {
        val intersectionMap  = mutableMapOf<R, Set<S>>()
        for (key in mapA.keys) {
            val intersection : Set<S> = (mapA.get(key) ?: setOf()).intersect(mapB.get(key) ?: setOf())
            intersectionMap.put(key, intersection)
        }
        return intersectionMap
    }

    /**
     * Initializes all the maps with a new set for each entry of param data

     * @param data
     */
    protected fun init(data: List<R>) {
        this.data.addAll(data)
        for (d in this.data) {
            inSet.put(d, setOf())
            outSet.put(d, setOf())
            local_cut.put(d, setOf())
            local_gen.put(d, setOf())
        }
    }

    /**
     * simply counts the total number of inSet values in all maps

     * @return
     */
    protected fun inSetTotalSize(): Int {
        var i = 0
        for (s in inSet.values) {
            i += s.size
        }
        return i
    }

    /**
     * simply counts the total number of outSet values in all maps

     * @return
     */
    protected fun outSetTotalSize(): Int {
        var i = 0
        for (s in outSet.values) {
            i += s.size
        }
        return i
    }

    /**
     * Does the Analysis until there are no more changes

     * @return
     */
    protected fun iterate() {
        var changedUpdate = false
        var changedTrans = false
        do {
            for (d in this.data) {
                updateLocalCut(d)
                updateLocalGen(d)
            }
            changedUpdate = update()
            changedTrans = trans()
        } while (changedTrans or changedUpdate)
    }

}