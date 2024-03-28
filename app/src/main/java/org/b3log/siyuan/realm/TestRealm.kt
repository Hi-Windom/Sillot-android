package org.b3log.siyuan.realm

import android.util.Log
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults

class TestRealm {
    fun onCreate() {
        Log.w("TestRealm","onCreate")
        // 01 打开数据库
        // 使用数据库模式定义 RealmConfiguration，然后使用它打开 Realm。
// use the RealmConfiguration.Builder() for more options
        val configuration = RealmConfiguration.create(schema = setOf(Person::class, Dog::class))
        val realm = Realm.open(configuration)

        // 02 写
        // 通过实例化模型对象并将其复制到开放的 Realm 实例中来持久化一些数据。
        // plain old kotlin object
        val person = Person().apply {
            name = "Carlo"
            dog = Dog().apply { name = "Fido"; age = 16 }
        }

// Persist it in a transaction
        realm.writeBlocking { // this : MutableRealm
            val managedPerson = copyToRealm(person)
        }

        // 03 查询
        // All persons

        val all = realm.query<Person>().find()

// Persons named 'Carlo'
        val personsByNameQuery: RealmQuery<Person> = realm.query<Person>("name = $0", "Carlo")
        val filteredByName: RealmResults<Person> = personsByNameQuery.find()
        Log.w("TestRealm",filteredByName.toString())

// Person having a dog aged more than 7 with a name starting with 'Fi'
        val filteredByDog = realm.query<Person>("dog.age > $0 AND dog.name BEGINSWITH $1", 7, "Fi").find()
        Log.w("TestRealm",filteredByDog.toString())

        // 04 更新
        // Find the first Person without a dog
        realm.query<Person>("dog == NULL LIMIT(1)")
            .first()
            .find()
            ?.also { personWithoutDog ->
                // Add a dog in a transaction
                realm.writeBlocking {
                    findLatest(personWithoutDog)?.dog = Dog().apply { name = "Laika"; age = 3 }
                }
            }

        // 05 删除
        // delete all Dogs
        realm.writeBlocking {
            // Selected by a query
            val query = this.query<Dog>()
            delete(query)

            // From a query result
            val results = query.find()
            delete(results)

            // From individual objects
            results.forEach { delete(it) }
        }

    }

}



class Person : RealmObject {
    var name: String = "Foo"
    var dog: Dog? = null
}

class Dog : RealmObject {
    var name: String = ""
    var age: Int = 0
}