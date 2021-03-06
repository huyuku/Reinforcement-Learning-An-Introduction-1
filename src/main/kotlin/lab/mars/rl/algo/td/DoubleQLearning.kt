package lab.mars.rl.algo.td

import lab.mars.rl.algo.V_from_Q
import lab.mars.rl.model.impl.mdp.*
import lab.mars.rl.model.isNotTerminal
import lab.mars.rl.model.log
import lab.mars.rl.util.log.debug
import lab.mars.rl.util.math.Rand
import lab.mars.rl.util.math.argmax
import lab.mars.rl.util.tuples.tuple3

fun IndexedMDP.DoubleQLearning(
    ε: Double,
    α: (IndexedState, IndexedAction) -> Double,
    episodes: Int): OptimalSolution {
  fun `ε-greedy`(s: IndexedState, Q1: ActionValueFunction, Q2: ActionValueFunction, π: IndexedPolicy) {
    val a_opt = argmax(s.actions) { Q1[s, it] + Q2[s, it] }
    val size = s.actions.size
    for (a in s.actions) {
      π[s, a] = when {
        a === a_opt -> 1 - ε + ε / size
        else -> ε / size
      }
    }
  }
  
  val π = IndexedPolicy(QFunc { 0.0 })
  var Q1 = QFunc { 0.0 }
  var Q2 = QFunc { 0.0 }
  
  for (episode in 1..episodes) {
    log.debug { "$episode/$episodes" }
    var s = started()
    while (true) {
      `ε-greedy`(s, Q1, Q2, π)
      val a = π(s)
      val (s_next, reward) = a.sample()
      if (Rand().nextBoolean()) {
        val tmp = Q1
        Q1 = Q2
        Q2 = tmp
      }
      if (s_next.isNotTerminal) {
        Q1[s, a] += α(s, a) * (reward + γ * Q2[s_next, argmax(s_next.actions) { Q1[s_next, it] }] - Q1[s, a])
        s = s_next
      } else {
        Q1[s, a] += α(s, a) * (reward + γ * 0.0 - Q1[s, a])//Q[terminalState,*]=0.0
        break
      }
    }
  }
  val V = VFunc { 0.0 }
  val result = tuple3(π, V, Q1)
  V_from_Q(states, result)
  return result
}