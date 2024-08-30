import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object Keys : Table() {
    val id = integer("id").autoIncrement()
    val key_id = varchar("key_id", 26)

    override val primaryKey = PrimaryKey(id)
}

data class Key(val id: Int, val keyId: String)

class AppConfig private constructor() {
    companion object {
        private val userHome = System.getProperty("user.home")
        private val appDir = File(userHome, ".myapp").also { it.mkdirs() }
        val dbFile = File(appDir, "warp_keys.db")
    }
}

fun initDatabase(): Boolean {
    return try {
        Database.connect("jdbc:h2:${AppConfig.dbFile.absolutePath}", driver = "org.h2.Driver")

        transaction {
            SchemaUtils.create(Keys)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

suspend fun addKey(keyId: String) {
    transaction {
        Keys.insert {
            it[Keys.key_id] = keyId
        }
    }
}

suspend fun getKeys(): List<Key> {
    return transaction {
        Keys.selectAll().map {
            Key(it[Keys.id], it[Keys.key_id])
        }
    }
}