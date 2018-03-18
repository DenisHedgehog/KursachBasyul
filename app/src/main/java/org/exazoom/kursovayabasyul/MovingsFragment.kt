package org.exazoom.kursovayabasyul

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_movings_list.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Movings
import org.exazoom.kursovayabasyul.db.ParkingDatabase


//  Фрагмент с движениями по парковке для сотрудника

class MovingsFragment : Fragment() {

    private var mColumnCount = 1
    private var mListener: OnListFragmentInteractionListener? = null
    private lateinit var dbInstance: ParkingDatabase
    private lateinit var adapter: MyMovingsRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_movings_list, container, false)
        dbInstance = ParkingDatabase.getInstance(activity)!!
        var movings = listOf<Movings>()

        view.all_movings.isChecked = true

        when {
            view.all_movings.isChecked -> movings = getMovings()
            view.going.isChecked -> movings = getMovingsByState("coming")
            view.parked.isChecked -> movings = getMovingsByState("parking")
            view.goes_away.isChecked -> movings = getMovingsByState("left")
        }

        view.movings_rg.setOnCheckedChangeListener { radioGroup, i ->
            when {
                view.all_movings.isChecked -> movings = getMovings()
                view.going.isChecked -> movings = getMovingsByState("coming")
                view.parked.isChecked -> movings = getMovingsByState("parking")
                view.goes_away.isChecked -> movings = getMovingsByState("left")
            }
            view.recycler_view.adapter = MyMovingsRecyclerViewAdapter(activity, movings, mListener)
            view.recycler_view.adapter.notifyDataSetChanged()
        }

        adapter = MyMovingsRecyclerViewAdapter(activity, movings, mListener)

        view.recycler_view.adapter = adapter
        view.recycler_view.layoutManager = LinearLayoutManager(view.recycler_view.context)

        return view
    }

    private fun getMovings() = runBlocking {
        async { dbInstance.getMovingsDao().getAllMovings().toList() }.await()
    }

    private fun getMovingsByState(state: String) = runBlocking {
        async { dbInstance.getMovingsDao().getMovingsByState(state).toList() }.await()
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onListFragmentInteraction(item: Movings)
    }

    companion object {

        // TODO: Customize parameter argument names
        private val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        fun newInstance(columnCount: Int): MovingsFragment {
            val fragment = MovingsFragment()
            val args = Bundle()
            args.putInt(ARG_COLUMN_COUNT, columnCount)
            fragment.arguments = args
            return fragment
        }
    }
}
