package hive.com.paradiseoctopus.awareness

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import hive.com.paradiseoctopus.awareness.createplace.CreatePlaceWithPagerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById(R.id.add_place).setOnClickListener { startActivity(Intent(this, CreatePlaceWithPagerView::class.java)) }

        startService(Intent(this, BackgroundDatabaseListenService::class.java))
    }


}
