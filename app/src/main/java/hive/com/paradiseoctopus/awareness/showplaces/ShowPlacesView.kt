package hive.com.paradiseoctopus.awareness.showplaces

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.DatabaseReference
import hive.com.paradiseoctopus.awareness.R
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceWithPagerView
import hive.com.paradiseoctopus.awareness.createplace.PlaceModel
import hive.com.paradiseoctopus.awareness.utils.SimpleDividerItemDecoration

/**
 * Created by edanylenko on 10/5/16.
 */

class ShowPlacesView : ShowPlacesContracts.ShowPlaceView, Fragment() {

    val PRESENTER_TAG = "ShowPlacesPresenter"
    lateinit var presenter : ShowPlacesContracts.ShowPlacePresenter
    lateinit var recycler : RecyclerView
    lateinit var addPlaceButton : View

    override fun displayPlaces(ref: DatabaseReference) {
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(context)
        recycler.addItemDecoration(SimpleDividerItemDecoration(context))
        recycler.adapter = PlacesAdapter(PlaceModel::class.java, R.layout.place_row, PlacesAdapter.PlaceViewHolder::class.java, ref, context)
        addPlaceButton.setOnClickListener { startActivity(Intent(activity, CreatePlaceWithPagerView::class.java)) }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v : View? = inflater?.inflate(R.layout.single_place_fragment, container, false)
        recycler = v?.findViewById(R.id.created_places) as RecyclerView
        addPlaceButton = v?.findViewById(R.id.add_place)!!
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (childFragmentManager.findFragmentByTag(PRESENTER_TAG) == null) {
            presenter = ShowPlacesPresenter(this)
            childFragmentManager.beginTransaction()
                    .add(presenter as Fragment, PRESENTER_TAG)
                    .commitNow()
            presenter.start()
        } else {
            presenter = childFragmentManager.findFragmentByTag(PRESENTER_TAG) as ShowPlacesContracts.ShowPlacePresenter
            presenter.provideView(this)
            presenter.start()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


}