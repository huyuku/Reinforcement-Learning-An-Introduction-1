package lab.mars.rl.model.impl

import lab.mars.rl.model.Indexable
import lab.mars.rl.model.IndexedCollection
import lab.mars.rl.model.MDP

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class DimNSet<E>(private val dim: IntArray, private val stride: IntArray, val raw: Array<E>) :
        IndexedCollection<E> {
    override fun init(maker: (IntArray) -> E) {
        val index = IntArray(dim.size)
        for (i in 0 until raw.size) {
            raw[i] = maker(index).apply {
                for (idx in index.size - 1 downTo 0) {
                    index[idx]++
                    if (index[idx] < dim[idx])
                        break
                    index[idx] = 0
                }
            }
        }
    }

    companion object {
        operator inline fun <reified T> invoke(vararg dim: Int): DimNSet<T>
                = invoke(*dim) { null as T }

        operator inline fun <reified T> invoke(vararg dim: Int, element_maker: (IntArray) -> T): DimNSet<T> {
            val stride = IntArray(dim.size)
            stride[stride.size - 1] = 1
            for (a in stride.size - 2 downTo 0)
                stride[a] = dim[a + 1] * stride[a + 1]
            val total = dim[0] * stride[0]
            val index = IntArray(dim.size)
            val raw = Array(total, {
                element_maker(index).apply {
                    for (idx in index.size - 1 downTo 0) {
                        index[idx]++
                        if (index[idx] < dim[idx])
                            break
                        index[idx] = 0
                    }
                }
            })
            return DimNSet(dim, stride, raw)
        }
    }

    private fun offset(idx: IntArray): Int {
        var offset = 0
        if (idx.size != dim.size)
            throw RuntimeException("index.length=${idx.size}  > dim.length=${dim.size}")
        for (a in 0 until idx.size) {
            if (idx[a] < 0 || idx[a] > dim[a])
                throw ArrayIndexOutOfBoundsException("index[$a]= ${idx[a]} while dim[$a]=${dim[a]}")
            offset += idx[a] * stride[a]
        }
        return offset
    }

    private fun combine(indexable: Array<out Indexable>): IntArray {
        var total = 0
        indexable.forEach { total += it.idx.size }
        val idx = IntArray(total)
        var offset = 0
        indexable.forEach {
            System.arraycopy(it.idx, 0, idx, offset, it.idx.size)
            offset += it.idx.size
        }
        return idx
    }

    override operator fun get(vararg index: Int) = raw[offset(index)]

    override operator fun get(indexable: Indexable) = raw[offset(indexable.idx)]

    override operator fun get(vararg indexable: Indexable) = raw[offset(combine(indexable))]

    override operator fun set(vararg index: Int, s: E) {
        raw[offset(index)] = s
    }

    override operator fun set(indexable: Indexable, s: E) {
        raw[offset(indexable.idx)] = s
    }

    override operator fun set(vararg indexable: Indexable, s: E) {
        raw[offset(combine(indexable))] = s
    }

    override fun iterator(): Iterator<E> {
        return object : Iterator<E> {
            var a = 0
            override fun hasNext() = a < raw.size

            override fun next() = raw[a++]
        }
    }
}

fun DimNSetMDP(state_dim: IntArray, action_dim: IntArray, gamma: Double) = MDP(
        states = DimNSet(*state_dim),
        gamma = gamma,
        v_maker = { DimNSet(*state_dim) { 0.0 } },
        q_maker = { DimNSet(*state_dim, *action_dim) { 0.0 } },
        pi_maker = { DimNSet(*state_dim) })

