// WITH_RUNTIME
// WITH_COROUTINES
import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*
import kotlin.reflect.KProperty

suspend fun suspendThere(v: String): String = suspendCoroutineOrReturn { x ->
    x.resume(v)
    COROUTINE_SUSPENDED
}

class A(val x: String) {
    var isSetValueCalled = false
    var isProvideDelegateCalled = false
    var isMinusAssignCalled = false
    var isIncCalled = false
    operator suspend fun component1() = suspendThere(x + "K")
    operator suspend fun getValue(thisRef: Any?, property: KProperty<*>) = suspendThere(x + "K")
    operator suspend fun setValue(thisRef: Any?, property: KProperty<*>, value: String): Unit = suspendCoroutineOrReturn { x ->
        if (value != "56") return@suspendCoroutineOrReturn Unit
        isSetValueCalled = true
        x.resume(Unit)
        COROUTINE_SUSPENDED
    }

    operator suspend fun provideDelegate(host: Any?, p: Any): A = suspendCoroutineOrReturn { x ->
        isProvideDelegateCalled = true
        x.resume(this)
        COROUTINE_SUSPENDED
    }

    operator suspend fun plus(y: String) = suspendThere(x + y)
    operator suspend fun unaryPlus() = suspendThere(x + "K")

    operator suspend fun inc(): A = suspendCoroutineOrReturn { x ->
        isIncCalled = true
        x.resume(this)
        COROUTINE_SUSPENDED
    }

    operator suspend fun minusAssign(y: String): Unit = suspendCoroutineOrReturn { x ->
        if (y != "56") return@suspendCoroutineOrReturn Unit
        isMinusAssignCalled = true
        x.resume(Unit)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

var a = A("O")

suspend fun foo1() {
    var x by a

    if (x != "OK") throw RuntimeException("fail 1")

    x = "56"

    if (!a.isSetValueCalled || !a.isProvideDelegateCalled) throw RuntimeException("fail 2")
}

suspend fun foo2() {
    val (y) = a
    if (y != "OK") throw RuntimeException("fail 3")
}

suspend fun foo3() {
    val y = a + "K"
    if (y != "OK") throw RuntimeException("fail 4")
}

suspend fun foo4() {
    val y = + a
    if (y != "OK") throw RuntimeException("fail 5")
}

// TODO: KT-15930
//suspend fun foo5() {
//    a -= "56"
//    if (!a.isMinusAssignCalled) throw RuntimeException("fail 6")
//}

suspend fun foo6() {
    var y = a++
    if (!y.isIncCalled) throw RuntimeException("fail 7")
}

suspend fun foo7() {
    a.isIncCalled = false
    val y = ++a
    if (!y.isIncCalled) throw RuntimeException("fail 8")
}

fun box(): String {

    builder {
        foo1()
        foo2()
        foo3()
        foo4()
        //foo5()
        foo6()
        foo7()
    }

    return "OK"
}
