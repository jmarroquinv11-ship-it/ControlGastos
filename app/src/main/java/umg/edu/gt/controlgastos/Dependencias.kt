package umg.edu.gt.controlgastos

import android.content.Context
import edu.umg.programacion2.gastos.servicio.ControlGastos

object Dependencias {

    private var instancia: ControlGastos? = null

    @Synchronized
    fun obtenerControl(context: Context): ControlGastos {

        if (instancia == null) {
            instancia = ControlGastos(
                GastoRepositoryPersistente(context.applicationContext)
            )
        }

        return instancia!!
    }
}