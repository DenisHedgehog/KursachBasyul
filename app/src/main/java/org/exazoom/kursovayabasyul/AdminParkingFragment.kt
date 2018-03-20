package org.exazoom.kursovayabasyul


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_admin_parking.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.db.Tariffs


//  Фрагмент парковки для администратора

class AdminParkingFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_admin_parking, container, false)
        dbInstance = ParkingDatabase.getInstance(context)!!
        var tariffs: Array<Tariffs>
        try {
            addTariffs()
        } catch (e: Exception) { }
        tariffs = getTariffs()
        view.hourly_price_edit_text.setText(tariffs.first { it.tariff == "hourly" }.price.toString(), TextView.BufferType.EDITABLE)
        view.daily_price_edit_text.setText(tariffs.first { it.tariff == "daily" }.price.toString(), TextView.BufferType.EDITABLE)

        view.update_tariffs.setOnClickListener {
            when {
                view.hourly_price_edit_text.text.toString().toInt() < 1 ||
                        view.daily_price_edit_text.text.toString().toInt() < 1 -> toast("Цена на тариф не должна быть меньше 1")
                else -> {
                    tariffs.first { it.tariff == "hourly" }.price = view.hourly_price_edit_text.text.toString().toInt()
                    tariffs.first { it.tariff == "daily" }.price = view.daily_price_edit_text.text.toString().toInt()
                    tariffs.map { updateTariffs(it) }
                    toast("Тарифы обновлены")
                }
            }
        }

        return view
    }

    private fun getTariffs() = runBlocking {
        async { dbInstance.getTariffsDao().getAllTariffs() }.await()
    }

    private fun updateTariffs(tariffs: Tariffs) = runBlocking {
        async { dbInstance.getTariffsDao().update(tariffs) }.await()
    }

    private fun toast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

    private fun addTariffs() = runBlocking {
        async {
            dbInstance.getTariffsDao().insert(Tariffs("hourly", 100))
            dbInstance.getTariffsDao().insert(Tariffs("daily", 1000))
        }.await()
    }

}
