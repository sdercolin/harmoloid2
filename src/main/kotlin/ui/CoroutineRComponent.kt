package ui

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import react.Component
import react.Props
import react.RComponent
import react.State

abstract class CoroutineRComponent<P : Props, S : State> : RComponent<P, S> {
    constructor() : super()
    constructor(props: P) : super(props)

    private val coroutineScope = MainScope()

    override fun componentWillUnmount() {
        coroutineScope.cancel()
    }

    fun launch(block: suspend () -> Unit) {
        coroutineScope.launch {
            block()
        }
    }
}
