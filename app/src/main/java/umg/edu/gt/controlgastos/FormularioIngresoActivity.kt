package umg.edu.gt.controlgastos

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import umg.edu.gt.controlgastos.databinding.ActivityFormularioIngresoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioIngresoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFormularioIngresoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormularioIngresoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(top = barras.top, bottom = barras.bottom)
            insets
        }
        binding.spTipoIngreso.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Salario", "Comisión", "Bono", "Otros")
        )
        binding.btnGuardarIngreso.setOnClickListener { guardarIngreso() }
    }

    private fun guardarIngreso() {
        val concepto = binding.etConceptoIngreso.text.toString().trim()
        val monto = binding.etMontoIngreso.text.toString().trim().toDoubleOrNull()
        if (concepto.isBlank()) {
            binding.etConceptoIngreso.error = "Escribe el concepto del ingreso"
            return
        }
        if (monto == null || !monto.isFinite() || monto <= 0.0) {
            binding.etMontoIngreso.error = "Ingresa un monto mayor que cero"
            return
        }
        val ingreso = Ingreso(
            concepto = concepto,
            monto = monto,
            tipo = binding.spTipoIngreso.selectedItem.toString(),
            fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        )
        try {
            val guardado = IngresoRepositoryPersistente(this).agregar(ingreso)
            if (guardado) {
                Toast.makeText(this, "Ingreso guardado", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "No se pudo guardar el ingreso", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar el ingreso", Toast.LENGTH_LONG).show()
        }
    }
}
