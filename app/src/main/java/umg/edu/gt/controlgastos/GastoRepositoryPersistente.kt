package umg.edu.gt.controlgastos

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import edu.umg.programacion2.gastos.datos.GastoRepository
import edu.umg.programacion2.gastos.modelo.Gasto
import edu.umg.programacion2.gastos.modelo.Categoria

class GastoRepositoryPersistente(context: Context) :
    SQLiteOpenHelper(context, "control_gastos.db", null, 1),
    GastoRepository {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE gastos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                descripcion TEXT NOT NULL,
                monto REAL NOT NULL,
                categoria TEXT NOT NULL,
                fecha TEXT NOT NULL
            )
        """)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        db.execSQL("DROP TABLE IF EXISTS gastos")
        onCreate(db)
    }

    override suspend fun agregar(gasto: Gasto): Gasto {
        val valores = ContentValues().apply {
            put("descripcion", gasto.descripcion)
            put("monto", gasto.monto)
            put("categoria", gasto.categoria.name)
            put("fecha", gasto.fecha)
        }

        val id = writableDatabase.insert("gastos", null, valores)

        if (id == -1L) {
            throw IllegalStateException("No se pudo guardar el gasto")
        }

        return gasto.copy(id = id.toInt())
    }

    override suspend fun listar(): List<Gasto> {
        val lista = mutableListOf<Gasto>()

        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM gastos ORDER BY id DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                lista.add(convertirGasto(it))
            }
        }

        return lista
    }

    override suspend fun buscarPorId(id: Int): Gasto? {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM gastos WHERE id = ?",
            arrayOf(id.toString())
        )

        cursor.use {
            if (it.moveToFirst()) {
                return convertirGasto(it)
            }
        }

        return null
    }

    override suspend fun actualizar(gasto: Gasto): Boolean {
        val valores = ContentValues().apply {
            put("descripcion", gasto.descripcion)
            put("monto", gasto.monto)
            put("categoria", gasto.categoria.name)
            put("fecha", gasto.fecha)
        }

        return writableDatabase.update(
            "gastos",
            valores,
            "id = ?",
            arrayOf(gasto.id.toString())
        ) > 0
    }

    override suspend fun eliminar(id: Int): Boolean {
        return writableDatabase.delete(
            "gastos",
            "id = ?",
            arrayOf(id.toString())
        ) > 0
    }

    private fun convertirGasto(cursor: Cursor): Gasto {
        return Gasto(
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
            descripcion = cursor.getString(
                cursor.getColumnIndexOrThrow("descripcion")
            ),
            monto = cursor.getDouble(
                cursor.getColumnIndexOrThrow("monto")
            ),
            categoria = Categoria.valueOf(
                cursor.getString(cursor.getColumnIndexOrThrow("categoria"))
            ),
            fecha = cursor.getString(
                cursor.getColumnIndexOrThrow("fecha")
            )
        )
    }
}