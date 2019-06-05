package com.example.ddareungi


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.ddareungi.MainActivity.Companion.courseList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class crsVP_title : Fragment() {

    var index:Int=0

    /*   constructor(index:Int){
        activity!!.applicationContext.index
    }
*/
    override fun onCreateView(
               inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val value=this.getArguments()!!.getInt("index")
        index=value
        return inflater.inflate(R.layout.fragment_crs_vp_title, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //액티비티까지 완성된 상태

        setdata(index)
        //디폴트는 첫번째 이미지(imgNum==0)
    }
    fun setdata(num:Int){
        index=num
        if(activity!=null) {
            val drawableTypeArray= resources.obtainTypedArray(R.array.drawable)

            val imageView = activity!!.findViewById<ImageView>(R.id.crsvp_titleimg)

            val title=activity!!.findViewById<TextView>(R.id.crsvp_maintitle)
            val subtitle=activity!!.findViewById<TextView>(R.id.crsvp_subtitile)


            //이미지 뷰도 리소스 배열 만들어서 설정
            imageView.setImageResource(drawableTypeArray.getResourceId(index*5,-1))
            title.text=courseList[index].title
            subtitle.text= courseList[index].subtitle


        }
    }




}
