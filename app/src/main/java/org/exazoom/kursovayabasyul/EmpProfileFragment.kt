package org.exazoom.kursovayabasyul


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_emp_profile.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Employees
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.getActiveEmp
import org.exazoom.kursovayabasyul.utils.removeActiveEmp


// Фрагмент профиля для сотрудника

class EmpProfileFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_emp_profile, container, false)
        dbInstance = ParkingDatabase.getInstance(context)!!

        val emp = getEmp()

        view.profile_employee_fio.text = emp.fio_emp
        view.profile_employee_address.setText(emp.adr_emp, TextView.BufferType.EDITABLE)
        view.profile_employee_number.setText(emp.tel_emp, TextView.BufferType.EDITABLE)

        view.change_employee.setOnClickListener {
            emp.adr_emp = view.profile_employee_address.text.toString()
            emp.tel_emp = view.profile_employee_number.text.toString()
            changeEmp(emp)
            toast("Данные изменены")
        }

        view.exit_emp.setOnClickListener {
            removeActiveEmp(activity)
            activity.supportFragmentManager.beginTransaction().replace(R.id.container, LoginFragment()).commit()
        }

        return view
    }

    fun getEmp(): Employees = runBlocking {
        async { dbInstance.getEmployeesDao().getEmployByKey(getActiveEmp(activity)!!).first() }.await()
    }

    private fun changeEmp(employees: Employees) = runBlocking {
        async { dbInstance.getEmployeesDao().update(employees) }.await()
    }

    private fun toast(text: String) {
        Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
    }

}
