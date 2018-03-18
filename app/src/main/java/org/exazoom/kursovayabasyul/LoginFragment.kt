package org.exazoom.kursovayabasyul

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_dialog_emp_login.*
import kotlinx.android.synthetic.main.alert_dialog_emp_login.view.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import org.exazoom.kursovayabasyul.db.Clients
import org.exazoom.kursovayabasyul.db.ParkingDatabase
import org.exazoom.kursovayabasyul.utils.saveActiveEmp
import org.exazoom.kursovayabasyul.utils.saveActiveUser
import kotlin.math.log


//  Фрагмент для авторизации

class LoginFragment : Fragment() {

    private lateinit var dbInstance: ParkingDatabase

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_login, container, false)!!

        dbInstance = ParkingDatabase.getInstance(activity.applicationContext)!!

        val login = view.findViewById<EditText>(R.id.login_edit_id)
        val password = view.findViewById<EditText>(R.id.password_edit_id)
        val logInButton = view.findViewById<Button>(R.id.login_button_id)
        val signInButton = view.findViewById<Button>(R.id.sign_in_button)

        Log.i("CLIENTS", "COUNT = ${clientsCount()}")

        var triesCount = 0

        logInButton.setOnClickListener {
            when (loginCheck(login.text.toString(), password.text.toString())) {
                true -> {
                    saveActiveUser(activity, login.text.toString())
                    activity.navigation.visibility = View.VISIBLE
                    if (login.text.toString() == "admin") {
                        activity.supportFragmentManager.beginTransaction().replace(R.id.container, AdminParkingFragment()).commit()
                        activity.navigation.selectedItemId = R.id.parking
                        activity.navigation.menu.getItem(2).isEnabled = true
                    } else {
                        activity.supportFragmentManager.beginTransaction().replace(R.id.container, ParkingFragment()).commit()
                        activity.navigation.selectedItemId = R.id.parking
                        activity.navigation.menu.getItem(2).isEnabled = false
                    }
                }
                false -> {
                    if (triesCount < 2) {
                        toast("Неверные данные пользователя\nПопыток осталось: ${2 - triesCount}")
                        triesCount++
                    } else {
                        toast("Отказано в доступе")
                        activity.finishAffinity()
                    }
                }
            }
        }

        signInButton.setOnClickListener {
            activity.supportFragmentManager.beginTransaction().replace(R.id.container, RegistrationFragment()).commit()
        }

        view.emp_login.setOnClickListener {
            getAlertDialog().show()
        }

        return view
    }

    private fun isUserCorrect(login: String, password: String) = dbInstance.getClientsDao().getAllClients()
            .any { it.login == login && it.password == password }

    private fun loginCheck(login: String, password: String) = runBlocking {

        val s = async {
            isUserCorrect(login, password)
        }
        return@runBlocking s.await()
    }

    private fun clientsCount(): Int = runBlocking {
        val s = async { dbInstance.getClientsDao().getAllClients().size }
        s.await()
    }

    private fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    private fun getAlertDialog(): AlertDialog {
        val alertDialog = AlertDialog.Builder(context).create()
        alertDialog.setTitle("Введите секретный ключ сотрудника")
        val input = EditText(context)
        alertDialog.setView(input)
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                { dialog, _ -> dialog.dismiss() })
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Войти",
                { dialog, _ ->
                    logInEmp(input.text.toString())
                    saveActiveEmp(activity, input.text.toString())
                    dialog.dismiss() })
        return alertDialog
    }

    private fun logInEmp(emp: String) = runBlocking{
        val s = async { dbInstance.getEmployeesDao().getAllEmployees().any { emp == it.emp_key } }.await()
        Log.i("EMPLOYEES", "$s")
        if (s) {
            saveActiveEmp(activity, emp)
            activity.supportFragmentManager.beginTransaction().replace(R.id.container, MovingsFragment()).commit()
            activity.navigation.visibility = View.VISIBLE
            activity.navigation.selectedItemId = R.id.parking
            toast("Доступ разрешён")
        } else {
            toast("Отказано в доступе")
        }

    }

}
