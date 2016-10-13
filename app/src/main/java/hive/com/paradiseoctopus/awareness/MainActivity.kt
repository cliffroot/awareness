package hive.com.paradiseoctopus.awareness

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import hive.com.paradiseoctopus.awareness.showplaces.ShowPlacesView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ShowPlacesView()).commitNow()

    }


}
