package hive.com.paradiseoctopus.awareness.createplace

import android.util.Log


/**
 * Created by edanylenko on 9/20/16.
 */


enum class FragmentTranstion {
    NONE, FORWARD, BACKWARD
}

class UiStateHandler (val presenter: CreatePlacePresenter) {

    var currentState : State? = State.DISMISS

    enum class State {
        PLACE_PICKER, DEVICE_PICKER, OTHER_OPTIONS, DISMISS, FINISH
    }

    val allowedTransitions : Map<State, Set<State>> =
            mapOf(State.PLACE_PICKER  to setOf(State.DEVICE_PICKER, State.OTHER_OPTIONS),
                  State.DEVICE_PICKER to setOf(State.OTHER_OPTIONS),
                  State.OTHER_OPTIONS to setOf(State.FINISH),
                  State.DISMISS to setOf(State.PLACE_PICKER))

    var history : MutableList<State?> = mutableListOf(State.DISMISS)

    fun next(predicate: (state: State) -> Boolean) {
        val latest : State? = history.last()
        history.add(allowedTransitions[latest]?.filter(predicate)?.single())
        val new : State? = history.last()
        Log.e("NEW", "n : $new")
        onEnter(new, FragmentTranstion.FORWARD)
    }

    private fun onEnter(new: State?, transition : FragmentTranstion) {
        currentState = new
        Log.e("NewState",  "$new <~")
        when (new) {
            UiStateHandler.State.PLACE_PICKER -> presenter.view?.showPlaceChooser(transition)
            UiStateHandler.State.DEVICE_PICKER -> presenter.view?.showDeviceChooser(transition)
            UiStateHandler.State.OTHER_OPTIONS -> presenter.view?.showAdditionalSettings(transition)
            UiStateHandler.State.DISMISS -> presenter.view?.finish()
            UiStateHandler.State.FINISH -> presenter.view?.finish()
        }
    }

    fun back() {
        history = history.dropLast(1) as MutableList<State?>
        onEnter(history.last(), FragmentTranstion.BACKWARD)
    }

    fun restore() {
        onEnter(history.last(), FragmentTranstion.NONE)
    }

}