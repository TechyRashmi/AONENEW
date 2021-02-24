package Extra

import android.content.Context
import android.content.SharedPreferences

//===
class Preferences(private val _context: Context) {



     val _preferences: SharedPreferences
     val _editor: SharedPreferences.Editor
     val prefName = "aone"

    //=====
    fun commit(): Preferences {
        _editor.commit()
        return this
    }

    //=====
    operator fun set(key: String?, value: String?): Preferences {
        _editor.putString(key, value)
        _editor.commit()
        return this
    }

    //=====
    operator fun get(key: String?): String? {
        return _preferences.getString(key, "")
    }

    //=====
    operator fun set(key: String?, value: Int): Preferences {
        _editor.putInt(key, value)
        return this
    }

    operator fun set(key: String?, value: Long): Preferences {
        _editor.putLong(key, value)
        return this
    }

    //=====
    fun getInt(key: String?): Int {
        return _preferences.getInt(key, 0)
    }

    fun getlong(key: String?): Long {
        return _preferences.getInt(key, -1).toLong()
    }

    //=====
    fun setBoolean(key: String?, value: Boolean): Preferences {
        _editor.putBoolean(key, value)
        return this
    }

    //=====
    fun removeKey(key: String?) {
        _editor.remove(key)
    }

    //=====
    fun getBoolean(key: String?): Boolean {
        return _preferences.getBoolean(key, false)
    }

    //=====
    init {
        _preferences = _context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        _editor = _preferences.edit()
    }
}