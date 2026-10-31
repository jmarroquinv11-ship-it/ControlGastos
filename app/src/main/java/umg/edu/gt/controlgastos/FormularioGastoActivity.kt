package umg.edu.gt.controlgastos

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import edu.umg.programacion2.gastos.modelo.Categoria
import edu.umg.programacion2.gastos.modelo.Gasto
import kotlinx.coroutines.launch
import umg.edu.gt.controlgastos.databinding.ActivityFormularioGastoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioGastoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioGastoBinding

    private val control by lazy {
        Dependencias.obtenerControl(this)
    }

    private var gastoEditando: Gasto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFormularioGastoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        respetarBarrasDelSistema()

        binding.spCategoria.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            Categoria.entries
        )

        val id = intent.getIntExtra(EXTRA_ID_GASTO, 0)

        if (id != 0) {
            lifecycleScope.launch {
                cargarGasto(id)
            }
        }

        binding.btnGuardar.setOnClickListener {
            guardar()
        }
    }

    private suspend fun cargarGasto(id: Int) {

        val gasto = control.buscarPorId(id)

        if (gasto == null) {
            Toast.makeText(
                this,
                "No existe el gasto #$id",
                Toast.LENGTH_SHORT
            ).show()

            finish()
            return
        }

        gastoEditando = gasto

        binding.tvTitulo.text = "Editar gasto"
        binding.btnGuardar.text = "Guardar cambios"

        binding.etDescripcion.setText(gasto.descripcion)
        binding.etMonto.setText(gasto.monto.toString())

        binding.spCategoria.setSelection(gasto.categoria.ordinal)
    }

    private fun guardar() {

        val monto = binding.etMonto.text.toString().toDoubleOrNull()

        if (monto == null) {
            binding.etMonto.error = "Ingresa un monto válido"
            return
        }

        val descripcion = binding.etDescripcion.text.toString().trim()

        val categoria = binding.spCategoria.selectedItem as Categoria

        val original = gastoEditando

        lifecycleScope.launch {

            val error = if (original == null) {

                control.registrar(
                    Gasto(
                        descripcion = descripcion,
                        monto = monto,
                        categoria = categoria,
                        fecha = fechaDeHoy()
                    )
                )

            } else {

                control.actualizar(
                    original.copy(
                        descripcion = descripcion,
                        monto = monto,
                        categoria = categoria
                    )
                )
            }

            if (error != null) {
                Toast.makeText(
                    this@FormularioGastoActivity,
                    error,
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            Toast.makeText(
                this@FormularioGastoActivity,
                "Gasto guardado",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    private fun fechaDeHoy(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun respetarBarrasDelSistema() {

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { vista, insets ->

            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            vista.updatePadding(
                top = barras.top,
                bottom = barras.bottom
            )

            insets
        }
    }

    companion object {
        const val EXTRA_ID_GASTO = "id_gasto"
    }
}
