package com.subashj.happyplace.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happyplacesapp.adapters.HappyPlacesAdapter
import com.example.happyplacesapp.database.DatabaseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.subashj.happyplace.R
import com.subashj.happyplace.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var fabAddHappyPlace :FloatingActionButton = findViewById(R.id.fabAddHappyPlace)
        fabAddHappyPlace.setOnClickListener{
                val intend  = Intent(this, AddHappyPlace::class.java)
            startActivityForResult(intend,ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }
        getHappyPlacesListFromLocalDB()
    }
    private fun setupHappyPlaceRecyclerView(happyplaceList: ArrayList<HappyPlaceModel>){
        rv_happy_places_list.layoutManager = LinearLayoutManager(this)
        rv_happy_places_list.setHasFixedSize(true)
        val placesAdapter = HappyPlacesAdapter(this,happyplaceList)
        rv_happy_places_list.adapter = placesAdapter

        placesAdapter.setOnClickListener(object  : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model : HappyPlaceModel) {
                val intent = Intent(this@MainActivity,HappyPlaceDetailsActivity::class.java)
            intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })
    }
    private fun getHappyPlacesListFromLocalDB(){
            val dbHandler = DatabaseHandler(this)
        val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()
        if (getHappyPlaceList.isNotEmpty()){

                rv_happy_places_list.visibility = View.VISIBLE
                tv_no_records.visibility = View.GONE
                setupHappyPlaceRecyclerView(getHappyPlaceList)

        }else{
            rv_happy_places_list.visibility = View.GONE
            tv_no_records.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == RESULT_OK){
                getHappyPlacesListFromLocalDB()
            }else{
                Log.e("Activity","cancelled or Pressed Back")
            }
        }
    }

    companion object{
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        var EXTRA_PLACE_DETAILS = "Extra_place_Details"
    }
}