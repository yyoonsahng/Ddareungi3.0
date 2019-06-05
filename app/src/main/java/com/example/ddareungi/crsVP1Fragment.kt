package com.example.ddareungi

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.ddareungi.MainActivity.Companion.courseInfoList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [crsVP1Fragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [crsVP1Fragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class crsVP1Fragment : Fragment() {

    var index=0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        index=this.getArguments()!!.getInt("index")
        Log.v("fragindex",index.toString())

        return inflater.inflate(R.layout.fragment_crs_vp1, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        index=4*index
        setdata(index)
    }
    fun setdata(num:Int){
            index=num
        var drawableTypeArray=context!!.resources.obtainTypedArray(R.array.drawable)
            if(activity!=null) {
                val imageView = activity!!.findViewById<ImageView>(R.id.crsvp1_titleimg)
                val title=activity!!.findViewById<TextView>(R.id.crsvp1_mainTitle)
                val subtitle=activity!!.findViewById<TextView>(R.id.crsvp1_subTitle)
                val bikeStop=activity!!.findViewById<TextView>(R.id.crsvp1_busstop)
                val open=activity!!.findViewById<TextView>(R.id.crsvp1_time)
                val place=activity!!.findViewById<TextView>(R.id.crsvp1_location)
                val tel=activity!!.findViewById<TextView>(R.id.crsvp1_tel)

                imageView.setImageResource(drawableTypeArray.getResourceId(index+1,-1))
                title.text=courseInfoList[index].mainTitle
                subtitle.text= courseInfoList[index].subtitle
                bikeStop.text=courseInfoList[index].bikeStop
                open.text=courseInfoList[index].open
                place.text=courseInfoList[index].location
                tel.text=courseInfoList[index].tel
            }
        }

}
