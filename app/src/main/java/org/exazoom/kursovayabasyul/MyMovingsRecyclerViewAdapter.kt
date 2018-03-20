package org.exazoom.kursovayabasyul

import android.app.Activity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

import org.exazoom.kursovayabasyul.MovingsFragment.OnListFragmentInteractionListener
import org.exazoom.kursovayabasyul.db.Count
import org.exazoom.kursovayabasyul.db.Employees
import org.exazoom.kursovayabasyul.db.Movings
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.getActiveEmp
import java.text.SimpleDateFormat
import java.util.*


//  Адаптер для списка движений по парковке

class MyMovingsRecyclerViewAdapter(private val activity: Activity, private val mValues: List<Movings>,
                                   private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyMovingsRecyclerViewAdapter.ViewHolder>() {

    private lateinit var dbInstance: ParkingDatabase
    private lateinit var parentContext: ViewGroup

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_movings, parent, false)
        dbInstance = ParkingDatabase.getInstance(parent.context)!!
        parentContext = parent
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.item = mValues[position]
        var move = ""
        when (holder.item!!.state) {
            "coming" -> {
                move = "В пути"
                holder.acceptButton.isEnabled = true
                holder.arriveDate.visibility = View.GONE
                holder.derriveDate.visibility = View.GONE
                holder.price.visibility = View.GONE
            }
            "parking" -> {
                move = "Припаркован"
                holder.acceptButton.isEnabled = true
                holder.arriveDate.text = "Прибыл: " + formateDate(holder.item?.date_arrival!!)
                holder.arriveDate.visibility = View.VISIBLE
                holder.derriveDate.visibility = View.GONE
                holder.price.visibility = View.GONE
            }
            "left" -> {
                move = "Уехал"
                holder.acceptButton.isEnabled = false
                holder.arriveDate.text = "Прибыл: " + formateDate(holder.item?.date_arrival!!)
                holder.derriveDate.text = "Уехал: " + formateDate(holder.item?.date_depart!!)
                holder.price.text = "Цена: " + holder.item?.price.toString()
                holder.arriveDate.visibility = View.VISIBLE
                holder.derriveDate.visibility = View.VISIBLE
                holder.price.visibility = View.VISIBLE
                holder.acceptButton.visibility = View.GONE
            }
        }
        holder.moveTextView.text = "Статус: $move"
        holder.clientTextView.text = "Клиент: ${getClientFio(position)} (${getClientCarNumber(position)})"
        Log.i("MOVINGS", "ACTIVE EMP is ${getActiveEmp(activity)}")
        holder.acceptButton.setOnClickListener {
            Log.i("${holder.clientTextView.text}", "Was clicked")
            val moving = mValues[position]
            val count = getCount()
            when (holder.item!!.state) {
                "coming" -> {
                    moving.date_arrival = Date()
                    moving.emp_arrival_id = getEmp(activity).emp_key
                    moving.state = "parking"
                    changeMove(moving)
                    changeCount(count)
                }
                "parking" -> {
                    moving.date_depart = Date()
                    moving.emp_depart_id = getEmp(activity).emp_key
                    moving.state = "left"
                    var prc: Number = 0
                    when (getClientTariff(position)) {
                        "hourly" -> {
                            prc = calculateHoursDifference(moving.date_arrival!!, moving.date_depart!!) * getTariffPrice("hourly")
                            Log.i("HOURLY PRICE IS ", "$prc for ${calculateHoursDifference(moving.date_arrival!!, moving.date_depart!!)} hours")
                        }
                        "daily" -> {
                            prc = calculateDaysDifference(moving.date_arrival!!, moving.date_depart!!) * getTariffPrice("daily")
                            Log.i("DAILY PRICE IS ", "$prc for ${calculateDaysDifference(moving.date_arrival!!, moving.date_depart!!)} days")
                        }
                    }
                    moving.price = prc.toInt()
                    changeMove(moving)
                    count.count++
                    changeCount(count)
                }
            }
            notifyItemChanged(position)
        }

    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val moveTextView: TextView = mView.findViewById(R.id.move) as TextView
        val clientTextView: TextView = mView.findViewById(R.id.client_name) as TextView
        val arriveDate: TextView = mView.findViewById(R.id.arrive_date) as TextView
        val derriveDate: TextView = mView.findViewById(R.id.derrive_date) as TextView
        val price: TextView = mView.findViewById(R.id.price) as TextView
        val acceptButton: Button = mView.findViewById(R.id.accept_parking_button)
        var item: Movings? = null

        init {

        }

        override fun toString(): String {
            return super.toString() + " '" + clientTextView.text + "'"
        }
    }

    private fun getClientFio(position: Int): String = runBlocking {
        async { dbInstance.getClientsDao().getClientById(mValues[position].id_client).first().fio_client }.await()
    }

    private fun getClientCarNumber(position: Int): String = runBlocking {
        async { dbInstance.getClientsDao().getClientById(mValues[position].id_client).first().car!! }.await()
    }

    private fun getClientTariff(position: Int): String = runBlocking {
        async { dbInstance.getClientsDao().getClientById(mValues[position].id_client).first().tariff }.await()
    }

    private fun getTariffPrice(tariff: String): Int = runBlocking {
        async { dbInstance.getTariffsDao().getAllTariffs().first { it.tariff == tariff }.price }.await()
    }

    private fun changeMove(movings: Movings) = runBlocking {
        async { dbInstance.getMovingsDao().update(movings) }.await()
    }

    private fun toast(context: Context, text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun getEmp(activity: Activity): Employees = runBlocking {
        async { dbInstance.getEmployeesDao().getEmployByKey(getActiveEmp(activity)!!).first() }.await()
    }

    private fun getCount(): Count = runBlocking {
        async { dbInstance.getCountDao().getAllCount().first() }.await()
    }

    private fun changeCount(count: Count) = runBlocking {
        async { dbInstance.getCountDao().update(count) }.await()
    }

    private fun formateDate(date: Date): String {
        val formatter = SimpleDateFormat("HH:mm dd.MM.yy")
        return formatter.format(date)
    }

    private fun calculateHoursDifference(date1: Date, date2: Date): Double {
        return Math.ceil((date2.time - date1.time).toDouble()/(1000*60*60).toDouble())
    }

    private fun calculateDaysDifference(date1: Date, date2: Date): Double {
        return Math.ceil((date2.time - date1.time).toDouble()/(1000*60*60*24).toDouble())
    }

}
