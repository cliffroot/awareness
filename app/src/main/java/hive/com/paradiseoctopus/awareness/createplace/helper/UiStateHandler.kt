package hive.com.paradiseoctopus.awareness.createplace.helper

import com.google.android.gms.maps.model.LatLng
import hive.com.paradiseoctopus.awareness.createplace.CreatePlacePresenter
import hive.com.paradiseoctopus.awareness.createplace.timeMillisToHoursMinutesPair
import rx.subjects.PublishSubject
import java.util.*


/**
 * Created by edanylenko on 9/20/16.
 */


class UiStateHandler (val presenter: CreatePlacePresenter) {

    var currentState : State? = State.DISMISS

    enum class State {
        PLACE_PICKER, DEVICE_PICKER, OTHER_OPTIONS, DISMISS, FINISH
    }

    val allowedTransitions : Map<State, Set<State>> =
            mapOf(State.PLACE_PICKER to setOf(State.DEVICE_PICKER),
                  State.DEVICE_PICKER to setOf(State.OTHER_OPTIONS),
                  State.OTHER_OPTIONS to setOf(State.FINISH),
                  State.DISMISS to setOf(State.PLACE_PICKER))

    var history : MutableList<State?> = mutableListOf(State.DISMISS)

    fun next(predicate: (state: State) -> Boolean) {
        val latest : State? = history.last()
        try {
            history.add(allowedTransitions[latest]?.filter(predicate)?.single())
            val new: State? = history.last()
            onEnter(new, FragmentTranstion.FORWARD)
        } catch (ex : NoSuchElementException) {

        }
    }

    fun dismiss() {
        onEnter(State.DISMISS, FragmentTranstion.NONE)
    }

    private fun onEnter(new: State?, transition : FragmentTranstion) {
        presenter.view?.progress(true)
        val previousState = currentState
        currentState = new
        when (new) {
            State.PLACE_PICKER -> presenter.getCurrentLocation().subscribe{
                location ->
                presenter.view?.showPlaceChooser(transition, LatLng(location.latitude, location.longitude),
                                presenter.place.name)
                presenter.view?.progress(false)
            }

            State.DEVICE_PICKER -> presenter.getNearbyDevices().subscribe{
                devices -> presenter.view?.showDeviceChooser(transition, devices,
                    presenter.place.device)
                    presenter.view?.progress(false)

            }

            State.OTHER_OPTIONS -> {
                presenter.view?.showAdditionalSettings(transition,
                        timeMillisToHoursMinutesPair(presenter.place.intervalFrom),
                        timeMillisToHoursMinutesPair(presenter.place.intervalTo),
                        if (presenter.place.code == null) presenter.generatePlaceCode() else presenter.place.code!!,
                        presenter.place.name)
                presenter.view?.progress(false)
            }
            State.DISMISS -> {
                val resultObservable : PublishSubject<Boolean> = PublishSubject.create()
                resultObservable.filter { !it }.subscribe{
                    if (currentState == new) { // if state hasn't changed rollback to previous
                        onEnter(previousState, FragmentTranstion.NONE)
                    } else { // otherwise go to current state
                        onEnter(currentState, FragmentTranstion.NONE)
                    }
                }
                presenter.view?.dismiss(resultObservable)
                presenter.view?.progress(false)
            }
            State.FINISH -> {presenter.view?.progress(false)}
        }
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
