package lab.mars.rl.problem

import lab.mars.rl.model.State
import lab.mars.rl.util.Rand
import lab.mars.rl.util.buf.DefaultIntBuf
import lab.mars.rl.util.tuples.tuple2

object SquareWave {
    val domain = 0.0..2.0
    val maxResolution = 100
    fun invoke(x: Double) = if (x in 0.5..1.5) 1.0 else 0.0
    fun sample(): tuple2<State, Double> {
        val x = Rand().nextInt(0, maxResolution)
        val y = invoke(x * 2.0 / maxResolution)
        return tuple2(State(DefaultIntBuf.of(x)), y)
    }
}