package org.exazoom.kursovayabasyul

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_parking.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Count
import org.exazoom.kursovayabasyul.db.Movings
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.getActiveUser

//  Фрагмент парковки для пользователя

class ParkingFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_parking, container, false)!!
        dbInstance = ParkingDatabase.getInstance(context)!!

        try {
            view.counter_text_view.text = getCount().count.toString()
        } catch (e: Exception) {
            setCount()
            view.counter_text_view.text = getCount().count.toString()
        }


        when {
            getCount().count.toString().toInt() < 1 -> {
                view.parking_button.isEnabled = false
                view.parking_button.text = getString(R.string.zero_places)
            }
            reserveIsEmpty() -> {
                view.parking_button.isEnabled = true
                view.parking_button.text = getString(R.string.reserve_place)
            }
            else -> {
                view.parking_button.isEnabled = true
                view.parking_button.text = getString(R.string.cancel_reserve)
            }
        }

        view.parking_button.setOnClickListener {
            when (view.parking_button.text) {
                getString(R.string.reserve_place) -> {
                    createReserve(Movings(id_client = getClientId(), state = "coming"))
                    view.parking_button.text = getString(R.string.cancel_reserve)
                    view.counter_text_view.text = getCount().count.toString()
                }
                getString(R.string.cancel_reserve) -> {
                    cancelReserve()
                    view.parking_button.text = getString(R.string.reserve_place)
                    view.counter_text_view.text = getCount().count.toString()
                }
            }

        }
        return view
    }

    private fun getCount() = runBlocking {
        async { dbInstance.getCountDao().getAllCount().first() }.await()
    }

    private fun setCount() = runBlocking {
        async { dbInstance.getCountDao().insert(Count("count", 100)) }.await()
    }

    private fun changeCount(count: Count) = runBlocking {
        async { dbInstance.getCountDao().update(count) }.await()
    }

    private fun createReserve(movings: Movings) = runBlocking {
        async { dbInstance.getMovingsDao().insert(movings)
            val count = getCount()
            count.count--
            changeCount(count)
        }.await()
    }

    private fun cancelReserve() = runBlocking {
        async {
            dbInstance.getMovingsDao().getAllMovings().filter { it.id_client == getClientId() }.forEach {
                dbInstance.getMovingsDao().delete(it)
            }
            val count = getCount()
            count.count++
            changeCount(count)
        }.await()
    }

    private fun getClientId(): Int = runBlocking {
        async { dbInstance.getClientsDao().getClientByLogin(getActiveUser(activity)!!).first().id_client }.await()
    }

    private fun reserveIsEmpty() = runBlocking {
        async {
            dbInstance.getMovingsDao().getAllMovings().none { it.id_client == getClientId() && it.state == "coming" }
        }.await()
    }
}