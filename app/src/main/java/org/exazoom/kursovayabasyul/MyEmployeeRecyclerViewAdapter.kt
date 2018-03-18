package org.exazoom.kursovayabasyul

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import org.exazoom.kursovayabasyul.EmployeeFragment.OnListFragmentInteractionListener
import org.exazoom.kursovayabasyul.db.Employees
import android.widget.Toast
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.ParkingDatabase


//  Адаптер для списка сотрудников

class MyEmployeeRecyclerViewAdapter(private val mValues: List<Employees>,
                                    private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyEmployeeRecyclerViewAdapter.ViewHolder>() {

    private lateinit var context: Context
    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_employee, parent, false)
        context = view.context
        dbInstance = ParkingDatabase.getInstance(context.applicationContext)!!
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.employeeFio.text = "Сотрудник: ${mValues[position].fio_emp}"
        holder.employeeKey.text = "Ключ: ${mValues[position].emp_key}"

        holder.mView.setOnLongClickListener {
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle("Удалить сотрудника?")
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Нет",
                    { dialog, _ -> dialog.dismiss() })
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Да",
                    { dialog, _ ->
                        deleteEmployee(mValues[position])
                        toast("Сотрудник удалён")
                        notifyItemRemoved(position)
                        dialog.dismiss() })
            alertDialog.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val employeeFio: TextView
        val employeeKey: TextView
        var mItem: Employees? = null

        init {
            employeeFio = mView.findViewById<View>(R.id.id) as TextView
            employeeKey = mView.findViewById<View>(R.id.content) as TextView
        }

        override fun toString(): String {
            return super.toString() + " '" + employeeKey.text + "'"
        }
    }

    private fun deleteEmployee(employees: Employees) = runBlocking {
        val s = async { dbInstance.getEmployeesDao().delete(employees) }
        s.await()
    }

    private fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}
