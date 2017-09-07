package lab.mars.rl.model.impl

import lab.mars.rl.model.Action
import lab.mars.rl.model.MDP
import lab.mars.rl.model.State
import org.junit.Test

/**
 * <p>
 * Created on 2017-09-01.
 * </p>
 *
 * @author wumo
 */
class TestNSet {
    @Test
    fun `test null`() {
        val set = NSet<Int>(2, 2)
        val result:Int? = set[0, 0]
    }

    @Test
    fun `inti raw with correct index and 0`() {
        var i = 0
        var set = NSet<Int?>(3, 4, 5) { idx -> println(idx.toList()); i++ }
//            set.forEach { println(it) }
//            println(set[2, 3, 4])
        set[2, 3, 4] = 100
//            println(set[2, 3, 4])
        set = NSet(3, 4, 5)
        set[0, 0, 0] = 1
//            println(set[0, 0, 0])
    }

    @Test
    fun `variational bound`() {
        var i = 0
        val set = NSet<Int>(3, 4) { NSet<Int>(5) { i++ } }
        for (a in set) {
            print("$a,")
        }
        println()
        val a = set[2, 3, 4]
        for (a in 0 until 3)
            for (b in 0 until 4)
                for (c in 0 until 5) {
                    println(set[a, b, c])
                }
        set[2, 3, 4] = 100
        set[0, 0] = NSet(2) { 1 }
        println(set[0, 0, 1])
        println(set[2, 3, 4])
    }

    @Test
    fun `NSetMDP`() {
        val mdp = NSetMDP(gamma = 1.0, state_dim = intArrayOf(3, 4, 5), action_dim = intArrayOf(4))
        val S = mdp.states
        val V = mdp.v_maker()
        val PI = mdp.pi_maker()
        val Q = mdp.q_maker()
        S.init { idx ->
            println(">>${idx[0]},${idx[1]},${idx[2]}")
            val s = State(*idx)
            s.actions = NSet(4) { Action(*it) }
            s
        }
        val index = intArrayOf(2, 3, 4)
        S.set(*index, s = null)
        S.forEach { s ->
            if (s == null) return@forEach
            println(s)
            V[s] = 1.0
            val a = s.actions.firstOrNull()
            PI[s] = s.actions.first()
            if (a == null) return@forEach
            Q[s, a] = 1.0
        }
    }
}

val dim = intArrayOf(2, 3, 4)
val mdp = MDP(
        states = NSet(*dim),
        gamma = 0.9,
        v_maker = { NSet(*dim) { 0.0 } },
        q_maker = { NSet(*dim, 4) { 0.0 } },
        pi_maker = { NSet(*dim, 4) })