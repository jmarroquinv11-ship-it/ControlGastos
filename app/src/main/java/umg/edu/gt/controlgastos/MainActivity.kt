package umg.edu.gt.controlgastos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val control by lazy {
        Dependencias.obtenerControl(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRegistrarGasto =
            findViewById<Button>(R.id.btnRegistrarGasto)

        btnRegistrarGasto.setOnClickListener {
            startActivity(
                Intent(this, FormularioGastoActivity::class.java)
            )
        }

        findViewById<Button>(R.id.btnRegistrarIngreso).setOnClickListener {
            startActivity(Intent(this, FormularioIngresoActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { vista, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            vista.setPadding(
                barras.left,
                barras.top,
                barras.right,
                barras.bottom
            )
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarResumen()
    }

    private fun actualizarResumen() {
        lifecycleScope.launch {
            val gastos = control.listar()
            val listaGastos = findViewById<android.widget.ListView>(R.id.listaGastos)

            val ingresos = IngresoRepositoryPersistente(this@MainActivity).listar()
            val movimientos = (gastos.map { gasto ->
                Triple(gasto.fecha, "GASTO: ${gasto.descripcion}  -Q ${"%.2f".format(Locale.US, gasto.monto)}\n${gasto.categoria} | ${gasto.fecha}", Pair(false, gasto.id))
            } + ingresos.map { ingreso ->
                Triple(ingreso.fecha, "INGRESO: ${ingreso.concepto}  +Q ${"%.2f".format(Locale.US, ingreso.monto)}\n${ingreso.tipo} | ${ingreso.fecha}", Pair(true, ingreso.id))
            }).sortedByDescending { it.first }
            val textosGastos = movimientos.map { it.second }

            val adaptador = android.widget.ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_list_item_1,
                textosGastos
            )

            listaGastos.adapter = adaptador
            listaGastos.setOnItemClickListener { _, _, posicion, _ ->

                val seleccion = movimientos[posicion].third
                if (seleccion.first) {
                    android.app.AlertDialog.Builder(this@MainActivity)
                        .setTitle("Eliminar ingreso")
                        .setMessage("¿Deseas eliminar este ingreso?")
                        .setPositiveButton("Eliminar") { _, _ ->
                            IngresoRepositoryPersistente(this@MainActivity).eliminar(seleccion.second)
                            actualizarResumen()
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                    return@setOnItemClickListener
                }
                val gastoSeleccionado = gastos.firstOrNull { it.id == seleccion.second }
                    ?: return@setOnItemClickListener

                val opciones = arrayOf("Editar", "Eliminar", "Cancelar")

                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle(gastoSeleccionado.descripcion)
                    .setItems(opciones) { _, opcion ->

                        when (opcion) {

                            0 -> {
                                val intent = Intent(
                                    this@MainActivity,
                                    FormularioGastoActivity::class.java
                                )
                                intent.putExtra(
                                    FormularioGastoActivity.EXTRA_ID_GASTO,
                                    gastoSeleccionado.id
                                )
                                startActivity(intent)
                            }

                            1 -> {
                                android.app.AlertDialog.Builder(this@MainActivity)
                                    .setTitle("Eliminar gasto")
                                    .setMessage("¿Seguro que deseas eliminar este gasto?")
                                    .setPositiveButton("Eliminar") { _, _ ->
                                        lifecycleScope.launch {
                                            control.eliminar(gastoSeleccionado.id)
                                            actualizarResumen()
                                        }
                                    }
                                    .setNegativeButton("Cancelar", null)
                                    .show()
                            }
                        }
                    }
                    .show()
            }
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val ahora = Calendar.getInstance()

            val hoy = formato.format(ahora.time)

            val inicioSemana = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                val diferencia =
                    (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
                add(Calendar.DAY_OF_MONTH, -diferencia)
            }

            val inicioSemanaTexto = formato.format(inicioSemana.time)

            val mesActual = hoy.substring(0, 7)
            val anioActual = hoy.substring(0, 4)

            val total = gastos.sumOf { it.monto }
            val totalIngresos = ingresos.sumOf { it.monto }

            val totalHoy = gastos
                .filter { it.fecha == hoy }
                .sumOf { it.monto }

            val totalSemana = gastos
                .filter { it.fecha >= inicioSemanaTexto && it.fecha <= hoy }
                .sumOf { it.monto }

            val totalMes = gastos
                .filter { it.fecha.startsWith(mesActual) }
                .sumOf { it.monto }

            val totalAnio = gastos
                .filter { it.fecha.startsWith(anioActual) }
                .sumOf { it.monto }

            findViewById<TextView>(R.id.txtBalance).text =
                "Q %.2f".format(Locale.US, totalIngresos - total)

            findViewById<TextView>(R.id.txtGastosHoy).text =
                "Hoy: Q %.2f".format(Locale.US, totalHoy)

            findViewById<TextView>(R.id.txtGastosSemana).text =
                "Esta semana: Q %.2f".format(Locale.US, totalSemana)

            findViewById<TextView>(R.id.txtGastosMes).text =
                "Este mes: Q %.2f".format(Locale.US, totalMes)

            findViewById<TextView>(R.id.txtGastosAnio).text =
                "Este año: Q %.2f".format(Locale.US, totalAnio)
        }
    }
}
