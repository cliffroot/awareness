package hive.com.paradiseoctopus.awareness.createplace

import com.google.android.gms.maps.model.LatLng


/**
 * Created by edanylenko on 9/20/16.
 */


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
        onEnter(new, FragmentTranstion.FORWARD)
    }

    private fun onEnter(new: State?, transition : FragmentTranstion) {
        currentState = new
        when (new) {
            UiStateHandler.State.PLACE_PICKER -> presenter.getCurrentLocation().subscribe{
                location -> presenter.view?.showPlaceChooser(transition, LatLng(location.latitude, location.longitude),
                                presenter.place.name)
            }

            UiStateHandler.State.DEVICE_PICKER -> presenter.getNearbyDevices().subscribe{
                devices -> presenter.view?.showDeviceChooser(transition, devices,
                    presenter.place.device)
            }

            UiStateHandler.State.OTHER_OPTIONS -> presenter.view?.showAdditionalSettings(transition,
                    timeMillisToHoursMinutesPair(presenter.place.intervalFrom),
                    timeMillisToHoursMinutesPair(presenter.place.intervalTo),
                    if (presenter.place.code == null) presenter.generatePlaceCode() else presenter.place.code!!,
                    presenter.place.name)
            UiStateHandler.State.DISMISS -> presenter.view?.finish()
            UiStateHandler.State.FINISH -> {}
        }
        presenter.view?.progress(false)
    }

    fun finish() {
        presenter.view?.finish()
    }

    fun back() {
        history = history.dropLast(1) as MutableList<State?>
        onEnter(history.last(), FragmentTranstion.BACKWARD)
    }

    fun restore() {
        onEnter(history.last(), FragmentTranstion.NONE)
    }
}

enum class FragmentTranstion {
    NONE, FORWARD, BACKWARD
}
