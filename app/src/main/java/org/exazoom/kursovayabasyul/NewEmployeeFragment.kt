package org.exazoom.kursovayabasyul


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.fragment_new_employee.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Employees
import org.exazoom.kursovayabasyul.db.ParkingDatabase


//  Фрагмент для создания нового сотрудника

class NewEmployeeFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_new_employee, container, false)!!
        dbInstance = ParkingDatabase.getInstance(activity.applicationContext)!!

        val telNumberListener = MaskedTextChangedListener(
                "+7 ([000]) [000]-[00]-[00]",
                true,
                view.employee_number,
                null,
                object : MaskedTextChangedListener.ValueListener {
                    override fun onTextChanged(maskFilled: Boolean, extractedValue: String) {
                        Log.d(MainActivity::class.java.simpleName, extractedValue)
                        Log.d(MainActivity::class.java.simpleName, maskFilled.toString())
                    }
                }
        )

        view.employee_number.addTextChangedListener(telNumberListener)
        view.employee_number.onFocusChangeListener = telNumberListener
        view.employee_number.hint = telNumberListener.placeholder()

        view.create_new_employee.setOnClickListener {
            newEmployee(Employees(view.employee_key.text.toString(),
                    view.employee_fio.text.toString(),
                    view.employee_number.text.toString(),
                    view.employee_address.text.toString()))
            toast("Сотрудник успешно создан")
            activity.supportFragmentManager.beginTransaction().replace(R.id.container, EmployeeFragment()).commit()
        }

        return view
    }

    private fun newEmployee(employees: Employees) = runBlocking {
        async { dbInstance.getEmployeesDao().insert(employees) }.await()
    }

    private fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

}
