package umg.edu.gt.controlgastos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Los ingresos se almacenan separados para conservar intactos los gastos ya guardados. */
data class Ingreso(
    val id: Int = 0,
    val concepto: String,
    val monto: Double,
    val tipo: String,
    val fecha: String
)

class IngresoRepositoryPersistente(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "control_ingresos.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE ingresos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            concepto TEXT NOT NULL,
            monto REAL NOT NULL,
            tipo TEXT NOT NULL,
            fecha TEXT NOT NULL
        )""")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // La versión 1 no necesita migración.
    }

    fun agregar(ingreso: Ingreso): Boolean {
        val valores = ContentValues().apply {
            put("concepto", ingreso.concepto)
            put("monto", ingreso.monto)
            put("tipo", ingreso.tipo)
            put("fecha", ingreso.fecha)
        }
        return writableDatabase.insert("ingresos", null, valores) != -1L
    }

    fun listar(): List<Ingreso> {
        val resultado = mutableListOf<Ingreso>()
        readableDatabase.rawQuery("SELECT * FROM ingresos ORDER BY fecha DESC, id DESC", null).use { cursor ->
            while (cursor.moveToNext()) {
                resultado.add(Ingreso(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
                    monto = cursor.getDouble(cursor.getColumnIndexOrThrow("monto")),
                    tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo")),
                    fecha = cursor.getString(cursor.getColumnIndexOrThrow("fecha"))
                ))
            }
        }
        return resultado
    }

    fun eliminar(id: Int): Boolean = writableDatabase.delete(
        "ingresos", "id = ?", arrayOf(id.toString())
    ) > 0
}
